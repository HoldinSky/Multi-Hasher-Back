package hashing.models

import hashing.common.HashType

data class HashRequest(
	val hashId: Long,
	val path: String,
	val hashTypes: List<HashType>,
                      )