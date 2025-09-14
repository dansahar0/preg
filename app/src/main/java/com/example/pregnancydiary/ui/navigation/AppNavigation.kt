package com.example.pregnancydiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pregnancydiary.ui.overview.WeeklyOverviewScreen

import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.pregnancydiary.ui.calendar.CalendarScreen
import com.example.pregnancydiary.ui.detail.DetailScreen
import com.example.pregnancydiary.ui.detail.DetailViewModel

object AppDestinations {
    const val OVERVIEW_ROUTE = "overview"
    const val DETAIL_ROUTE = "detail"
    const val CALENDAR_ROUTE = "calendar"
    const val WEEK_ID_ARG = "weekId"
    val detailRouteWithArgs = "$DETAIL_ROUTE/{$WEEK_ID_ARG}"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.OVERVIEW_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppDestinations.OVERVIEW_ROUTE) {
            WeeklyOverviewScreen(
                onWeekClicked = { weekId ->
                    navController.navigate("${AppDestinations.DETAIL_ROUTE}/$weekId")
                },
                onCalendarClicked = {
                    navController.navigate(AppDestinations.CALENDAR_ROUTE)
                }
            )
        }
        composable(
            route = AppDestinations.detailRouteWithArgs,
            arguments = listOf(navArgument(AppDestinations.WEEK_ID_ARG) { type = NavType.IntType })
        ) {
            val viewModel: DetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            DetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onExportClicked = { viewModel.exportToPdf() }
            )
        }
        composable(AppDestinations.CALENDAR_ROUTE) {
            // This is a bit of a hack. A better way would be to use a DI framework
            // to get the same ViewModel instance here and in the screen, or to have the
            // screen itself handle the call. But for simplicity, this works.
            val viewModel: com.example.pregnancydiary.ui.calendar.CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            CalendarScreen(
                onExportClicked = {
                    viewModel.exportReminders()
                }
            )
        }
    }
}
