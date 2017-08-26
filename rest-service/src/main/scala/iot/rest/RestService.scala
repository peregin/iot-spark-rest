package iot.rest

import java.util.concurrent.atomic.AtomicLong

import iot.data.SummaryData
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.scalatra.{FutureSupport, ScalatraServlet}

import scala.concurrent.ExecutionContext

class RestService extends ScalatraServlet with JacksonJsonSupport with FutureSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = ExecutionContext.Implicits.global

  val counter = new AtomicLong

  before("/*") {
    contentType = formats("json")
  }

  get("/summary") {
    log("get")
    s"received ${counter.get()} entries so far"
  }

  post("/summary") {
    val entry = parsedBody.extract[SummaryData]
    val at = counter.incrementAndGet()
    log(s"entry[$at] received $entry")
    "ok"
  }
}
