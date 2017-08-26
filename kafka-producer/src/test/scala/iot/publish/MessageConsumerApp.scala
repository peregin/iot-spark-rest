package iot.publish

import java.util.Collections

import scala.collection.JavaConversions._

object MessageConsumerApp extends App with KafkaConnectivity {

  val consumer = createConsumer()
  consumer.subscribe(Collections.singletonList(topicName))
  println("consumer has been subscribed ...")

  (1 to 100).foreach { i =>
    val records = consumer.poll(1000)
    println(s"got ${records.count()} records ...")
    records.map { record =>
      println(s"got $record")
    }
  }

  println("done")
  consumer.close()
}
