Cloud Foundry Brooklyn Service Broker
-------------------------------------

To run, ensure brooklyn is running, then,


    $ gradle clean
    $ gradle bootRun

The project will then generate a security password,
which should be used for making REST calls, for example

    $ export PASSWORD=<the generated password>
    
Then to get the catalog,    
    
    $ curl http://user:$PASSWORD@localhost:8080/v2/catalog
    
To create a WebClusterDatabaseExample from the catalog
    
    $ curl http://user:$PASSWORD@localhost:8080/v2/service_instances/1234 -H "Content-Type: application/json" -d '{ "service_id": "brooklyn.demo.WebClusterDatabaseExample", "plan_id": "localhost", "organization_guid": "300","space_guid":"400"}' -X PUT

    $ curl "http://user:$PASSWORD@localhost:8080/v2/service_instances/1234?service_id=brooklyn&plan_id=brooklyn-plan" -X DELETE
