# IOT data processing with Spark

Simple example for IOT processing.
It collects json data from vehicles, it publishes to Kafka, Spark aggregates the data and sends it to a REST service.

```text
  --------------------        --------        ----------        ---------
 |                    |      |        |      |          |      |         |
 | PublishMessagesApp | ---> |   ZK   | ---> | SparkApp | ---> | RestApp |
 |  VehicleData json  |      | Kafka  |      |          |      | Counter |
 |                    |      |        |      |          |      |         |
  --------------------        --------        ----------        ---------
```

## Modules

### iot-data
This module is shared with the rest of the modules, it provides the scala model of the ```VehicleData``` and ```SummaryData```.
Also provides a utility to convert model to json format and vice versa.
In the test resources I was reusing some of my cycling activities recorded with a GPS device to generate test input for Kafka.

### kafka-producer
It creates the topic if doesn't exists yet and it will publish ```VehicleData``` in json format to the Kafka broker.
To have a fully functional setup I was using spotify/kafka Docker image to run the broker locally on my computer.
Run it as: ```iot.publish.PublishMessagesApp```

### rest-service
It exposes the 'summary' endpoint via a rest service. It logs the incoming messages (```SummaryData``` type) and counts them.
If you hit localhost:8888 on the browser, the GET will response with the value of the counter.
Run it as: ```RestApp```

### spark-processor
It reads the input messages from the kafka topic as a stream and it processes it. The results are posted to a
REST service running locally.
The default values (e.g. iot.kafka.topic, iot.rest.endpoint) can be overridden via system properties.
Run it as: ```iot.spark.SparkApp```