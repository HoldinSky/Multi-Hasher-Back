package hashing.models


data class TaskInProgress(val taskId: Long, val path: String, val hashTypes: String, val progress: Int, val speed: Int, val currentHash: String)