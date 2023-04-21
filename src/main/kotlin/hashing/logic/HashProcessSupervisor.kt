package hashing.logic

import hashing.common.TaskStatus
import hashing.models.*
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import kotlin.math.roundToInt

class HashProcessSupervisor : IProcessSupervisor
{
	private val currentTasks = mutableMapOf<Long, HashTask>()
	private val results = mutableMapOf<Long, HashResult>()
	private val jobsInProgress = mutableMapOf<Long, Job>()

	override fun getTask(taskId: Long): HashTask = currentTasks[taskId] ?: EMPTY_TASK

	override fun getAllTasks(): List<HashTask> = currentTasks.values.toList()

	override fun addNewTask(task: HashTask)
	{
		currentTasks[task.taskId] = task
	}

	override fun updateStatusOfTask(taskId: Long, newStatus: TaskStatus)
	{
		val process = currentTasks[taskId] ?: return
		process.status = newStatus
	}

	override fun calculateAndGetProgressOfTask(state: TaskState): Int
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

	override fun getResultsOfTask(taskId: Long): HashResult = results[taskId] ?: getEmptyHashResult(currentTasks[taskId]!!.resId)

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

}