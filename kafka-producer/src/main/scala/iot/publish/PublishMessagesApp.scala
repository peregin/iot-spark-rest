package iot.publish

import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}

import scala.io.Source

object PublishMessagesApp extends App with KafkaConnectivity {

  createTopicIfNeeded()

  val producer = createProducer()

  val entries = Source.fromFile("kafka-producer/src/main/resources/kafka/input.json").getLines()
  //val entries = Source.fromInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream("kafka-producer/src/main/resources/kafka/input.json.gz")))).getLines()

  println("sending ...")
  entries.foreach{json =>
    val data = new ProducerRecord[String, String](topicName, json)
    producer.send(data, new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception) {
        Option(exception).foreach(_.printStackTrace())
        Option(metadata).foreach(m => println(s"sent[${m.offset()}]= $json"))
      }
    })
  }
  println("sent")

  producer.close(10, TimeUnit.SECONDS)
  println("done")
}
