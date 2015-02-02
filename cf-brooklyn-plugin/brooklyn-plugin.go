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

type BrooklynPlugin struct{}

func (c *BrooklynPlugin) readYAMLFile(path string) (yamlMap generic.Map, err error) {
	fmt.Println("Reading YAML")
	file, err := os.Open(filepath.Clean(path))
	if err != nil {
		return
	}
	defer file.Close()

	yamlMap, err = c.parseManifest(file)
	if err != nil {
		return
	}

	return
}

func (c *BrooklynPlugin) parseManifest(file io.Reader) (yamlMap generic.Map, err error) {
	decoder := candiedyaml.NewDecoder(file)
	yamlMap = generic.NewMap()
	err = decoder.Decode(yamlMap)
	if err != nil {
		return
	}

	if !generic.IsMappable(yamlMap) {
		err = errors.New(T("Invalid manifest. Expected a map"))
		return
	}

	return
}

func (c *BrooklynPlugin) writeYAMLFile(yamlMap generic.Map, path string) {

	fileToWrite, err := os.Create(path)
	if err != nil {
		println("Failed to open file for writing:", err.Error())
		return
	}

	encoder := candiedyaml.NewEncoder(fileToWrite)
	err = encoder.Encode(yamlMap)

	if err != nil {
		return
	}

	return
}

func (c *BrooklynPlugin) assert(cond bool, message string) {
	if !cond {
		panic(errors.New("PLUGIN ERROR: " + message))
	}
}

func (c *BrooklynPlugin) randomString(size int) string{
	rb := make([]byte,size)
  	_, err := rand.Read(rb)
	c.assert(err == nil, "")
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

func (c *BrooklynPlugin) createNewCatalogItem(cliConnection plugin.CliConnection, name string, blueprintMap []interface{}){
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
	brokerUrl, err := c.serviceBrokerUrl(cliConnection, broker)
	c.assert(err == nil, "")
	fmt.Println(brokerUrl)
	c.addCatalog(cliConnection, broker, username, password, tempFile)

	cliConnection.CliCommandWithoutTerminalOutput("update-service-broker", broker, username, password, brokerUrl)
	cliConnection.CliCommandWithoutTerminalOutput("enable-service-access", name)
	err = os.Remove(tempFile)
	if err != nil {
		fmt.Println("PLUGIN ERROR: ", err)
	}
}

func (c *BrooklynPlugin) push(cliConnection plugin.CliConnection, args []string){
	fmt.Println("Running the brooklyn command")
		// modify the application manifest before passing to
		// to original command
					
		// TODO if -f flag sets manifest use that instead
		
		yamlMap, err := c.readYAMLFile("manifest.yml")
		if err != nil {
			fmt.Println("PLUGIN ERROR: ", err)
		}
		//fmt.Println(yamlMap)
		fmt.Println("getting brooklyn")
		// find the section that begins "brooklyn"
		applications := yamlMap.Get("applications").([]interface{})

		for _, app := range applications {
			//fmt.Println("app...\n", app)
			application, found := app.(map[interface{}]interface{})
			c.assert(found, "")
			brooklyn, found := application["brooklyn"].([]interface{})
			c.assert(found, "")

			services := []string{}
			for _, brooklynApp := range brooklyn {
				//fmt.Println("brooklyn app... \n", brooklynApp)
				brooklynApplication, found := brooklynApp.(map[interface{}]interface{})
				c.assert(found, "")
				name, found := brooklynApplication["name"].(string)
				c.assert(found, "")
				location, found := brooklynApplication["location"].(string)
				c.assert(found, "")
				
				// If there is a service section then this refers to an
				// existing catalog entry.
				service, found := brooklynApplication["service"].(string)
				
				// If there is a services section then this is a blueprint
				// and this should be extracted and sent as a catalog item 
				blueprints, found := brooklynApplication["services"].([]interface{})
				if found {
					c.createNewCatalogItem(cliConnection, name, blueprints)
				}
				
				cliConnection.CliCommandWithoutTerminalOutput("create-service", service, location, name)

				services = append(services, name)
			}
			
			// check to see if services section already exists
			if oldServices, found := application["services"].([]interface {}); found {
				for _, name := range oldServices {
					//fmt.Println("found", name)
    				services = append(services, name.(string))
				}
			}
			application["services"] = services
			delete(application, "brooklyn")
			fmt.Println("\nmodified...", application)
		}
		tempFile := "manifest.temp.yml"
		c.writeYAMLFile(yamlMap, tempFile)
		_, err = cliConnection.CliCommand(append(args, "-f", tempFile)...)
		if err != nil {
			fmt.Println("ERROR: ", err)
		}
		err = os.Remove(tempFile)
		if err != nil {
			fmt.Println("PLUGIN ERROR: ", err)
		}
}

func (c *BrooklynPlugin) serviceBrokerUrl(cliConnection plugin.CliConnection, broker string) (string, error){
	brokers, err := cliConnection.CliCommandWithoutTerminalOutput("service-brokers")
	c.assert(err == nil, "")
	for _, a := range brokers {
		fields := strings.Fields(a)	
		if fields[0] == broker { 
			return fields[1], nil
		}
	}
	return "", errors.New("No such broker")
}

func (c *BrooklynPlugin) addCatalog(cliConnection plugin.CliConnection, broker, username, password, filePath string) {
	fmt.Println("Adding Brooklyn catalog item...")
	brokerUrl, err := c.serviceBrokerUrl(cliConnection, broker)
	c.assert(err == nil, "No such broker")
	brooklynUrl, err := url.Parse(brokerUrl)
	c.assert(err == nil, "")	
	brooklynUrl.Path = "create"
	brooklynUrl.User = url.UserPassword(username, password)
	file, err := os.Open(filepath.Clean(filePath))
	c.assert(err == nil, "")
	defer file.Close()
	
	req, err := http.NewRequest("POST", brooklynUrl.String(), file)
	c.assert(err == nil, "")
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	
	client := &http.Client{}
    resp, err := client.Do(req)
    c.assert(err == nil, "")
    defer resp.Body.Close()
	if resp.Status != "200 OK" {
    	fmt.Println("response Status:", resp.Status)
    	fmt.Println("response Headers:", resp.Header)
    	body, _ := ioutil.ReadAll(resp.Body)
    	fmt.Println("response Body:", string(body))
	}
}



func (c *BrooklynPlugin) Run(cliConnection plugin.CliConnection, args []string) {
	defer func() {
        if r := recover(); r != nil {
            fmt.Println(r)
        }
    }()
	switch args[1] {
	case "push":
		c.push(cliConnection, args[1:])
	case "add-catalog":
		c.assert(len(args) == 6, "incorrect number of arguments")
		c.addCatalog(cliConnection, args[2], args[3], args[4], args[5])
		defer fmt.Println("Catalog item sucessfully added.")
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
