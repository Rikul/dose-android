package com.waseefakhtar.doseapp.feature.addmedication.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.waseefakhtar.doseapp.core.navigation.DoseNavigationDestination
import com.waseefakhtar.doseapp.domain.model.Medication
import com.waseefakhtar.doseapp.feature.addmedication.AddMedicationRoute
import com.waseefakhtar.doseapp.feature.home.navigation.ASK_ALARM_PERMISSION
import com.waseefakhtar.doseapp.feature.home.navigation.ASK_NOTIFICATION_PERMISSION

object AddMedicationDestination : DoseNavigationDestination {
    override val route = "add_medication_route?id={id}"
    override val destination = "add_medication_destination"
    
    fun createEditNavigationRoute(id: Long) = "add_medication_route?id=$id"
}

fun NavGraphBuilder.addMedicationGraph(navController: NavController, bottomBarVisibility: MutableState<Boolean>, fabVisibility: MutableState<Boolean>, onBackClicked: () -> Unit, navigateToMedicationConfirm: (List<Medication>) -> Unit) {
    composable(
        route = AddMedicationDestination.route,
        arguments = listOf(
            navArgument("id") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            }
        )
    ) {
        LaunchedEffect(null) {
            bottomBarVisibility.value = false
            fabVisibility.value = false
        }

        navController.previousBackStackEntry?.savedStateHandle.apply {
            this?.set(ASK_NOTIFICATION_PERMISSION, true)
        }
        navController.previousBackStackEntry?.savedStateHandle.apply {
            this?.set(ASK_ALARM_PERMISSION, true)
        }
        AddMedicationRoute(onBackClicked, navigateToMedicationConfirm)
    }
}
