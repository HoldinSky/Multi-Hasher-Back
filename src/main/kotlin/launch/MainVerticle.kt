package launch

import service.CustomVertxService
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class MainVerticle : AbstractVerticle()
{

	override fun start(startPromise: Promise<Void>)
	{
		val cvs = CustomVertxService(vertx)
		val globalRouter = CustomVertxService.getGlobalRouter(cvs)

		vertx
			.createHttpServer()
			.requestHandler(globalRouter)
			.listen(8888)
			.onSuccess {
				println("\nHTTP server is running on port ${it.actualPort()}\n")
			}
	}
}

fun main() {
	val verticle = MainVerticle()

	verticle.start()
}