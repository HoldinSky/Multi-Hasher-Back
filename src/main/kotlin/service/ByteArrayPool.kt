package service

class ByteArrayPool(poolSize: Int = 5, arraySize: Int = 2097152) :
	ObjectPool<ByteArray>(poolSize) {

	init {
		for (i in 1..poolSize)
			populate(ByteArray(arraySize))
	}

}