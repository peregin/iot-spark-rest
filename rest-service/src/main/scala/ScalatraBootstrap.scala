import org.scalatra.LifeCycle
import javax.servlet.ServletContext

import iot.rest.RestService

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) {

    sys.props(org.scalatra.EnvironmentKey) = "dev"

    val rest = new RestService
    context.mount(rest, "/*")
  }

}
