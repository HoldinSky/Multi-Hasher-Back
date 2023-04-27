package hashing.logic.hashing

import hashing.models.task.TaskState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest


internal fun checkSum(input: String, messageDigest: MessageDigest): String
{
	messageDigest.update(input.toByteArray())

	val bytes: ByteArray = messageDigest.digest()

	val sb = StringBuilder()

	for (i in bytes.indices)
		sb.append(
			((bytes[i].toInt() and 0xff) + 0x100).toString(16)
				.substring(1)
		         )

	return sb.toString()
}

internal fun checkSum(
	file: File,
	digestList: List<MessageDigest>,
	taskState: TaskState,
                             ): List<String>
{
	updateDigestListWithDataFromFile(file, digestList, taskState)
	return parseDigestListToStringList(digestList)
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
private fun updateDigestListWithDataFromFile(file: File, digestList: List<MessageDigest>, state: TaskState)
{
	val byteArray = ByteArray(8192)
	var bytesCount: Int

	runBlocking {
		GlobalScope.produce(Dispatchers.IO, 100) {
			val fis = FileInputStream(file)

			while (fis.read(byteArray).also { bytesCount = it } != -1)
				send(Pair(byteArray.clone(), bytesCount))

			fis.close()
		}.consumeEach { (array, count) ->
			digestList.stream().parallel().forEach { digest -> digest.update(array, 0, count) }
			state.bytesProcessed += count
		}
	}
}

private fun parseDigestListToStringList(digestList: List<MessageDigest>): List<String>
{
	val bytesList = digestList.map { it.digest() }
	val hashes = mutableListOf<String>()

	bytesList.forEach { array ->
		val sb = StringBuilder()
		for (i in array.indices)
			sb.append(
				((array[i].toInt() and 0xff) + 0x100).toString(16)
					.substring(1)
			         )
		hashes.add(sb.toString())
	}

	return hashes
}