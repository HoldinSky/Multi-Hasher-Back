package hashing.logic.hashing

import hashing.models.result.HashResult
import hashing.models.result.getEmptyHashResult
import hashing.models.task.*
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class HashProcessSupervisor : IProcessSupervisor
{
	private val currentTasks = mutableMapOf<Long, HashTask>()
	private val results = mutableMapOf<Long, HashResult>()
	private val jobsInProgress = mutableMapOf<Long, Job>()

	override fun getTask(taskId: Long): HashTask = currentTasks[taskId] ?: EMPTY_TASK

	override fun getAllTasks(): List<HashTask>
	{
		val now = LocalDateTime.now()
		currentTasks.values.forEach {
			it.state.speed = calculateSpeed(it.state.bytesProcessed, Duration.between(it.state.startTime, now))
		}

		return currentTasks.values.toList()
	}

	override fun addNewTask(task: HashTask)
	{
		currentTasks[task.taskId] = task
	}

	override fun updateStatusOfTask(taskId: Long, newStatus: TaskStatus)
	{
		val task = currentTasks[taskId] ?: return
		task.status = newStatus
	}

	override fun calculateProgressOfTask(state: TaskState): Int
	{
		if (state.totalBytes == 0L) return 0
		return (state.bytesProcessed.toFloat() * 100 / state.totalBytes.toFloat()).roundToInt()
	}

	override fun startExecutingTask(taskId: Long): Job
	{
		val task = currentTasks[taskId] ?: throw IllegalArgumentException("$taskId - No task with such id")

		task.status = TaskStatus.IN_PROGRESS

		jobsInProgress[taskId] = CoroutineScope(Dispatchers.IO).launch { results[taskId] = task.executeBlock.invoke() }
		return jobsInProgress[taskId]!!
	}

	override fun stopTask(taskId: Long)
	{
		jobsInProgress[taskId]?.cancel()
		updateStatusOfTask(taskId, TaskStatus.INTERRUPTED)
	}

	override fun getInfoAboutTask(taskId: Long): HashTask = currentTasks[taskId] ?: EMPTY_TASK

	override fun getResultsOfTask(taskId: Long): HashResult =
		results[taskId] ?: getEmptyHashResult(currentTasks[taskId]!!.hashId)

	override fun removeTask(taskId: Long)
	{
		currentTasks.remove(taskId)
	}

	override fun removeResultsOfTask(taskId: Long)
	{
		results.remove(taskId)
	}

	override fun removeJobOfTask(taskId: Long)
	{
		jobsInProgress.remove(taskId)
	}

	override fun cleanUpAfterTask(taskId: Long)
	{
		removeResultsOfTask(taskId)
		removeJobOfTask(taskId)
		removeTask(taskId)
	}

	private fun calculateSpeed(processedBytes: Long, elapsed: Duration): Long
	{
		val elapsedMillis = max(1, elapsed.toMillis())
		return (processedBytes * 1000.0 / elapsedMillis).roundToLong()
	}
}