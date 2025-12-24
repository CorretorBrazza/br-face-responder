package com.app.brfaceresponder.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.brfaceresponder.data.RuleRepository
import com.app.brfaceresponder.model.Rule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RuleViewModel(application: Application) : AndroidViewModel(application) {

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        _rules.value = RuleRepository.getRules(getApplication())
    }

    fun addRule(keyword: String, replyMessage: String, isRegex: Boolean, priority: Int, delaySeconds: Int) {
        val newRule = Rule(keyword = keyword, replyMessage = replyMessage, isRegex = isRegex, priority = priority, delaySeconds = delaySeconds)
        RuleRepository.addRule(getApplication(), newRule)
        loadRules() // Reload rules from the source of truth
    }

    fun deleteRule(rule: Rule) {
        RuleRepository.deleteRule(getApplication(), rule)
        loadRules() // Reload rules from the source of truth
    }

    fun increaseRulePriority(rule: Rule) {
        // To prevent priority going below 0
        if (rule.priority > 0) {
            val updatedRule = rule.copy(priority = rule.priority - 1)
            RuleRepository.updateRule(getApplication(), updatedRule)
            loadRules()
        }
    }

    fun decreaseRulePriority(rule: Rule) {
        val updatedRule = rule.copy(priority = rule.priority + 1)
        RuleRepository.updateRule(getApplication(), updatedRule)
        loadRules()
    }
}