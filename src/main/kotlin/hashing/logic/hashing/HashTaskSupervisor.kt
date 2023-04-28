package hashing.logic.hashing

import hashing.models.result.HashResult
import hashing.models.result.getEmptyHashResult
import hashing.models.task.*
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.roundToLong

class HashTaskSupervisor : ITaskSupervisor {
	private val currentTasks = mutableMapOf<Long, HashTask>()
	private val results = mutableMapOf<Long, HashResult>()
	private val jobsInProgress = mutableMapOf<Long, Job>()

	override fun getTask(taskId: Long): HashTask = currentTasks[taskId] ?: EMPTY_TASK

	override fun getAllTasks(): List<HashTask> {
		currentTasks.values.forEach {
			it.state.speed = updateDataAndCalculateSpeed(it.state)
		}

		return currentTasks.values.toList()
	}

	override fun addNewTask(task: HashTask) {
		currentTasks[task.taskId] = task
	}

	override fun updateStatusOfTask(taskId: Long, newStatus: TaskStatus) {
		val task = currentTasks[taskId] ?: return
		task.status = newStatus
	}

	override fun startExecutingTask(taskId: Long): Job {
		val task = currentTasks[taskId] ?: throw IllegalArgumentException("$taskId - No task with such id")

		task.status = TaskStatus.IN_PROGRESS

		jobsInProgress[taskId] = CoroutineScope(Dispatchers.IO).launch { results[taskId] = task.executeBlock.invoke() }
		return jobsInProgress[taskId]!!
	}

	override fun stopTask(taskId: Long) {
		jobsInProgress[taskId]?.cancel()
		updateStatusOfTask(taskId, TaskStatus.INTERRUPTED)
	}

	override fun getInfoAboutTask(taskId: Long): HashTask = currentTasks[taskId] ?: EMPTY_TASK

	override fun getResultsOfTask(taskId: Long): HashResult =
		results[taskId] ?: getEmptyHashResult(currentTasks[taskId]!!.hashId)

	override fun removeTask(taskId: Long) {
		currentTasks.remove(taskId)
	}

	override fun removeResultsOfTask(taskId: Long) {
		results.remove(taskId)
	}

	override fun removeJobOfTask(taskId: Long) {
		jobsInProgress.remove(taskId)
	}

	override fun cleanUpAfterTask(taskId: Long) {
		removeResultsOfTask(taskId)
		removeJobOfTask(taskId)
		removeTask(taskId)
	}

	private fun updateDataAndCalculateSpeed(state: TaskState): Long {
		updateProgressState(state)
		val elapsedMillis = max(1, state.delta.elapsed.toMillis())
		return (state.delta.processed * 1000.0 / elapsedMillis).roundToLong()
	}

	private fun updateProgressState(state: TaskState) {
		state.delta.processed = state.bytesProcessed - state.delta.processed
		state.delta.elapsed = Duration.between(state.startTime, LocalDateTime.now()) - state.delta.elapsed
	}
}