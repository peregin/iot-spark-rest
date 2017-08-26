
import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object RestApp extends App {

  val server = new Server
  server.setStopTimeout(5000)
  server.setStopAtShutdown(true)

  val connector = new ServerConnector(server)
  connector.setHost("127.0.0.1")
  connector.setPort(8888)

  server.addConnector(connector)

  val webAppContext = new WebAppContext
  webAppContext.setContextPath("/")
  webAppContext.setResourceBase("target/webapp")
  webAppContext.setEventListeners(Array(new ScalatraListener))
  server.setHandler(webAppContext)

  server.start
  server.join
}
