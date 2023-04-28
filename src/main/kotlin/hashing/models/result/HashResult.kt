package hashing.models.result

import hashing.models.HashType

data class HashResult(
	val resultId: Long,
	val hasError: Boolean,
	val size: Long,
	val path: String,
	val hashTypes: String,
	val hashes: Map<HashType, Map<String, String>>?,
	val error: String?
                     )

fun getErrorResult(resultId: Long, path: String, hashTypeList: List<HashType>, errorMessage: String?): HashResult =
	HashResult(
		resultId,
		true,
		0L,
		path,
		hashTypeList.joinToString(", ") { it.representation },
		emptyMap(),
		errorMessage
	          )

fun getEmptyHashResult(resultId: Long): HashResult =
	HashResult(
		resultId,
		true,
		0L,
		"",
		"",
		emptyMap(),
		""
	          )