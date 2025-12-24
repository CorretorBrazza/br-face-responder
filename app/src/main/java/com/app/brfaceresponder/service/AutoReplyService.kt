package com.app.brfaceresponder.service

import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.app.brfaceresponder.data.RuleRepository
import com.app.brfaceresponder.data.SharedPrefs

class AutoReplyService : NotificationListenerService() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        // 1. Check if service is enabled
        if (!SharedPrefs.isServiceEnabled(this)) {
            return
        }

        if (sbn == null) return

        // For simplicity, we can target a specific app like WhatsApp, but this can be expanded.
        val packageName = sbn.packageName
        val enabledApps = SharedPrefs.getEnabledAppPackages(this)
        if (!enabledApps.contains(packageName)) {
            return
        }

        val notification = sbn.notification
        val extras = notification.extras
        val notificationText = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()

        if (notificationText.isNullOrEmpty()) {
            return
        }

        // 4. Check the message against our rules
        val matchingRule = RuleRepository.findRuleForMessage(this, notificationText)

        if (matchingRule != null) {
            // 5. If a rule matches, find the reply action and send the response.
            val replyAction = findReplyAction(notification)
            if (replyAction != null) {
                if (matchingRule.delaySeconds > 0) {
                    handler.postDelayed({
                        sendReply(replyAction, matchingRule.replyMessage)
                    }, matchingRule.delaySeconds * 1000L) // Convert seconds to milliseconds
                } else {
                    sendReply(replyAction, matchingRule.replyMessage)
                }
            }
        }
    }

    private fun findReplyAction(notification: android.app.Notification): android.app.Notification.Action? {
        for (action in notification.actions) {
            if (action.remoteInputs != null) {
                for (remoteInput in action.remoteInputs) {
                    if (remoteInput.resultKey.equals("text", ignoreCase = true)) {
                        return action
                    }
                }
            }
        }
        return null
    }

    private fun sendReply(action: android.app.Notification.Action, message: String) {
        val intent = Intent()
        val bundle = Bundle()
        action.remoteInputs.forEach { remoteInput ->
            bundle.putCharSequence(remoteInput.resultKey, message)
        }
        RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
        try {
            action.actionIntent.send(applicationContext, 0, intent)
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
    }
}
