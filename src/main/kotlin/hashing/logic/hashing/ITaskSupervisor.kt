package hashing.logic.hashing

import hashing.models.result.HashResult
import hashing.models.task.HashTask
import hashing.models.task.TaskStatus
import kotlinx.coroutines.Job

interface ITaskSupervisor {
	fun getTask(taskId: Long): HashTask
	fun getAllTasks(): List<HashTask>
	fun addNewTask(task: HashTask)
	fun removeTask(taskId: Long)

	fun updateStatusOfTask(taskId: Long, newStatus: TaskStatus)

	fun startExecutingTask(taskId: Long): Job
	fun stopTask(taskId: Long)

	fun getInfoAboutTask(taskId: Long): HashTask
	fun getResultsOfTask(taskId: Long): HashResult

	fun removeResultsOfTask(taskId: Long)
	fun removeJobOfTask(taskId: Long)
	fun cleanUpAfterTask(taskId: Long)
}
