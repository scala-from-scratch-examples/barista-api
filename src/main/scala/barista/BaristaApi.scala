package barista

import concurrent.threadName
import fitzgerald.Methods._
import fitzgerald._
import httpserver.Response._
import httpserver._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class Cappuccino(value: String)
final case class ApiConfig(
    httpPort: Int,
    espressoApiUrl: String,
    foamApiUrl: String
)

final class BaristaApi(apiConfig: ApiConfig) {
  private val espressoClient = new EspressoClient(apiConfig.espressoApiUrl)
  private val foamClient     = new FoamClient(apiConfig.foamApiUrl)

  private val router = Router.of {
    case GET() ~> RootPath() =>
      Future.successful {
        ok(
          """
            |Welcome to the Scala from Scratch barista API!
            |
            |How are you? We serve espresso and cappuccino:
            |
            |POST /barista/order/:orderNumber/espresso
            |POST /barista/order/:orderNumber/cappuccino
      """.stripMargin
        )
      }
    case POST() ~> Path("barista", "order", LongVar(orderId), "espresso") =>
      prepareEspresso(orderId)
        .map(espresso => ok(espresso.value))
    case POST() ~> Path("barista", "order", LongVar(orderId), "cappuccino") =>
      prepareCappuccino(orderId)
        .map(cappuccino => ok(cappuccino.value))
  }

  def launchServer(): Server = Server.start(apiConfig.httpPort, router)

  private def prepareEspresso(orderId: Long): Future[Espresso] =
    Future.failed(new NotImplementedError)

  private def prepareCappuccino(orderId: Long): Future[Cappuccino] =
    Future.failed(new NotImplementedError)
}
