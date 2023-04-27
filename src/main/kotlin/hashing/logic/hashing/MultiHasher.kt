package hashing.logic.hashing

import hashing.common.HashType
import hashing.models.task.TaskState

import java.io.File
import java.security.MessageDigest


class MultiHasher : IHasher
{
	override fun calculateHash(input: String, type: HashType): String
	{
		val digest = MessageDigest.getInstance(type.representation)
		return checkSum(input, digest)
	}

	override fun calculateHashOfFile(
		file: File,
		types: List<HashType>,
		taskState: TaskState
	                                        ): Map<HashType, String>
	{
		val digestList = types.map { MessageDigest.getInstance(it.representation) }.toList()
		val hashes = checkSum(file, digestList, taskState)

		val map = mutableMapOf<HashType, String>()
		for (i in types.indices)
			map[types[i]] = hashes[i]

		return map
	}

	override fun calculateHashOfFiles(
		files: List<File>,
		types: List<HashType>,
		taskState: TaskState
	                                         ): Map<HashType, Map<String, String>>
	{
		val hashes = mutableMapOf<HashType, MutableMap<String, String>>()

		types.forEach {
			hashes[it] = mutableMapOf()
		}

		files.forEach {file ->
			calculateHashOfFile(file, types, taskState).forEach { (type, hash) ->
				hashes[type]?.set(file.name, hash)
			}
		}

		return hashes
	}
}