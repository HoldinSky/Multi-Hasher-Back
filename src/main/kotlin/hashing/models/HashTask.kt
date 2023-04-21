package hashing.models

import hashing.common.HashType
import hashing.common.TaskStatus

class HashTask(
	val taskId: Long,
	val resId: Long,
	val path: String,
	val hashTypes: String,
	var status: TaskStatus,
	val state: TaskState,
	val executeBlock: () -> HashResult
              )
{

	override fun toString(): String
	{
		return "{TaskID: '$taskId' ResultId: '$resId', File/Dir: '$path', Hash types: '$hashTypes' Status: '$status'}"
	}

}

val EMPTY_TASK = HashTask(0, 0, "", "", TaskStatus.UNDEFINED, TaskState(0, 0, 0, HashType.NONE, 0)) { getEmptyHashResult(0) }