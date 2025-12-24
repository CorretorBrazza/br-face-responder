package com.app.brfaceresponder.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.app.brfaceresponder.data.RuleRepository
import com.app.brfaceresponder.data.SharedPrefs

class AutoReplyService : NotificationListenerService() {

    companion object {
        private const val TAG = "AutoReplyService"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val repliedNotifications = mutableSetOf<String>() // Evita respostas duplicadas

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService conectado")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) {
            Log.d(TAG, "Notificação nula recebida")
            return
        }

        // 1. Verificar se o serviço está habilitado
        if (!SharedPrefs.isServiceEnabled(this)) {
            Log.d(TAG, "Serviço desabilitado")
            return
        }

        val packageName = sbn.packageName
        val notificationKey = sbn.key

        // 2. Verificar se o app está na lista de apps habilitados
        val enabledApps = SharedPrefs.getEnabledAppPackages(this)
        if (!enabledApps.contains(packageName)) {
            Log.d(TAG, "App $packageName não está habilitado")
            return
        }

        // 3. Evitar responder à mesma notificação múltiplas vezes
        if (repliedNotifications.contains(notificationKey)) {
            Log.d(TAG, "Já respondeu a esta notificação: $notificationKey")
            return
        }

        val notification = sbn.notification
        val extras = notification.extras
        val notificationText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        if (notificationText.isNullOrEmpty()) {
            Log.d(TAG, "Texto da notificação vazio")
            return
        }

        Log.d(TAG, "Mensagem recebida de $packageName: $notificationText")

        // 4. Verificar se há uma regra correspondente
        val matchingRule = RuleRepository.findRuleForMessage(this, notificationText)

        if (matchingRule != null) {
            Log.d(TAG, "Regra encontrada: ${matchingRule.keyword} -> ${matchingRule.replyMessage}")
            
            // 5. Encontrar a ação de resposta
            val replyAction = findReplyAction(notification)
            
            if (replyAction != null) {
                repliedNotifications.add(notificationKey) // Marcar como respondida
                
                if (matchingRule.delaySeconds > 0) {
                    Log.d(TAG, "Aguardando ${matchingRule.delaySeconds}s antes de responder")
                    handler.postDelayed({
                        sendReply(replyAction, matchingRule.replyMessage, notificationKey)
                    }, matchingRule.delaySeconds * 1000L)
                } else {
                    sendReply(replyAction, matchingRule.replyMessage, notificationKey)
                }
            } else {
                Log.w(TAG, "Nenhuma ação de resposta encontrada na notificação")
            }
        } else {
            Log.d(TAG, "Nenhuma regra corresponde à mensagem")
        }
    }

    private fun findReplyAction(notification: Notification): Notification.Action? {
        // CORREÇÃO: Verificar se actions não é null
        val actions = notification.actions ?: return null
        
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                // Verificação mais flexível para diferentes apps
                val resultKey = remoteInput.resultKey?.lowercase() ?: continue
                if (resultKey.contains("text") || 
                    resultKey.contains("reply") || 
                    resultKey.contains("input")) {
                    return action
                }
            }
        }
        return null
    }

    private fun sendReply(action: Notification.Action, message: String, notificationKey: String) {
        val intent = Intent()
        val bundle = Bundle()
        
        action.remoteInputs?.forEach { remoteInput ->
            bundle.putCharSequence(remoteInput.resultKey, message)
        }
        
        RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
        
        try {
            action.actionIntent.send(applicationContext, 0, intent)
            Log.d(TAG, "Resposta enviada com sucesso: $message")
        } catch (e: PendingIntent.CanceledException) {
            Log.e(TAG, "Erro ao enviar resposta: ${e.message}")
            repliedNotifications.remove(notificationKey) // Permite nova tentativa
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado: ${e.message}")
            repliedNotifications.remove(notificationKey)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Limpar notificação da lista ao ser removida
        sbn?.key?.let { repliedNotifications.remove(it) }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListenerService desconectado")
        repliedNotifications.clear()
    }
}
