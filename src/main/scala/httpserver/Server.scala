package httpserver

import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

import com.sun.net.httpserver._
import concurrent.{NamedThreadFactory, threadName}

import scala.concurrent.ExecutionContext
import scala.io.{Codec, Source}
import scala.util.{Failure, Success}

class Server(port: Int, router: Router) extends Closeable {
  import Server.createRequest

  private val executor = Executors
    .newSingleThreadExecutor(new NamedThreadFactory("web-server-pool"))
  private implicit val executionContext: ExecutionContext =
    ExecutionContext.fromExecutor(executor)

  private val server = HttpServer.create(new InetSocketAddress(port), 0)
  server.setExecutor(executor)

  server.createContext(
    "/",
    exchange => {
      val request   = createRequest(exchange)
      val requestId = java.util.UUID.randomUUID()
      println(s"[${threadName()}], requestID: $requestId")
      val response = router(request)
      response.onComplete {
        case Success(value) =>
          exchange.sendResponseHeaders(value.status, 0)
          exchange
            .getResponseBody()
            .write(value.body.getBytes(StandardCharsets.UTF_8))
          exchange.close()
          println(
            s"[${threadName()}] sent response on server thread, requestID: $requestId"
          )
        case Failure(exception) =>
          exchange.sendResponseHeaders(500, 0)
          exchange
            .getResponseBody()
            .write(exception.getMessage.getBytes(StandardCharsets.UTF_8))
          exchange.close()
      }
    }
  )
  server.start()
  println(s"Server ready and listening on port $port")

  override def close(): Unit = {
    print("Server shutting down... ")
    server.stop(2)
    executor.shutdown()
    println("bye!")
  }
}
object Server {
  def start(port: Int, router: Router): Server =
    new Server(port, router)

  private def createRequest(exchange: HttpExchange): Request =
    Request(
      method = exchange.getRequestMethod,
      path = exchange.getRequestURI.getPath,
      body = requestBody(exchange)
    )

  private def requestBody(exchange: HttpExchange): String =
    Source
      .fromInputStream(exchange.getRequestBody)(Codec.UTF8)
      .getLines()
      .mkString("\n")
}
