package com.shahsurveyors.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.data.BillingRepository
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.SessionManager
import com.shahsurveyors.myapplication.data.local.AppDatabase
import com.shahsurveyors.myapplication.ui.admin.*
import com.shahsurveyors.myapplication.ui.attendance.AttendanceScreen
import com.shahsurveyors.myapplication.ui.attendance.AttendanceViewModel
import com.shahsurveyors.myapplication.ui.auth.AuthViewModel
import com.shahsurveyors.myapplication.ui.auth.AuthViewModelFactory
import com.shahsurveyors.myapplication.ui.auth.LoginScreen
import com.shahsurveyors.myapplication.ui.auth.SignupScreen
import com.shahsurveyors.myapplication.ui.billing.*
import com.shahsurveyors.myapplication.ui.chat.RadarScreen
import com.shahsurveyors.myapplication.ui.components.MainScaffold
import com.shahsurveyors.myapplication.ui.crm.CRMClientScreen
import com.shahsurveyors.myapplication.ui.dashboard.DashboardScreen
import com.shahsurveyors.myapplication.ui.dashboard.DashboardViewModel
import com.shahsurveyors.myapplication.ui.equipment.EquipmentTrackerScreen
import com.shahsurveyors.myapplication.ui.finance.SalaryManagementScreen
import com.shahsurveyors.myapplication.ui.finance.SalaryViewModel
import com.shahsurveyors.myapplication.ui.finance.SalaryViewModelFactory
import com.shahsurveyors.myapplication.ui.more.MoreModulesScreen
import com.shahsurveyors.myapplication.ui.ops.DailyStatusReportScreen
import com.shahsurveyors.myapplication.ui.ops.ExpenseClaimsScreen
import com.shahsurveyors.myapplication.ui.splash.SplashScreen
import com.shahsurveyors.myapplication.ui.survey.SurveyCalculatorScreen
import com.shahsurveyors.myapplication.ui.tasks.TaskManagementScreen
import com.shahsurveyors.myapplication.ui.tasks.TaskViewModel
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShahTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()

                    // Local database & repositories
                    val database = AppDatabase.getDatabase(context)
                    val billingRepository = BillingRepository(database.appDao())
                    val salaryRepository = SalaryRepository()
                    val sessionManager = SessionManager(context)

                    // ViewModels
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModelFactory(sessionManager)
                    )
                    val attendanceViewModel: AttendanceViewModel = viewModel()
                    val salaryViewModel: SalaryViewModel = viewModel(
                        factory = SalaryViewModelFactory(salaryRepository, billingRepository)
                    )
                    val taskViewModel: TaskViewModel = viewModel()

                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val isAdmin = authViewModel.userRole.equals("admin", ignoreCase = true)

                    MainScaffold(
                        currentRoute = currentRoute,
                        isAdmin = isAdmin,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) { modifier ->
                        NavHost(
                            navController = navController,
                            startDestination = "splash",
                            modifier = modifier,
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition = { slideOutHorizontally { -it } + fadeOut() },
                            popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
                            popExitTransition = { slideOutHorizontally { it } + fadeOut() }
                        ) {
                            // Splash
                            composable("splash") {
                                SplashScreen(
                                    onAnimationFinished = {
                                        val destination = if (authViewModel.isUserLoggedIn) "dashboard" else "login"
                                        navController.navigate(destination) {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Login
                            composable("login") {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onSignupClick = { navController.navigate("signup") },
                                    onLoginSuccess = {
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            // Signup
                            composable("signup") {
                                SignupScreen(
                                    viewModel = authViewModel,
                                    onBackToLogin = { navController.popBackStack() }
                                )
                            }

                            // Dashboard
                            composable("dashboard") {
                                val dashboardViewModel: DashboardViewModel = viewModel()

                                DashboardScreen(
                                    currentUid = authViewModel.userUid,
                                    userName = authViewModel.userName,
                                    userRole = authViewModel.userRole,
                                    userAccess = authViewModel.userAccess,
                                    onNavigateToAttendance = { navController.navigate("attendance") },
                                    onNavigateToEquipment = { navController.navigate("equipment") },
                                    onNavigateToTasks = { navController.navigate("tasks") },
                                    onNavigateToSurvey = { navController.navigate("survey") },
                                    onNavigateToChat = { navController.navigate("chat") },
                                    onNavigateToAdmin = {
                                        if (isAdmin) navController.navigate("admin_hub")
                                        else navController.navigate("tasks")
                                    },
                                    onNavigateToBilling = {
                                        if (isAdmin || authViewModel.userAccess.contains("BILLING")) navController.navigate("billing")
                                        else navController.navigate("salary")
                                    },
                                    onNavigateToExpense = { navController.navigate("expense") },
                                    onNavigateToDsr = { navController.navigate("dsr") },
                                    onNavigateToClients = { navController.navigate("clients") },
                                    onNavigateToSalary = { navController.navigate("salary") },
                                    isAdmin = isAdmin,
                                    isSyncing = dashboardViewModel.isLoading,
                                    onRefresh = { dashboardViewModel.fetchDashboardData() },
                                    viewModel = dashboardViewModel
                                )
                            }

                            // Attendance
                            composable("attendance") {
                                AttendanceScreen(
                                    viewModel = attendanceViewModel,
                                    staffName = authViewModel.userName,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Employees & Staff Management (ADMIN ONLY)
                            composable("employees") {
                                if (!isAdmin) {
                                    AccessDeniedScreen(onBack = { navController.popBackStack() })
                                } else {
                                    EmployeeManagementScreen(
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }

                            // Tasks (Admin & Employee)
                            composable("tasks") {
                                TaskManagementScreen(
                                    viewModel = taskViewModel,
                                    currentUid = authViewModel.userUid,
                                    currentUserName = authViewModel.userName,
                                    isAdmin = isAdmin,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Chat / Live Radar
                            composable("chat") {
                                RadarScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // More Modules
                            composable("more") {
                                MoreModulesScreen(
                                    userRole = authViewModel.userRole,
                                    userAccess = authViewModel.userAccess,
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }

                            // Equipment Tracker
                            composable("equipment") {
                                EquipmentTrackerScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Survey Calculator
                            composable("survey") {
                                SurveyCalculatorScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Settings / GeoFence
                            composable("settings") {
                                if (!isAdmin) {
                                    AccessDeniedScreen(onBack = { navController.popBackStack() })
                                } else {
                                    GeoFenceSettingsScreen(
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }

                            // Admin Hub (ADMIN ONLY)
                            composable("admin_hub") {
                                if (!isAdmin) {
                                    AccessDeniedScreen(onBack = { navController.popBackStack() })
                                } else {
                                    val adminViewModel: AdminViewModel = viewModel(
                                        factory = AdminViewModelFactory(billingRepository)
                                    )

                                    AdminHubScreen(
                                        viewModel = adminViewModel,
                                        onBack = { navController.popBackStack() },
                                        onNavigateToCompanySettings = { navController.navigate("company_settings") },
                                        onNavigateToBankDetails = { navController.navigate("bank_details") },
                                        onNavigateToTerms = { navController.navigate("terms_conditions") }
                                    )
                                }
                            }

                            // Company Settings (ADMIN ONLY)
                            composable("company_settings") {
                                if (!isAdmin) {
                                    AccessDeniedScreen(onBack = { navController.popBackStack() })
                                } else {
                                    val adminViewModel: AdminViewModel = viewModel(
                                        factory = AdminViewModelFactory(billingRepository)
                                    )
                                    CompanySettingsScreen(
                                        viewModel = adminViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }

                            // Bank Details (ADMIN ONLY)
                            composable("bank_details") {
                                if (!isAdmin) {
                                    AccessDeniedScreen(onBack = { navController.popBackStack() })
                                } else {
                                    val adminViewModel: AdminViewModel = viewModel(
                                        factory = AdminViewModelFactory(billingRepository)
                                    )
                                    BankDetailsScreen(
                                        viewModel = adminViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }

                            // Terms & Conditions (ADMIN ONLY)
                            composable("terms_conditions") {
                                if (!isAdmin) {
                                    AccessDeniedScreen(onBack = { navController.popBackStack() })
                                } else {
                                    val adminViewModel: AdminViewModel = viewModel(
                                        factory = AdminViewModelFactory(billingRepository)
                                    )
                                    TermsAndConditionsScreen(
                                        viewModel = adminViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }

                            // Salary & Payroll (Admin & Employee)
                            composable("salary") {
                                SalaryManagementScreen(
                                    viewModel = salaryViewModel,
                                    currentUid = authViewModel.userUid,
                                    currentUserName = authViewModel.userName,
                                    currentUserEmpId = authViewModel.userEmployeeId,
                                    currentUserDept = authViewModel.userDepartment,
                                    isAdmin = isAdmin,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Billing
                            composable("billing") {
                                val billingViewModel: BillingViewModel = viewModel(
                                    factory = BillingViewModelFactory(billingRepository)
                                )
                                BillingScreen(
                                    viewModel = billingViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Expense Claims
                            composable("expense") {
                                ExpenseClaimsScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // Daily Status Report (DSR)
                            composable("dsr") {
                                val firestore = FirebaseFirestore.getInstance()
                                DailyStatusReportScreen(
                                    onBack = { navController.popBackStack() },
                                    onSubmit = { chainage, points, area, instrument, remarks, fileUri ->
                                        coroutineScope.launch {
                                            try {
                                                val dsrDoc = firestore.collection("daily_reports").document()
                                                val dsrData = hashMapOf(
                                                    "id" to dsrDoc.id,
                                                    "employeeUid" to authViewModel.userUid,
                                                    "employeeName" to authViewModel.userName,
                                                    "chainage" to chainage,
                                                    "points" to points,
                                                    "area" to area,
                                                    "instrument" to instrument,
                                                    "remarks" to remarks,
                                                    "fileUri" to (fileUri?.toString() ?: ""),
                                                    "submittedAt" to System.currentTimeMillis()
                                                )
                                                dsrDoc.set(dsrData).await()
                                                Toast.makeText(context, "DSR Submitted Successfully", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Failed to submit DSR: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                )
                            }

                            // Clients CRM
                            composable("clients") {
                                CRMClientScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccessDeniedScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShahGrey)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Access Denied",
                tint = ErrorRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Access Restricted",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ShahBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This module requires Administrator permissions.\nPlease contact HR / Admin if you need access.",
                color = ShahMediumGrey,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite)
            ) {
                Text("GO BACK")
            }
        }
    }
}
