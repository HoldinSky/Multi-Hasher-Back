package hashing.handler

import hashing.models.request.HashRequestProps
import hashing.common.HashType
import hashing.common.filesInDirectory
import hashing.logic.IHasher
import hashing.logic.IProcessSupervisor
import hashing.logic.SizeCalculator
import hashing.models.request.HashRequest
import hashing.models.request.SingleHashRequest
import hashing.models.result.HashResult
import hashing.models.result.getErrorResult
import hashing.models.task.*
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import kotlinx.coroutines.delay
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

	suspend fun hashFilesAsync(rc: RoutingContext) {
		val body = rc.body().asJsonObject()
		val props = parseRequestProperties(body)

		val requests = mutableListOf<SingleHashRequest>()
		props.hashTypes.forEach {
			requests.add(SingleHashRequest(props.hashId, props.contentPath, it))
		}

		requests.forEach {
			val state = getInitialState(props.hashTypes.size.toByte())
			supervisor.addNewTask(
				HashTask(
					props,
					TaskStatus.PLANNED,
					state,
				        ) { startSingleHashingTask(it, state) })

			val task = supervisor.startExecutingTask(props.taskId)
		}

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
			val progress = supervisor.retrieveProgressOfTask(it.state)

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

	private fun startSingleHashingTask(req: SingleHashRequest, state: TaskState): HashResult
	{
		val file = File(req.path)
		if (!file.exists())
			return getErrorResult(req.hashId, req.path, listOf(req.hashType), "No such file or directory")


		state.totalBytes =
			if (file.isFile)
				sizeCalculator.calculateSizeForFile(file)
			else
				sizeCalculator.calculateSizeForDirectory(file)

		val directoryTree =
			if (!file.isFile)
				filesInDirectory(file)
			else
				null

		val hashes =
			if (file.isFile)
					mapOf(file.name to hasher.calculateHashOfFile(file, req.hashType, state))
				else
					hasher.calculateHashOfFiles(directoryTree!!, file.toURI(), req.hashType, state)


		return HashResult(
			req.hashId,
			false,
			state.totalBytes / 1024 / 1024,
			req.path,
			req.hashType.representation,
			mapOf(req.hashType to hashes),
			null
		                 )
	}

	private fun startHashingTask(req: HashRequest, state: TaskState): HashResult
	{
		val hashTypesInString = req.hashTypes.joinToString(", ") { it.representation }
		val hashes = mutableMapOf<HashType, Map<String, String>>()

		val file = File(req.path)
		if (!file.exists())
			return getErrorResult(req.hashId, req.path, req.hashTypes, "No such file or directory")

		val sizeInBytes =
			if (file.isFile)
				sizeCalculator.calculateSizeForFile(file)
			else
				sizeCalculator.calculateSizeForDirectory(file)

		state.totalBytes = sizeInBytes * state.numberOfHashTypes

		val directoryTree =
			if (!file.isFile)
				filesInDirectory(file)
			else
				null

		for (type in req.hashTypes)
		{
			hashes[type] =
				if (file.isFile)
					mapOf(file.name to hasher.calculateHashOfFile(file, type, state))
				else
					hasher.calculateHashOfFiles(directoryTree!!, file.toURI(), type, state)
		}

		return HashResult(
			req.hashId,
			false,
			sizeInBytes / 1024 / 1024,
			req.path,
			hashTypesInString,
			hashes,
			null
		                 )
	}
}