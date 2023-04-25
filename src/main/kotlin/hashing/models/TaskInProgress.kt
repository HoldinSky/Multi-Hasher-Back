package hashing.models

import hashing.common.TaskStatus


data class TaskInProgress(
	val taskId: Long,
	val path: String,
	val hashTypes: String,
	val progress: Int,
	val speed: Int,
	val currentHash: String,
	val status: TaskStatus
                         )