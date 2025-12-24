package com.app.brfaceresponder

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.brfaceresponder.ui.screens.AppSelectionScreen
import com.app.brfaceresponder.ui.screens.RuleEditorScreen
import com.app.brfaceresponder.ui.screens.RuleListScreen
import com.app.brfaceresponder.ui.viewmodel.RuleViewModel

object AppDestinations {
    const val RULE_LIST = "rule_list"
    const val RULE_EDITOR = "rule_editor"
    const val APP_SELECTION = "app_selection"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val ruleViewModel: RuleViewModel = viewModel()

    NavHost(navController = navController, startDestination = AppDestinations.RULE_LIST) {
        composable(AppDestinations.RULE_LIST) {
            RuleListScreen(
                viewModel = ruleViewModel,
                onAddRuleClicked = {
                    navController.navigate(AppDestinations.RULE_EDITOR)
                },
                onManageAppsClicked = {
                    navController.navigate(AppDestinations.APP_SELECTION)
                }
            )
        }
        composable(AppDestinations.RULE_EDITOR) {
            RuleEditorScreen(
                viewModel = ruleViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppDestinations.APP_SELECTION) {
            AppSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}