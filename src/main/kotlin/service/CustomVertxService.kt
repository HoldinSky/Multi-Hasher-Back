package service

import hashing.handler.HashingHandler
import hashing.logic.HashProcessSupervisor
import hashing.logic.MultiHasher
import hashing.models.result.HashResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CustomVertxService(private val vertx: Vertx)
{
	private val hashingHandler: HashingHandler = HashingHandler(HashProcessSupervisor(), MultiHasher())

	private fun getCORSHandler(): CorsHandler
	{
		return CorsHandler.create()
			.addOrigin("http://localhost:3000")
			.allowCredentials(true)
			.allowedMethods(setOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE))
			.allowedHeaders(setOf("Content-Type"))
	}


	@OptIn(DelicateCoroutinesApi::class)
	private fun getHashingHandler(): Handler<RoutingContext> = Handler { rc ->
		GlobalScope.launch(Dispatchers.IO) {
			hashingHandler.hashFiles(rc)

			rc.response().putHeader("Content-Type", "application/json")
			rc.json(
				mapOf(
					"status" to "Successfully started",
				     )
			       )
		}
	}

	private fun getProgressHandler(): Handler<RoutingContext> = Handler { rc ->
		rc.response().putHeader("Content-Type", "application/json")
		rc.json(
			hashingHandler.retrieveProgresses()
		       )
	}

	private fun getFinishedHandler(): Handler<RoutingContext> = Handler { rc ->
		val hashResult = hashingHandler.retrieveFinishedTask(rc.pathParam("taskId").toLong())

		rc.response().putHeader("Content-Type", "application/json")
		generateHashResultsJSON(rc, hashResult)
	}

	private fun getStoppingHandler(): Handler<RoutingContext> = Handler { rc ->
		val hashResult = hashingHandler.stopTaskById(rc.pathParam("taskId").toLong())

		rc.response().putHeader("Content-Type", "application/json")
		generateHashResultsJSON(rc, hashResult)
	}


	companion object
	{
		fun getGlobalRouter(cvs: CustomVertxService): Router
		{
			val router = Router.router(cvs.vertx)

			val customCORSHandler = cvs.getCORSHandler()
			val hashingHandler = cvs.getHashingHandler()
			val stoppingHandler = cvs.getStoppingHandler()
			val progressHandler = cvs.getProgressHandler()
			val finishedTaskHandler = cvs.getFinishedHandler()

			router.route().handler(BodyHandler.create())
			router.route("/*").handler(customCORSHandler)

			router.route("/hashing").handler(hashingHandler)
			router.route("/hashing/stop/:taskId").handler(stoppingHandler)
			router.route("/hashing/task/:taskId").method(HttpMethod.GET).handler(finishedTaskHandler)
			router.route("/hashing/progress").method(HttpMethod.GET).handler(progressHandler)

			return router
		}
	}

	private fun generateHashResultsJSON(rc: RoutingContext, hashResult: HashResult): RoutingContext
	{
		rc.json(
			mapOf(
				"resId" to hashResult.resultId,
				"hasError" to hashResult.hasError,
				"size" to hashResult.size,
				"results" to mapOf(
					"pathToContent" to hashResult.path,
					"hashTypes" to hashResult.hashTypes,
					"hashes" to hashResult.hashes,
					"error" to hashResult.error,
				                  )
			     )
		       )
		return rc
	}
}