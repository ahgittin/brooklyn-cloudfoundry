Brooklyn Cloud Foundry Plugin
-----------------------------
To build,

    $ go build brooklyn-plugin.go

To install,

    $ cf install-plugin brooklyn-plugin

To run,

    $ cf brooklyn push

this will lookup the manifest.yml, and if it contains a section
called brooklyn, it will use a service broker to create these
services and create a new manifest.temp.yml file taking out
the brooklyn service and replacing it with a services section
containing the service instances created by the brooklyn service
broker. It will then delegate to the original push command with
the manifest.temp.yml file before deleting it.

    $ cf brooklyn add-catalog <brooklyn-url> <path/to/blueprint.yml>
    
this allows new entities to be created and added to the brooklyn
catalog.  The service broker that is associated will need to be
refreshed with `cf update-service-broker` and enabled with 
`enable-service-access` for these new services to become available.

To uninstall,

    $ cf uninstall-plugin BrooklynPlugin
