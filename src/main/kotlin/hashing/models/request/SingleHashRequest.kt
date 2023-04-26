package hashing.models.request

import hashing.common.HashType

class SingleHashRequest(
	val hashId: Long,
	val path: String,
	val hashType: HashType,
                       )