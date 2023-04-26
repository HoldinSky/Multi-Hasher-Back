package hashing.logic

import hashing.common.HashType
import hashing.models.task.TaskState
import java.io.File
import java.net.URI

interface IHasher
{
	fun calculateHash(input: String, type: HashType): String
	fun calculateHashOfFile(file: File, type: HashType, taskState: TaskState): String
	fun calculateHashOfFiles(files: List<File>, baseDirURI: URI, type: HashType, taskState: TaskState): Map<String, String>
	fun calculateHashOfDirectory(directory: File, type: HashType, taskState: TaskState): String
}