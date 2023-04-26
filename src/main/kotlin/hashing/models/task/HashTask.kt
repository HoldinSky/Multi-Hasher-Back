package hashing.models.task

import hashing.models.result.HashResult
import hashing.models.result.getEmptyHashResult
import hashing.models.request.HashRequestProps
import java.time.LocalDateTime

class HashTask(
	val taskId: Long,
	val hashId: Long,
	val path: String,
	val hashTypes: String,
	var status: TaskStatus,
	val state: TaskState,
	val executeBlock: () -> HashResult
              )
{
	constructor(props: HashRequestProps, status: TaskStatus, state: TaskState, executeBlock: () -> HashResult):
		this(props.taskId, props.hashId, props.contentPath, props.hashTypesInString, status, state, executeBlock)

	override fun toString(): String
	{
		return "{TaskID: '$taskId' HashId: '$hashId', File/Dir: '$path', Hash types: '$hashTypes' Status: '$status'}"
	}

}

val EMPTY_TASK = HashTask(0, 0, "", "", TaskStatus.UNDEFINED, TaskState(0, 0, 0, LocalDateTime.now(), 0)) { getEmptyHashResult(0) }