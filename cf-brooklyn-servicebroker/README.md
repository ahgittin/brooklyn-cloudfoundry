Cloud Foundry Brooklyn Service Broker
-------------------------------------

To run, ensure brooklyn is running, then,


    $ gradle clean
    $ gradle bootRun

The project will then generate a security password,
which should be used for making REST calls, for example

    $ export PASSWORD=the generated password>
    $ curl http://user:$PASSWORD@localhost:8080/v2/catalog
