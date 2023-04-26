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

	override suspend fun calculateHashOfFile(file: File, types: List<HashType>, taskState: TaskState): Map<HashType, String>
	{
		val digestList = types.map { MessageDigest.getInstance(it.representation) }.toList()
		val hashes = checkSum(file, digestList, taskState)

		val map = mutableMapOf<HashType, String>()
		for (i in types.indices)
			map[types[i]] = hashes[i]

		return map
	}

	override suspend fun calculateHashOfFiles(files: List<File>, types: List<HashType>, taskState: TaskState): Map<HashType, Map<String, String>>
	{
		val hashes = mutableMapOf<HashType, MutableMap<String, String>>()

		types.forEach { type ->
			val map = mutableMapOf<String, String>()
			for (file in files)
			{
				calculateHashOfFile(file, listOf(type), taskState).forEach {
					map[file.name] = it.value
				}
				hashes[type] = map
			}
		}

		return hashes
	}
}