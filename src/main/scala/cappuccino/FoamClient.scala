package cappuccino

import httpclient.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private[cappuccino] final case class FoamableLiquid(value: String)
private[cappuccino] final case class Foam(value: String)

private[cappuccino] final class FoamClient(baseUrl: String) {
  private val httpClient = new HttpClient(baseUrl)

  def foam(liquid: FoamableLiquid): Future[Foam] =
    httpClient
      .get(s"/liquid/${HttpClient.encode(liquid.value)}/foamed")
      .flatMap(HttpClient.ifOk(Foam))
}
