Cloud Foundry Brooklyn Service Broker
-------------------------------------

To run, ensure brooklyn is running, then,


    $ gradle clean
    $ gradle bootRun

The by default the project will create a user called user and generate a password.

For testing purposes you can use this for making REST calls without the CF tool, for example

    $ export PASSWORD=<the generated password>
    
You can override this by setting the username and password in the application.properties file

    security.user.name=<new-username>
    security.user.password=<password>
    
Then to get the catalog,    
    
    $ curl http://user:$PASSWORD@localhost:8080/v2/catalog
    
To create a WebClusterDatabaseExample from the catalog,
    
    $ curl http://user:$PASSWORD@localhost:8080/v2/service_instances/1234 -H "Content-Type: application/json" -d '{ "service_id": "brooklyn.demo.WebClusterDatabaseExample", "plan_id": "localhost", "organization_guid": "300","space_guid":"400"}' -X PUT

Then to the delete it,

    $ curl "http://user:$PASSWORD@localhost:8080/v2/service_instances/1234?service_id=brooklyn&plan_id=brooklyn-plan" -X DELETE
    
Using with the CF tool
----------------------

First, register the service broker with BOSH

    $ cf create-service-broker <broker-name> <user> <password> <url>
    
Check for new services that have no access

    $ cf service-access 
    
Enable those services that you wish to appear in the marketplace

    $ cf enable-service-access <service-name>
    
Create the service that you wish to use

    $ cf create-service <service-name> <plan-name> <service-instance-id>
    
Delete the service that you no longer need    

    $ cf delete-service <service-instance-id>
