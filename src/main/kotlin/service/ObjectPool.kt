package service

import java.util.*
import kotlin.collections.HashSet

sealed class ObjectPool<T>(private val size: Int = 5) : IObjectPool<T> {

	// Object pool stack. last in first out
	private val stackPool: Stack<T> = Stack()

	// The hashCode collection of the loaned object
	private val borrowHashCodeSet: HashSet<Int> = HashSet()

	@Synchronized
	override fun populate(item: T) {
		if (stackPool.size + borrowHashCodeSet.size == size) {
			throw RuntimeException("The object in the pool has reached the maximum value")
		}
		stackPool.add(item)
	}

	@Synchronized
	override fun take(): T {
		if (stackPool.isEmpty()) {
			throw RuntimeException("There are no object that can be lent")
		}
		val pop: T = stackPool.pop()
		borrowHashCodeSet.add(pop.hashCode())

		return pop
	}

	@Synchronized
	override fun giveBack(item: T): IObjectPool<T> {
		if (borrowHashCodeSet.contains(item.hashCode())) {
			stackPool.add(item)
			borrowHashCodeSet.remove(item.hashCode())
			return this
		}
		throw RuntimeException("Only objects lent from the pool can be returned")
	}

}