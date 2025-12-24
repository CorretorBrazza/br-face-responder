package com.app.brfaceresponder.model

import java.util.UUID

data class Rule(
    val id: String = UUID.randomUUID().toString(), // UUID único e confiável
    val keyword: String,
    val replyMessage: String,
    val isRegex: Boolean = false,
    val priority: Int = 0,
    val delaySeconds: Int = 0
) {
    // Validar regex antes de usar
    fun isValidRegex(): Boolean {
        return if (isRegex) {
            try {
                Regex(keyword)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }
    }
}
