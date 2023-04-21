package hashing.logic

import hashing.common.TaskStatus
import hashing.models.HashTask
import hashing.models.HashResult
import hashing.models.TaskState
import kotlinx.coroutines.Job

interface IProcessSupervisor
{
	fun getTask(taskId: Long): HashTask
	fun getAllTasks(): List<HashTask>
	fun addNewTask(task: HashTask)
	fun removeTask(taskId: Long)

	fun updateStatusOfTask(taskId: Long, newStatus: TaskStatus)
	fun calculateAndGetProgressOfTask(state: TaskState): Int

	fun startExecutingTask(taskId: Long): Job
	fun stopTask(taskId: Long)

	fun getInfoAboutTask(taskId: Long): HashTask
	fun getResultsOfTask(taskId: Long): HashResult

	fun removeResultsOfTask(taskId: Long)
	fun removeJobOfTask(taskId: Long)
	fun cleanUpAfterTask(taskId: Long)
}
