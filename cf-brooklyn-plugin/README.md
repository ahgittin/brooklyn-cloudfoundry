Brooklyn Cloud Foundry Plugin
-----------------------------
To build,

    $ go build brooklyn-plugin.go

To install,

    $ cf install-plugin brooklyn-plugin

To run,

    $ cf brooklyn <original-command>

this will lookup the manifest.yml, and if it contains a section
called brooklyn, it will use a service broker to create these
services and create a new manifest.temp.yml file taking out
the brooklyn service and replacing it with a services section
containing the service instances created by the brooklyn service
broker.

To uninstall,

    $ cf uninstall-plugin BrooklynPlugin
