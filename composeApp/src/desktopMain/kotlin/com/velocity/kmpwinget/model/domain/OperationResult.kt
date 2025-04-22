package com.velocity.kmpwinget.model.domain

/**
 * Represents the result of an async operation
 */
sealed class OperationResult {
    object Loading : OperationResult()
    data class Success(val message: String) : OperationResult()
    data class Error(val message: String) : OperationResult()
}
