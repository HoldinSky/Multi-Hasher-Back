package hashing.common

enum class HashType(val representation: String) {
	MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256"), NONE("NONE"), PREPARING("PREPARING");
}