package hashing.handler

import hashing.models.request.HashRequestProps
import hashing.common.HashType
import hashing.common.filesInDirectory
import hashing.logic.hashing.IHasher
import hashing.logic.hashing.IProcessSupervisor
import hashing.logic.SizeCalculator
import hashing.models.request.HashRequest
import hashing.models.result.HashResult
import hashing.models.result.getErrorResult
import hashing.models.task.*
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import kotlinx.coroutines.*
import java.io.File


class HashingHandler(private val supervisor: IProcessSupervisor, private val hasher: IHasher)
{
	private val sizeCalculator = SizeCalculator()

	suspend fun hashFiles(rc: RoutingContext)
	{
		val body = rc.body().asJsonObject()

		val props = parseRequestProperties(body)
		val request = HashRequest(props)

		val state = getInitialState(props.hashTypes.size.toByte())
		supervisor.addNewTask(
			HashTask(
				props,
				TaskStatus.PLANNED,
				state,
			        ) { startHashingTask(request, state) })

		val task = supervisor.startExecutingTask(props.taskId)

		while (task.isActive)
			delay(200)

		supervisor.updateStatusOfTask(props.taskId, TaskStatus.FINISHED)
	}

	private fun startHashingTask(req: HashRequest, state: TaskState): HashResult
	{
		var hashes: Map<HashType, Map<String, String>>

		val file = File(req.path)
		if (!file.exists())
			return getErrorResult(req.hashId, req.path, req.hashTypes, "No such file or directory")

		state.totalBytes =
			if (file.isFile)
				sizeCalculator.calculateSizeForFile(file)
			else
				sizeCalculator.calculateSizeForDirectory(file)

		hashes =
			if (file.isFile)
				hasher.calculateHashOfFile(file, req.hashTypes, state)
					.mapTo(mutableListOf()) { it.key to mapOf(file.name to it.value) }.toMap()
			else
				hasher.calculateHashOfFiles(filesInDirectory(file), req.hashTypes, state)

		return HashResult(
			req.hashId,
			false,
			state.totalBytes / 1024 / 1024,
			req.path,
			req.hashTypes.joinToString(", ") { it.representation },
			hashes,
			null
		                 )
	}

	private fun parseRequestProperties(body: JsonObject): HashRequestProps =
		HashRequestProps(
			body["taskId"],
			(Math.random() * 1_000_000).toLong(),
			body["fullPath"],
			parseHashTypesFromJSON(body["hashTypes"])
		                )

	private fun parseHashTypesFromJSON(typesInString: String): List<HashType>
	{
		val split = typesInString.split(",")
		val hashTypes = mutableListOf<HashType>()
		split.forEach { hashTypes.add(HashType.valueOf(it)) }

		return hashTypes
	}

	fun retrieveFinishedTask(taskId: Long): HashResult
	{
		val result = supervisor.getResultsOfTask(taskId)
		supervisor.cleanUpAfterTask(taskId)

		return result
	}

	fun retrieveProgresses(): List<TaskInProgress>
	{
		val allTasks = supervisor.getAllTasks()

		val allProgresses = mutableListOf<TaskInProgress>()
		allTasks.forEach {
			val progress = supervisor.calculateProgressOfTask(it.state)

			allProgresses.add(
				TaskInProgress(
					it.taskId,
					it.path,
					it.hashTypes,
					progress,
					it.state.speed.toInt() / 1024 / 1024,
					it.status
				              )
			                 )
		}

		return allProgresses
	}

	fun stopTaskById(taskId: Long): HashResult
	{
		val info = supervisor.getInfoAboutTask(taskId)
		val hashResult = HashResult(info.hashId, true, 0L, info.path, info.hashTypes, emptyMap(), "Manual stop")

		supervisor.stopTask(taskId)
		supervisor.cleanUpAfterTask(taskId)

		return hashResult
	}
}