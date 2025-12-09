package com.trinity.hrm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trinity.hrm.ui.auth.LoginScreen
import com.trinity.hrm.ui.auth.SignupScreen
import com.trinity.hrm.ui.dashboard.DashboardScreen
import com.trinity.hrm.ui.employees.EmployeesScreen
import com.trinity.hrm.ui.tasks.TasksScreen
import com.trinity.hrm.ui.attendance.AttendanceScreen
import com.trinity.hrm.ui.departments.DepartmentsScreen
import com.trinity.hrm.ui.leaves.LeavesScreen
import com.trinity.hrm.ui.messages.MessagesScreen
import com.trinity.hrm.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Employees : Screen("employees")
    object Departments : Screen("departments")
    object Tasks : Screen("tasks")
    object Attendance : Screen("attendance")
    object Leaves : Screen("leaves")
    object Messages : Screen("messages")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Employees.route) {
            EmployeesScreen()
        }
        composable(Screen.Tasks.route) {
            TasksScreen()
        }
        composable(Screen.Attendance.route) {
            AttendanceScreen()
        }
        composable(Screen.Departments.route) {
            DepartmentsScreen()
        }
        composable(Screen.Leaves.route) {
            LeavesScreen()
        }
        composable(Screen.Messages.route) {
            MessagesScreen()
        }
    }
}

