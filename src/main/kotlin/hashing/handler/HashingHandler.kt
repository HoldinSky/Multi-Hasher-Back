package hashing.handler

import hashing.common.HashType
import hashing.common.TaskStatus
import hashing.common.filesInDirectory
import hashing.logic.IHasher
import hashing.logic.IProcessSupervisor
import hashing.logic.SizeCalculator
import hashing.models.*
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime


class HashingHandler(private val supervisor: IProcessSupervisor, private val hasher: IHasher)
{
	private val sizeCalculator = SizeCalculator()

	suspend fun hashFiles(rc: RoutingContext): HashResult
	{
		val body = rc.body().asJsonObject()

		val taskId: Long = body["taskId"]
		val path: String = body["fullPath"]
		val stringTypes: String = body["hashTypes"]

		val split = stringTypes.split(",")
		val hashTypes = mutableListOf<HashType>()
		split.forEach { hashTypes.add(HashType.valueOf(it)) }

		val hashId = (Math.random() * 1_000_000).toLong()
		val request = HashRequest(hashId, path, hashTypes)

		val state = TaskState(0, 0, 0, HashType.NONE, hashTypes.size.toByte())
		supervisor.addNewTask(
			HashTask(
				taskId,
				hashId,
				path,
				hashTypes.joinToString(", ") { it.representation },
				TaskStatus.PLANNED,
				state
			        ) { startHashingTask(request, state) })

		val task = supervisor.startExecutingTask(taskId)

		// TODO (questionable place, on .join() refuses to cancel!)
		while (task.isActive)
			delay(200)

		val result = supervisor.getResultsOfTask(taskId)

		supervisor.updateStatusOfTask(taskId, TaskStatus.FINISHED)

		return result
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
					it.state.currentHash.representation,
					it.status
				              )
			                 )
		}

		return allProgresses
	}

	fun stopTaskById(taskId: Long): HashResult
	{
		val info = supervisor.getInfoAboutTask(taskId)
		val hashResult = HashResult(info.resId, true, 0L, info.path, info.hashTypes, emptyMap(), "Manual stop")

		supervisor.stopTask(taskId)
		supervisor.cleanUpAfterTask(taskId)

		return hashResult
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
		val startTime = LocalDateTime.now()

		for (type in req.hashTypes)
		{
			state.currentHash = type
			hashes[type] =
				if (file.isFile)
					mapOf(file.name to hasher.calculateHashOfFile(file, type, state, startTime))
				else
					hasher.calculateHashOfFiles(filesInDirectory(file), file.toURI(), type, state, startTime)
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