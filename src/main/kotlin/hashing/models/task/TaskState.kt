package hashing.models.task

import java.time.LocalDateTime

data class TaskState(
	var bytesProcessed: Long,
	var totalBytes: Long,
	var speed: Long,
	val startTime: LocalDateTime,
	val numberOfHashTypes: Byte)

fun getInitialState(numberOfHashes: Byte): TaskState = TaskState(0, 0, 0, LocalDateTime.now(), numberOfHashes)