package main

import (
	"fmt"
	"github.com/cloudfoundry-incubator/candiedyaml"
	"github.com/cloudfoundry/cli/cf/errors"
	. "github.com/cloudfoundry/cli/cf/i18n"
	"github.com/cloudfoundry/cli/generic"
	"github.com/cloudfoundry/cli/plugin"
	"io"
	"os"
	"path/filepath"
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
		fmt.Println("PLUGIN ERROR: ", message)
	}
}

func (c *BrooklynPlugin) Run(cliConnection plugin.CliConnection, args []string) {
	// Ensure that we called the command brooklyn
	if args[1] == "push" {
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
				service, found := brooklynApplication["service"].(string)
				c.assert(found, "")
				// do brooklyn calls here to setup
				cliConnection.CliCommand("create-service", service, location, name)

				services = append(services, name)
			}
			application["services"] = services
			delete(application, "brooklyn")
			fmt.Println("\nmodified...", application)
		}
		c.writeYAMLFile(yamlMap, "manifest.temp.yml")
		_, err = cliConnection.CliCommand(append(args[1:], "-f", "manifest.temp.yml")...)
		if err != nil {
			fmt.Println("ERROR: ", err)
		}
		err = os.Remove("manifest.temp.yml")
		if err != nil {
			fmt.Println("PLUGIN ERROR: ", err)
		}
	}
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
