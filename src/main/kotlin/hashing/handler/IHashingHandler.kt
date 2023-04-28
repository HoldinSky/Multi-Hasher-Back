package hashing.handler

import hashing.models.result.HashResult
import hashing.models.task.TaskInProgress
import io.vertx.ext.web.RoutingContext

interface IHashingHandler {
	suspend fun hashFiles(rc: RoutingContext)

	fun getFinishedTask(taskId: Long): HashResult
	fun getProgresses(): List<TaskInProgress>
	fun stopTaskById(taskId: Long): HashResult
}