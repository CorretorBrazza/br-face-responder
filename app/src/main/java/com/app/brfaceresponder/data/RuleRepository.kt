package com.app.brfaceresponder.data

import android.content.Context
import com.app.brfaceresponder.model.Rule
import java.util.UUID

object RuleRepository {

    // No internal state - the repository is now a stateless service
    // that interacts directly with SharedPrefs.

    fun getRules(context: Context): List<Rule> {
        return SharedPrefs.getRules(context)
    }

    fun addRule(context: Context, rule: Rule) {
        val currentRules = getRules(context).toMutableList()
        currentRules.add(rule)
        SharedPrefs.saveRules(context, currentRules.sortedBy { it.priority })
    }

    fun updateRule(context: Context, rule: Rule) {
        val currentRules = getRules(context).toMutableList()
        val index = currentRules.indexOfFirst { it.id == rule.id }
        if (index != -1) {
            currentRules[index] = rule
            SharedPrefs.saveRules(context, currentRules.sortedBy { it.priority })
        }
    }

    fun deleteRule(context: Context, rule: Rule) {
        val currentRules = getRules(context).toMutableList()
        currentRules.removeAll { it.id == rule.id }
        SharedPrefs.saveRules(context, currentRules)
    }

    fun findRuleForMessage(context: Context, message: String): Rule? {
        val rules = getRules(context)
        return rules.sortedBy { it.priority }.firstOrNull { rule ->
            if (rule.isRegex) {
                try {
                    Regex(rule.keyword, RegexOption.IGNORE_CASE).containsMatchIn(message)
                } catch (e: Exception) {
                    // Invalid regex, ignore this rule for matching
                    false
                }
            } else {
                message.contains(rule.keyword, ignoreCase = true)
            }
        }
    }
}