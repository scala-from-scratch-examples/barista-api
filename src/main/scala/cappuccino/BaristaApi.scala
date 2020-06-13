package cappuccino

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
      prepareCappuccinoSequentially(orderId)
        .map(cappuccino => ok(cappuccino.value))
  }

  def launchServer(): Server = Server.start(apiConfig.httpPort, router)

  private def prepareEspresso(orderId: Long): Future[Espresso] =
    for {
      ground       <- espressoClient.grind(CoffeeBeans("arabica beans"))
      tampedCoffee <- espressoClient.tamp(ground)
      espresso     <- espressoClient.brew(tampedCoffee)
    } yield Espresso(s"order #$orderId: ${espresso.value}")

  private def prepareCappuccinoSequentially(orderId: Long): Future[Cappuccino] =
    for {
      ground       <- espressoClient.grind(CoffeeBeans("arabica beans"))
      tampedCoffee <- espressoClient.tamp(ground)
      espresso     <- espressoClient.brew(tampedCoffee)
      foam         <- foamClient.foam(FoamableLiquid("oat milk"))
    } yield combine(orderId, espresso, foam)

  private def combine(
      orderId: Long,
      espresso: Espresso,
      foam: Foam
  ): Cappuccino = {
    println(s"[${threadName()}] combining ${espresso.value} and ${foam.value}")
    Cappuccino(
      s"order #$orderId: cappuccino from ${espresso.value} and ${foam.value}"
    )
  }
}
