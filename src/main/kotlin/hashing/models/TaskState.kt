package hashing.models

import hashing.common.HashType

data class TaskState(var bytesProcessed: Long, var totalBytes: Long, var speed: Long, var currentHash: HashType, val numberOfHashTypes: Byte)