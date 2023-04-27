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

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun checkSum(
	file: File,
	digestList: List<MessageDigest>,
	taskState: TaskState,
                             ): List<String>
{
	val byteArray = ByteArray(8192)
	var bytesCount: Int

	runBlocking {
		produce(Dispatchers.IO, 100) {
			val fis = FileInputStream(file)

			while (fis.read(byteArray).also { bytesCount = it } != -1)
				send(Pair(byteArray.clone(), bytesCount))

			fis.close()
		}.consumeEach { (array, count) ->
			digestList.forEach { digest ->
				coroutineScope {
					digest.update(array, 0, count)
				}
			}
			taskState.bytesProcessed += count
		}
	}

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