package hashing.models.request

import hashing.common.HashType

data class HashRequestProps(val taskId: Long, val hashId: Long, val contentPath: String, val hashTypes: List<HashType>) {
	val hashTypesInString: String = hashTypes.joinToString(", ") { it.representation }
}