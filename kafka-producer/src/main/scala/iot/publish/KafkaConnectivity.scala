package iot.publish

import java.util.Properties

import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

trait KafkaConnectivity {

  val bootstrapServers = "localhost:9092"
  val zookeeperServers = "localhost:2181"
  val topicName = "iot-spark"

  def createTopicIfNeeded() {

    val zkUtils = ZkUtils(zookeeperServers, 30000, 30000, isZkSecurityEnabled = false)
    val topicNames = zkUtils.getAllTopics()
    println(s"configured topics: \n${topicNames.mkString("\t", ",", "\n")}")

    if (topicNames.find(_.equalsIgnoreCase(topicName)).isEmpty) {
      println(s"creating $topicName topic ...")
      AdminUtils.createTopic(zkUtils, topicName, 1, 1)
      println("topic has been created")
    }

    zkUtils.close()
  }

  def createProducer() = {
    val props = new Properties()
    props.put("client.id", "iot-data")
    props.put("linger.ms", "0")
    props.put("request.timeout.ms", "10000")
    props.put("bootstrap.servers", bootstrapServers)
    props.put("key.serializer", classOf[StringSerializer].getCanonicalName)
    props.put("value.serializer", classOf[StringSerializer].getCanonicalName)
    new KafkaProducer[String, String](props)
  }

  def createConsumer() = {
    val props = new Properties()
    props.put("group.id", "iot-data")
    props.put("bootstrap.servers", bootstrapServers)
    props.put("key.deserializer", classOf[StringDeserializer].getCanonicalName)
    props.put("value.deserializer", classOf[StringDeserializer].getCanonicalName)
    new KafkaConsumer[String, String](props)
  }
}
