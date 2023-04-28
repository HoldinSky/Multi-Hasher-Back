package service

interface IObjectPool<T> {

	fun populate(item: T)
	fun take(): T
	fun giveBack(item: T): IObjectPool<T>

}