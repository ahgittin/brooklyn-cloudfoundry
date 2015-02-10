package main

import (
	"fmt"
	"github.com/cloudfoundry-incubator/candiedyaml"
	"github.com/cloudfoundry/cli/cf/errors"
	. "github.com/cloudfoundry/cli/cf/i18n"
	"github.com/cloudfoundry/cli/generic"
	"github.com/cloudfoundry/cli/plugin"
	"io"
	"io/ioutil"
	"os"
	"path/filepath"
	"net/http"
	//"reflect"
	"strings"
	"net/url"
	"encoding/base64"
    "crypto/rand"
)

type BrooklynPlugin struct{
	cliConnection plugin.CliConnection
	yamlMap generic.Map
}

func (c *BrooklynPlugin) readYAMLFile(path string) {
	//fmt.Println("Reading YAML")
	file, err := os.Open(filepath.Clean(path))
	c.assertErrorIsNil(err)
	defer file.Close()

	yamlMap, err := c.parseManifest(file)
	c.assertErrorIsNil(err)
	c.yamlMap = yamlMap
}

func (c *BrooklynPlugin) parseManifest(file io.Reader) (yamlMap generic.Map, err error) {
	//fmt.Println("Parsing Manifest")
	decoder := candiedyaml.NewDecoder(file)
	yamlMap = generic.NewMap()
	err = decoder.Decode(yamlMap)
	
	c.assertErrorIsNil(err)

	if !generic.IsMappable(yamlMap) {
		err = errors.New(T("Invalid manifest. Expected a map"))
		return
	}

	return
}

func (c *BrooklynPlugin) writeYAMLFile(yamlMap generic.Map, path string) {

	fileToWrite, err := os.Create(path)
	c.assertErrorIsNil(err)

	encoder := candiedyaml.NewEncoder(fileToWrite)
	err = encoder.Encode(yamlMap)

	c.assertErrorIsNil(err)

	return
}

func (c *BrooklynPlugin) assertErrorIsNil(err error) {
	if err != nil {
		c.assert(false, "error not nil, "+err.Error())
	}
}

func (c *BrooklynPlugin) assert(cond bool, message string) {
	if !cond {
		panic(errors.New("PLUGIN ERROR: " + message))
	}
}

func (c *BrooklynPlugin) randomString(size int) string{
	rb := make([]byte,size)
  	_, err := rand.Read(rb)
	c.assertErrorIsNil(err)
	return base64.URLEncoding.EncodeToString(rb)
}

func (c *BrooklynPlugin) promptForBrokerCredentials() (string, string, string){
	var broker, username, password string
	fmt.Printf("Enter broker: ")
	fmt.Scanf("%s", &broker)
	fmt.Printf("Enter username: ")
	fmt.Scanf("%s", &username)
	fmt.Printf("Enter password: ")
	fmt.Scanf("%s", &password)
	return broker, username, password
}

func (c *BrooklynPlugin) createNewCatalogItem(name string, blueprintMap []interface{}){
	yamlMap := generic.NewMap()
	entry := map[string]string{
    	"id": c.randomString(8),
    	"version": "1.0",
    	"iconUrl": "",
    	"description": "A user defined blueprint",
	}
	yamlMap.Set("brooklyn.catalog", entry)
	yamlMap.Set("name", name)
	yamlMap.Set("services", []map[string]interface{}{
		map[string]interface{}{
			"type": "brooklyn.entity.basic.BasicApplication",
			"brooklyn.children": blueprintMap,
		},
	})
	tempFile := "catalog.temp.yml"
	c.writeYAMLFile(yamlMap, tempFile)
	
	broker, username, password := c.promptForBrokerCredentials()
	brokerUrl, err := c.serviceBrokerUrl(broker)
	c.assertErrorIsNil(err)
	//fmt.Println(brokerUrl)
	c.addCatalog(broker, username, password, tempFile)

	c.cliConnection.CliCommand("update-service-broker", broker, username, password, brokerUrl)
	c.cliConnection.CliCommand("enable-service-access", name)
	err = os.Remove(tempFile)
	c.assertErrorIsNil(err)
}

/*
    modify the application manifest before passing to to original command
    TODO: We need to ensure that multiple calls to push do not keep 
	      instantiating new instances of services that are already running
*/
func (c *BrooklynPlugin) push(args []string){
	//fmt.Println("Running the brooklyn command")
	// TODO if -f flag sets manifest use that instead
		
	c.readYAMLFile("manifest.yml")
	
	//fmt.Println("getting brooklyn")
	applications := c.yamlMap.Get("applications").([]interface{})
	for _, app := range applications {
		//fmt.Println("app...\n", app)
		application, found := app.(map[interface{}]interface{})
		c.assert(found, "Application not found.")
		c.replaceBrooklynCreatingServices(application)
	}
	c.pushWith(args, "manifest.temp.yml")
}

func (c *BrooklynPlugin) pushWith(args []string, tempFile string) {
	c.writeYAMLFile(c.yamlMap, tempFile)
	_, err := c.cliConnection.CliCommand(append(args, "-f", tempFile)...)
	c.assertErrorIsNil(err)
	err = os.Remove(tempFile)
	c.assertErrorIsNil(err)
}

func (c *BrooklynPlugin) replaceBrooklynCreatingServices(application map[interface{}]interface{}){
	brooklyn, found := application["brooklyn"].([]interface{})
	c.assert(found, "Brooklyn not found.")
	// check to see if services section already exists
	application["services"] = c.mergeServices(application, c.createAllServices(brooklyn))
	//fmt.Println("\nmodified...", application)
	delete(application, "brooklyn")
	//fmt.Println("\nmodified...", application)
}

func (c *BrooklynPlugin) createAllServices(brooklyn []interface{}) []string{
	services := []string{}
	for _, brooklynApp := range brooklyn {
		//fmt.Println("brooklyn app... \n", brooklynApp)
		brooklynApplication, found := brooklynApp.(map[interface{}]interface{})
		c.assert(found, "Expected Map.")
		services = append(services, c.newService(brooklynApplication))	
	}
	//fmt.Println("finished creating services \n")
	return services
}
func (c *BrooklynPlugin) newService(brooklynApplication map[interface{}]interface{}) string{
	name, found := brooklynApplication["name"].(string)
	c.assert(found, "Expected Name.")
	location, found := brooklynApplication["location"].(string)
	c.assert(found, "Expected Location")
	//fmt.Println("creating service:",name, location)
	c.createServices(brooklynApplication, name, location)
	return name
}

func (c *BrooklynPlugin) mergeServices(application map[interface{}]interface{}, services []string) []string {
	if oldServices, found := application["services"].([]interface {}); found {
		for _, name := range oldServices {
			//fmt.Println("found", name)
    		services = append(services, name.(string))
		}
	}
	return services
}

func (c *BrooklynPlugin) createServices(brooklynApplication map[interface{}]interface{}, name, location string){
	// If there is a service section then this refers to an
	// existing catalog entry.
	service, found := brooklynApplication["service"].(string)
	if found {
		c.cliConnection.CliCommand("create-service", service, location, name)
	} else {
		c.extractAndCreateService(brooklynApplication, name, location)
	}
}

func (c *BrooklynPlugin) extractAndCreateService(brooklynApplication map[interface{}]interface{}, name, location string){
	// If there is a services section then this is a blueprint
	// and this should be extracted and sent as a catalog item 
	blueprints, found := brooklynApplication["services"].([]interface{})
	
	// only do this if catalog doesn't contain it already
	if found {
		//fmt.Println("found catalog entry")
		if exists := c.catalogItemExists(name); !exists {
			c.createNewCatalogItem(name, blueprints)
		}
		c.cliConnection.CliCommand("create-service", name, location, name)
	}
}

func (c *BrooklynPlugin) catalogItemExists(name string) bool {
	services, err := c.cliConnection.CliCommandWithoutTerminalOutput("marketplace", "-s", name)
	if err != nil {
		return false
	}
	
	for _, a := range services {
		fields := strings.Fields(a)
		if fields[0] == "OK" {
			return true
		}
	}
	return false
}

func (c *BrooklynPlugin) serviceBrokerUrl(broker string) (string, error){
	brokers, err := c.cliConnection.CliCommandWithoutTerminalOutput("service-brokers")
	c.assert(err == nil, "")
	for _, a := range brokers {
		fields := strings.Fields(a)	
		if fields[0] == broker { 
			return fields[1], nil
		}
	}
	return "", errors.New("No such broker")
}

func (c *BrooklynPlugin) addCatalog(broker, username, password, filePath string) {
	fmt.Println("Adding Brooklyn catalog item...")
	
	file, err := os.Open(filepath.Clean(filePath))
	c.assertErrorIsNil(err)
	defer file.Close()
	
	req, err := http.NewRequest("POST", c.createRestCallUrlString(broker, username, password, "create"), file)
	c.assertErrorIsNil(err)
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	c.sendRequest(req)
}

/*  Be careful:
    Catalog items should not be deleted if there are running apps 
    which were created using the same item. During rebinding the 
	catalog item is used to reconstruct the entity.
*/
func (c *BrooklynPlugin) deleteCatalog(broker, username, password, name, version string) {
	fmt.Println("Deleting Brooklyn catalog item...")
	req, err := http.NewRequest("DELETE", 
	    c.createRestCallUrlString(broker, username, password, "delete/" +name+ "/" + version + "/"), 
		nil)
	c.assertErrorIsNil(err)
	c.sendRequest(req)
}

func (c *BrooklynPlugin) sendRequest(req *http.Request){
	client := &http.Client{}
    resp, err := client.Do(req)
    c.assertErrorIsNil(err)
    defer resp.Body.Close()
	if resp.Status != "200 OK" {
    	fmt.Println("response Status:", resp.Status)
    	fmt.Println("response Headers:", resp.Header)
    	body, _ := ioutil.ReadAll(resp.Body)
    	fmt.Println("response Body:", string(body))
	}
}

func (c *BrooklynPlugin) createRestCallUrlString(broker, username, password, path string) string{
	brokerUrl, err := c.serviceBrokerUrl(broker)
	c.assert(err == nil, "No such broker")
	brooklynUrl, err := url.Parse(brokerUrl)
	c.assert(err == nil, "")	
	brooklynUrl.Path = path
	brooklynUrl.User = url.UserPassword(username, password)
	return brooklynUrl.String()
}

func (c *BrooklynPlugin) Run(cliConnection plugin.CliConnection, args []string) {
	defer func() {
        if r := recover(); r != nil {
            fmt.Println(r)
        }
    }()
	c.cliConnection = cliConnection
	switch args[1] {
	case "push":
		c.push(args[1:])
	case "add-catalog":
		c.assert(len(args) == 6, "incorrect number of arguments")
		c.addCatalog(args[2], args[3], args[4], args[5])
		defer fmt.Println("Catalog item sucessfully added.")
	case "delete-catalog":
		c.assert(len(args) == 7, "incorrect number of arguments")
		c.deleteCatalog(args[2], args[3], args[4], args[5], args[6])
	}
	fmt.Println("OK")
	
}

func (c *BrooklynPlugin) GetMetadata() plugin.PluginMetadata {
	return plugin.PluginMetadata{
		Name: "BrooklynPlugin",
		Version: plugin.VersionType{
			Major: 1,
			Minor: 0,
			Build: 0,
		},
		Commands: []plugin.Command{
			plugin.Command{
				Name:     "brooklyn",
				HelpText: "Brooklyn plugin command's help text",
				// UsageDetails is optional
				// It is used to show help of usage of each command
				UsageDetails: plugin.Usage{
					Usage: "brooklyn\n   cf brooklyn",
				},
			},
		},
	}
}

func main() {
	plugin.Start(new(BrooklynPlugin))
}
