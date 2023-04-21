package hashing.logic

import java.io.File

class SizeCalculator
{
	fun calculateSizeForDirectInput(input: String): Long = input.toByteArray().size.toLong()

	fun calculateSizeForFile(file: File): Long = file.length()

	fun calculateSizeForDirectory(directory: File): Long {
		var size = 0L

		directory.walk().forEach {
			if (it.isFile)
				size += it.length()
		}
		return size
	}
}