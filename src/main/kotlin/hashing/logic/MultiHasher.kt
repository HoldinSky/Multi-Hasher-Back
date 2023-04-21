package hashing.logic

import hashing.common.HashType
import hashing.common.filesInDirectory
import hashing.models.TaskState

import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.time.LocalDateTime


class MultiHasher : IHasher
{
	override fun calculateHash(input: String, type: HashType): String {
		val digest = MessageDigest.getInstance(type.representation)
		return checkSum(input, digest)
	}

	override fun calculateHashOfFile(file: File, type: HashType, taskState: TaskState, startTime: LocalDateTime): String
	{
		val digest = MessageDigest.getInstance(type.representation)
		return checkSum(file, digest, taskState, startTime)
	}

	override fun calculateHashOfFiles(files: List<File>, baseDirURI: URI, type: HashType, taskState: TaskState, startTime: LocalDateTime): Map<String, String> {
		val hashes = mutableMapOf<String, String>()

		for (file in files) {
			val hash = calculateHashOfFile(file, type, taskState, startTime)
			hashes[file.name] = hash
		}

		return hashes
	}

	override fun calculateHashOfDirectory(directory: File, type: HashType, taskState: TaskState): String
	{
		val digest = MessageDigest.getInstance(type.representation)
		val files = filesInDirectory(directory)

		return checkSum(files, digest, taskState, LocalDateTime.now())
	}
}