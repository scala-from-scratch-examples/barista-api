package cappuccino

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import httpclient.HttpClient

private[cappuccino] final case class CoffeeBeans(value: String)
private[cappuccino] final case class GroundCoffee(value: String)
private[cappuccino] final case class TampedCoffee(value: String)
private[cappuccino] final case class Espresso(value: String)

private[cappuccino] final class EspressoClient(baseUrl: String) {
  private val httpClient = new HttpClient(baseUrl)

  def grind(coffeeBeans: CoffeeBeans): Future[GroundCoffee] =
    httpClient
      .get(s"/coffee/${HttpClient.encode(coffeeBeans.value)}/ground")
      .flatMap(HttpClient.ifOk(GroundCoffee))

  def tamp(groundCoffee: GroundCoffee): Future[TampedCoffee] =
    httpClient
      .get(s"/coffee/${HttpClient.encode(groundCoffee.value)}/tamped")
      .flatMap(HttpClient.ifOk(TampedCoffee))

  def brew(tampedCoffee: TampedCoffee): Future[Espresso] =
    httpClient
      .get(s"/coffee/${HttpClient.encode(tampedCoffee.value)}/brewed")
      .flatMap(HttpClient.ifOk(Espresso))
}
