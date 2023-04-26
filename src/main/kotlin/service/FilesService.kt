package hashing.common

import java.io.File
import java.io.FileNotFoundException

fun filesInDirectory(directory: File): List<File>
{
	val list = mutableListOf<File>()

	directory.walk().forEach {
		if (it.isFile) list.add(it)
	}

	if (list.size == 0)
		throw FileNotFoundException("$directory (No such file or directory)")

	return list
}