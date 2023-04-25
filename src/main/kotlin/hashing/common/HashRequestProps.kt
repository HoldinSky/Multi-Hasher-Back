package hashing.common

data class HashRequestProps(val taskId: Long, val hashId: Long, val contentPath: String, val hashTypes: List<HashType>) {
	val hashTypesInString: String = hashTypes.joinToString(", ") { it.representation }
}