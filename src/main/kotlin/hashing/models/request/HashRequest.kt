package hashing.models.request

import hashing.models.HashType

data class HashRequest(
	val hashId: Long,
	val path: String,
	val hashTypes: List<HashType>,
                      ) {
	constructor(props: HashRequestProps) : this(props.hashId, props.contentPath, props.hashTypes)
}