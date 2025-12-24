package com.app.brfaceresponder.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.brfaceresponder.data.RuleRepository
import com.app.brfaceresponder.model.Rule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RuleViewModel(application: Application) : AndroidViewModel(application) {

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        viewModelScope.launch {
            try {
                val loadedRules = withContext(Dispatchers.IO) {
                    RuleRepository.getRules(getApplication())
                }
                _rules.value = loadedRules
            } catch (e: Exception) {
                _error.value = "Erro ao carregar regras: ${e.message}"
            }
        }
    }

    fun addRule(keyword: String, replyMessage: String, isRegex: Boolean, priority: Int, delaySeconds: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val newRule = Rule(
                    keyword = keyword.trim(),
                    replyMessage = replyMessage.trim(),
                    isRegex = isRegex,
                    priority = priority,
                    delaySeconds = delaySeconds
                )
                
                // Validar regex antes de salvar
                if (!newRule.isValidRegex()) {
                    _error.value = "Expressão regular inválida"
                    return@launch
                }
                
                withContext(Dispatchers.IO) {
                    RuleRepository.addRule(getApplication(), newRule)
                }
                loadRules()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Erro ao adicionar regra: ${e.message}"
            }
        }
    }

    fun deleteRule(rule: Rule) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RuleRepository.deleteRule(getApplication(), rule)
                }
                loadRules()
            } catch (e: Exception) {
                _error.value = "Erro ao excluir regra: ${e.message}"
            }
        }
    }

    fun increaseRulePriority(rule: Rule) {
        viewModelScope.launch {
            if (rule.priority > 0) {
                try {
                    val updatedRule = rule.copy(priority = rule.priority - 1)
                    withContext(Dispatchers.IO) {
                        RuleRepository.updateRule(getApplication(), updatedRule)
                    }
                    loadRules()
                } catch (e: Exception) {
                    _error.value = "Erro ao atualizar prioridade: ${e.message}"
                }
            }
        }
    }

    fun decreaseRulePriority(rule: Rule) {
        viewModelScope.launch {
            try {
                val updatedRule = rule.copy(priority = rule.priority + 1)
                withContext(Dispatchers.IO) {
                    RuleRepository.updateRule(getApplication(), updatedRule)
                }
                loadRules()
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar prioridade: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}