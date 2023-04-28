package hashing.logic.hashing

import hashing.models.HashType
import hashing.models.task.TaskState
import service.ByteArrayPool
import java.io.File

interface IHasher
{
	fun calculateHash(input: String, type: HashType): String
	fun calculateHashOfFile(file: File, types: List<HashType>, taskState: TaskState, arrayPool: ByteArrayPool): Map<HashType, String>
	fun calculateHashOfFiles(files: List<File>, types: List<HashType>, taskState: TaskState, arrayPool: ByteArrayPool): Map<HashType, Map<String, String>>
}