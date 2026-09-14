package com.velocity.kmpwinget.domain.model

sealed interface OperationResult {
    data object Idle : OperationResult

    data class Loading(
        val title: String = "Processing...",
        val message: String = "Please wait",
        val currentProgress: Float? = null,
        val step: Int = 0,
        val totalSteps: Int = 0,
        val logOutput: String = ""
    ) : OperationResult

    data class Success(
        val message: String,
        val details: String? = null,
        val logOutput: String = ""
    ) : OperationResult

    data class Error(
        val message: String,
        val details: String? = null,
        val logOutput: String = ""
    ) : OperationResult
}
