package hashing.models.task


data class TaskInProgress(
	val taskId: Long,
	val path: String,
	val hashTypes: String,
	val progress: Int,
	val speed: Int,
	val status: TaskStatus
                         )