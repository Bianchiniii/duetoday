package br.com.contaemdia.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.contaemdia.presentation.bill_detail.BillDetailRoute
import br.com.contaemdia.presentation.bill_form.BillFormRoute
import br.com.contaemdia.presentation.dashboard.DashboardRoute
import br.com.contaemdia.presentation.summary.SummaryRoute

object Routes {
    const val DASHBOARD = "dashboard"
    const val SUMMARY = "summary"
    const val BILL_FORM = "bill_form?billId={billId}"
    const val BILL_DETAIL = "bill_detail/{billId}"

    fun billForm(billId: Long = 0L): String = "bill_form?billId=$billId"
    fun billDetail(billId: Long): String = "bill_detail/$billId"
}

@Composable
fun ContaEmDiaNavHost(
    navController: NavHostController,
    adsEnabled: Boolean,
) {
    fun navigateToDashboard() {
        navController.navigate(Routes.DASHBOARD) {
            popUpTo(Routes.DASHBOARD) { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardRoute(
                onAddBill = { navController.navigate(Routes.billForm()) },
                onOpenBill = { navController.navigate(Routes.billDetail(it)) },
                onOpenSummary = { navController.navigate(Routes.SUMMARY) },
                adsEnabled = adsEnabled,
            )
        }
        composable(Routes.SUMMARY) {
            SummaryRoute(
                onBack = { navController.popBackStack() },
                adsEnabled = adsEnabled,
            )
        }
        composable(
            route = Routes.BILL_FORM,
            arguments = listOf(navArgument("billId") { type = NavType.LongType; defaultValue = 0L }),
        ) { entry ->
            BillFormRoute(
                billId = entry.arguments?.getLong("billId") ?: 0L,
                onBack = { navController.popBackStack() },
                onSaved = { navigateToDashboard() },
            )
        }
        composable(
            route = Routes.BILL_DETAIL,
            arguments = listOf(navArgument("billId") { type = NavType.LongType }),
        ) { entry ->
            val billId = entry.arguments?.getLong("billId") ?: return@composable
            BillDetailRoute(
                billId = billId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.billForm(billId)) },
                onActionCompleted = { navigateToDashboard() },
            )
        }
    }
}
