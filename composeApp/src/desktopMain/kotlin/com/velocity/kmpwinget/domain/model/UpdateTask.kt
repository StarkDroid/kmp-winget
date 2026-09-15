package com.velocity.kmpwinget.domain.model

enum class TaskStatus {
    QUEUED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class UpdateTask(
    val pkg: Package,
    val status: TaskStatus = TaskStatus.QUEUED,
    val progress: Float = 0f,
    val message: String = "Queued",
    val logs: String = "",
    val errorMessage: String? = null
)

data class BackgroundQueueState(
    val tasks: List<UpdateTask> = emptyList(),
    val isRunning: Boolean = false,
    val isExpanded: Boolean = false
) {
    val totalCount: Int get() = tasks.size
    val completedCount: Int get() = tasks.count { it.status == TaskStatus.COMPLETED }
    val failedCount: Int get() = tasks.count { it.status == TaskStatus.FAILED }
    val inProgressTask: UpdateTask? get() = tasks.firstOrNull { it.status == TaskStatus.IN_PROGRESS }
    val overallProgress: Float
        get() = if (tasks.isEmpty()) 0f else (completedCount.toFloat() / tasks.size.toFloat())
}
