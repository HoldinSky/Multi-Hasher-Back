package hashing.logic

import hashing.common.HashType
import hashing.common.filesInDirectory
import hashing.models.task.TaskState
import kotlinx.coroutines.runBlocking

import java.io.File
import java.net.URI
import java.security.MessageDigest


class MultiHasher : IHasher
{
	override fun calculateHash(input: String, type: HashType): String {
		val digest = MessageDigest.getInstance(type.representation)
		return checkSum(input, digest)
	}

	override fun calculateHashOfFile(file: File, type: HashType, taskState: TaskState): String
	{
		val digest = MessageDigest.getInstance(type.representation)
		return runBlocking { checkSumAsync(file, digest, taskState) }
	}

	override fun calculateHashOfFiles(files: List<File>, baseDirURI: URI, type: HashType, taskState: TaskState): Map<String, String> {
		val hashes = mutableMapOf<String, String>()

		for (file in files) {
			val hash = calculateHashOfFile(file, type, taskState)
			hashes[file.name] = hash
		}

		return hashes
	}

	override fun calculateHashOfDirectory(directory: File, type: HashType, taskState: TaskState): String
	{
		val digest = MessageDigest.getInstance(type.representation)
		val files = filesInDirectory(directory)

		return checkSum(files, digest, taskState)
	}
}