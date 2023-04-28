package hashing.models.task

import java.time.Duration
import java.time.LocalDateTime

class DeltaState {
	var processed = 0L
	var elapsed: Duration = Duration.ZERO
}

data class TaskState(
	var bytesProcessed: Long,
	var totalBytes: Long,
	var speed: Long,
	val startTime: LocalDateTime) {

	val delta = DeltaState()
}

fun getInitialState(): TaskState = TaskState(0, 0, 0, LocalDateTime.now())