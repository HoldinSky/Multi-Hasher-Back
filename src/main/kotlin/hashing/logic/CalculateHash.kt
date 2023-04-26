package hashing.logic

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

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal suspend fun checkSumAsync(
	file: File,
	messageDigest: MessageDigest,
	taskState: TaskState,
                                  ): String
{

	val fis = withContext(Dispatchers.IO) {
		FileInputStream(file)
	}

	val byteArray = ByteArray(2048)
	var bytesCount: Int

	val blocks = GlobalScope.produce(Dispatchers.IO, capacity = 200) {
		while (fis.read(byteArray).also { bytesCount = it } != -1)
			send(Pair(byteArray.copyOf(), bytesCount))

		fis.close()
	}

	runBlocking {
		blocks.consumeEach { (array, count) ->
			messageDigest.update(array, 0, count)
			taskState.bytesProcessed += count
		}
	}

	val bytes: ByteArray = messageDigest.digest()

	val sb = StringBuilder()

	for (i in bytes.indices)
		sb.append(
			((bytes[i].toInt() and 0xff) + 0x100).toString(16)
				.substring(1)
		         )

	return sb.toString()
}

internal fun checkSum(file: File, messageDigest: MessageDigest, taskState: TaskState): String
{
	val fis = FileInputStream(file)

	val byteArray = ByteArray(1024)
	var bytesCount: Int

	while (fis.read(byteArray).also { bytesCount = it } != -1)
	{
		messageDigest.update(byteArray, 0, bytesCount)
		taskState.bytesProcessed += bytesCount
	}

	fis.close()

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
	files: List<File>,
	messageDigest: MessageDigest,
	taskState: TaskState,
                     ): String
{
	for (file in files)
	{
		val fis = FileInputStream(file)

		val byteArray = ByteArray(1024)
		var bytesCount: Int

		while (fis.read(byteArray).also { bytesCount = it } != -1)
		{
			messageDigest.update(byteArray, 0, bytesCount)
			taskState.bytesProcessed += bytesCount.toLong()
		}

		fis.close()
	}

	val bytes: ByteArray = messageDigest.digest()

	val sb = StringBuilder()

	for (i in bytes.indices)
		sb.append(
			((bytes[i].toInt() and 0xff) + 0x100).toString(16)
				.substring(1)
		         )

	return sb.toString()
}
