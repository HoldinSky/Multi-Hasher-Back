package hashing.logic

import hashing.models.TaskState
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.roundToLong


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

internal fun checkSum(file: File, messageDigest: MessageDigest, taskState: TaskState, startTime: LocalDateTime): String
{
	val fis = FileInputStream(file)

	val byteArray = ByteArray(1024)
	var bytesCount: Int

	while (fis.read(byteArray).also { bytesCount = it } != -1)
	{
		messageDigest.update(byteArray, 0, bytesCount)
		taskState.bytesProcessed += bytesCount.toLong()
		taskState.speed = calculateSpeed(taskState.bytesProcessed, Duration.between(startTime, LocalDateTime.now()))
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

internal fun checkSum(files: List<File>, messageDigest: MessageDigest, taskState: TaskState, startTime: LocalDateTime): String {
	for (file in files) {
		val fis = FileInputStream(file)

		val byteArray = ByteArray(1024)
		var bytesCount: Int

		while (fis.read(byteArray).also { bytesCount = it } != -1)
		{
			messageDigest.update(byteArray, 0, bytesCount)
			taskState.bytesProcessed += bytesCount.toLong()
			taskState.speed = calculateSpeed(taskState.bytesProcessed, Duration.between(startTime, LocalDateTime.now()))
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

private fun calculateSpeed(processedBytes: Long, elapsed: Duration): Long {
	val elapsedMillis = max(1, elapsed.toMillis())
	return (processedBytes * 1000.0 / elapsedMillis).roundToLong()
}