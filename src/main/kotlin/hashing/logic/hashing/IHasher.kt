package hashing.logic.hashing

import hashing.common.HashType
import hashing.models.task.TaskState
import java.io.File
import java.net.URI

interface IHasher
{
	fun calculateHash(input: String, type: HashType): String
	suspend fun calculateHashOfFile(file: File, types: List<HashType>, taskState: TaskState): Map<HashType, String>
	suspend fun calculateHashOfFiles(files: List<File>, types: List<HashType>, taskState: TaskState): Map<HashType, Map<String, String>>
}