package com.app.brfaceresponder.model

data class Rule(
    val id: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(), // Simple unique ID
    val keyword: String,
    val replyMessage: String,
    val isRegex: Boolean = false,
    val priority: Int = 0,
    val delaySeconds: Int = 0
)
