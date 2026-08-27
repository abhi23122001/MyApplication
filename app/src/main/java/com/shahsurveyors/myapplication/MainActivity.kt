package com.shahsurveyors.myapplication

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import com.shahsurveyors.myapplication.data.*
import com.shahsurveyors.myapplication.data.local.AppDatabase

import com.shahsurveyors.myapplication.ui.admin.*
import com.shahsurveyors.myapplication.ui.attendance.*
import com.shahsurveyors.myapplication.ui.auth.*
import com.shahsurveyors.myapplication.ui.billing.*
import com.shahsurveyors.myapplication.ui.chat.RadarScreen
import com.shahsurveyors.myapplication.ui.crm.*
import com.shahsurveyors.myapplication.ui.dashboard.*
import com.shahsurveyors.myapplication.ui.equipment.*
import com.shahsurveyors.myapplication.ui.finance.*
import com.shahsurveyors.myapplication.ui.leave.*
import com.shahsurveyors.myapplication.ui.more.MoreModulesScreen
import com.shahsurveyors.myapplication.ui.ops.*
import com.shahsurveyors.myapplication.ui.splash.SplashScreen
import com.shahsurveyors.myapplication.ui.survey.SurveyCalculatorScreen
import com.shahsurveyors.myapplication.ui.tasks.*
import com.shahsurveyors.myapplication.ui.components.MainScaffold
import com.shahsurveyors.myapplication.ui.theme.ShahTheme
import com.shahsurveyors.myapplication.utils.SalarySlipGenerator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    val auth = remember { FirebaseAuth.getInstance() }
                    val firestore = remember { FirebaseFirestore.getInstance() }
                    val storage = remember { FirebaseStorage.getInstance() }
                    val sessionManager = remember { SessionManager(context) }
                    val database = remember { AppDatabase.getDatabase(context) }
                    val authRepository = remember { AuthRepository(auth) }
                    val userRepository = remember { UserRepository(firestore) }
                    val attendanceRepository = remember { AttendanceRepository(firestore) }
                    val storageRepository = remember { StorageRepository() }
                    val projectRepository = remember { ProjectRepository(firestore) }
                    val expenseRepository = remember { ExpenseRepository(firestore) }
                    val equipmentRepository = remember { EquipmentRepository(firestore) }
                    val clientRepository = remember { ClientRepository(firestore) }
                    val dsrRepository = remember { DSRRepository(firestore) }
                    val taskRepository = remember { TaskRepository(firestore) }
                    val dashboardRepository = remember { DashboardRepository(firestore) }
                    val leaveRepository = remember { LeaveRepository(firestore) }
                    val billingRepository = remember { BillingRepository(database.appDao()) }
                    val salaryRepository = remember { SalaryRepository(firestore) }
                    val advanceSalaryRepository = remember { AdvanceSalaryRepository(firestore) }

                    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository, userRepository, sessionManager))
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(dashboardRepository))
                    val attendanceViewModel: AttendanceViewModel = viewModel(factory = AttendanceViewModelFactory(attendanceRepository, storageRepository))
                    val expenseViewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepository, storageRepository))
                    val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(billingRepository, userRepository, expenseRepository, attendanceRepository, projectRepository, equipmentRepository, clientRepository))
                    val equipmentViewModel: EquipmentViewModel = viewModel(factory = EquipmentViewModelFactory(equipmentRepository))
                    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(taskRepository))
                    val clientViewModel: ClientViewModel = viewModel(factory = ClientViewModelFactory(clientRepository))
                    val salaryViewModel: SalaryViewModel = viewModel(factory = SalaryViewModelFactory(userRepository, salaryRepository))
                    val dsrViewModel: DSRViewModel = viewModel(factory = DSRViewModelFactory(dsrRepository, storageRepository))

                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    MainScaffold(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) { modifier ->
                        NavHost(navController = navController, startDestination = "splash", modifier = modifier) {
                            composable("splash") {
                                SplashScreen(onAnimationFinished = {
                                    val destination = if (authViewModel.isUserLoggedIn) "dashboard" else "login"
                                    navController.navigate(destination) { popUpTo("splash") { inclusive = true } }
                                })
                            }
                            composable("login") {
                                LoginScreen(viewModel = authViewModel, onSignupClick = { navController.navigate("signup") }, onLoginSuccess = {
                                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true }; launchSingleTop = true }
                                })
                            }
                            composable("signup") { SignupScreen(viewModel = authViewModel, onBackToLogin = { navController.popBackStack() }) }
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    userName = authViewModel.userName,
                                    userRole = authViewModel.userRole,
                                    userAccess = authViewModel.userAccess,
                                    onNavigateToAttendance = { navController.navigate("attendance") },
                                    onNavigateToEquipment = { navController.navigate("equipment") },
                                    onNavigateToTasks = { navController.navigate("tasks") },
                                    onNavigateToSurvey = { navController.navigate("survey") },
                                    onNavigateToChat = { navController.navigate("chat") },
                                    onNavigateToAdmin = { navController.navigate("employees") },
                                    onNavigateToBilling = { navController.navigate("billing") },
                                    onNavigateToExpense = { navController.navigate("expense") },
                                    onNavigateToDsr = { navController.navigate("dsr") },
                                    onNavigateToClients = { navController.navigate("clients") },
                                    isAdmin = authViewModel.userRole.equals("admin", ignoreCase = true),
                                    onRefresh = { dashboardViewModel.refresh() }
                                )
                            }
                            composable("attendance") {
                                LaunchedEffect(authViewModel.currentUserUid) { authViewModel.currentUserUid?.let { attendanceViewModel.checkStatus(it) } }
                                AttendanceScreen(uid = authViewModel.currentUserUid ?: "", userName = authViewModel.userName)
                            }
                            composable("employees") { EmployeeManagementScreen(viewModel = adminViewModel, onBack = { navController.popBackStack() }) }
                            composable("leave") { LeaveManagementScreen(repository = leaveRepository, uid = authViewModel.currentUserUid ?: "", userName = authViewModel.userName, userRole = authViewModel.userRole, onBack = { navController.popBackStack() }) }
                            composable("advance_salary") { AdvanceSalaryScreen(repository = advanceSalaryRepository, uid = authViewModel.currentUserUid ?: "", userName = authViewModel.userName, onBack = { navController.popBackStack() }) }
                            composable("chat") { RadarScreen(onBack = { navController.popBackStack() }) }
                            composable("more") { MoreModulesScreen(onNavigate = { route -> navController.navigate(route) }) }
                            composable("equipment") { EquipmentTrackerScreen(viewModel = equipmentViewModel, onBack = { navController.popBackStack() }) }
                            composable("tasks") { TaskManagementScreen(viewModel = taskViewModel, uid = authViewModel.currentUserUid ?: "", onBack = { navController.popBackStack() }) }
                            composable("survey") { SurveyCalculatorScreen(onBack = { navController.popBackStack() }) }
                            composable("settings") { GeoFenceSettingsScreen(onBack = { navController.popBackStack() }) }
                            composable("admin_hub") { AdminHubScreen(viewModel = adminViewModel, onBack = { navController.popBackStack() }, onNavigateToCompanySettings = { navController.navigate("company_settings") }, onNavigateToBankDetails = { navController.navigate("bank_details") }, onNavigateToTerms = { navController.navigate("terms_conditions") }) }
                            composable("company_settings") { CompanySettingsScreen(viewModel = adminViewModel, onBack = { navController.popBackStack() }) }
                            composable("bank_details") { BankDetailsScreen(viewModel = adminViewModel, onBack = { navController.popBackStack() }) }
                            composable("terms_conditions") { TermsAndConditionsScreen(viewModel = adminViewModel, onBack = { navController.popBackStack() }) }
                            composable("salary") {
                                SalaryManagementScreen(
                                    viewModel = salaryViewModel,
                                    onBack = { navController.popBackStack() },
                                    onGenerateSalarySlip = { salary ->
                                        try {
                                            val file = SalarySlipGenerator.generatePdf(context, salary)
                                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "application/pdf")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (_: ActivityNotFoundException) {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Open or share salary slip"))
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                )
                            }
                            composable("billing") { val billingViewModel: BillingViewModel = viewModel(factory = BillingViewModelFactory(billingRepository)); BillingScreen(viewModel = billingViewModel, onBack = { navController.popBackStack() }) }
                            composable("expense") { ExpenseClaimsScreen(viewModel = expenseViewModel, uid = authViewModel.currentUserUid ?: "", userName = authViewModel.userName, onBack = { navController.popBackStack() }) }
                            composable("dsr") { DailyStatusReportScreen(viewModel = dsrViewModel, onBack = { navController.popBackStack() }) }
                            composable("clients") { CRMClientScreen(viewModel = clientViewModel, onBack = { navController.popBackStack() }) }
                            composable("projects") { Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { androidx.compose.material3.Text(text = "Projects module") } }
                            composable("marketing") { Box(modifier = Modifier.fillMaxSize()) }
                        }
                    }
                }
            }
        }
    }
}