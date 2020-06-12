package cappuccino

object BaristaApp extends App {
  val apiConfig = ApiConfig(
    httpPort = sys.env.get("PORT").flatMap(_.toIntOption).getOrElse(8080),
    espressoApiUrl = sys.env.getOrElse(
      "ESPRESSO_API_URL",
      "https://scalafromscratch-espresso.herokuapp.com"
    ),
    foamApiUrl = sys.env
      .getOrElse("FOAM_API_URL", "https://scalafromscratch-foam.herokuapp.com")
  )

  val api    = new BaristaApi(apiConfig)
  val server = api.launchServer()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = {
      server.close()
    }
  })
}
