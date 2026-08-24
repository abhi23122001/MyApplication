package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.AttendanceRecord
import com.shahsurveyors.myapplication.models.ExpenseRecord
import com.shahsurveyors.myapplication.models.UserProfile
import com.shahsurveyors.myapplication.ui.components.GlobalAsyncLoader
import com.shahsurveyors.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onNavigateToCompanySettings: () -> Unit = {},
    onNavigateToBankDetails: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {}
) {

    var showCreateUserDialog by remember {
        mutableStateOf(false)
    }

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAdminData()
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Admin Hub",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = ShahDarkGreen
                    )
            )
        },

        floatingActionButton = {

            if (selectedTab == 0) {

                ExtendedFloatingActionButton(

                    onClick = {
                        showCreateUserDialog = true
                    },

                    icon = {
                        Icon(
                            imageVector =
                                Icons.Default.PersonAdd,
                            contentDescription = null
                        )
                    },

                    text = {
                        Text("Add Employee")
                    },

                    containerColor = ShahGreen,
                    contentColor = ShahWhite
                )
            }
        }

    ) { paddingValues ->

        Column(

            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(ShahGrey)
        ) {

            ScrollableTabRow(

                selectedTabIndex =
                    selectedTab,

                containerColor =
                    ShahWhite,

                contentColor =
                    ShahGreen,

                edgePadding =
                    16.dp,

                indicator = { tabPositions ->

                    if (
                        selectedTab <
                        tabPositions.size
                    ) {

                        TabRowDefaults.SecondaryIndicator(

                            modifier =
                                Modifier.tabIndicatorOffset(
                                    tabPositions[selectedTab]
                                ),

                            color =
                                ShahGreen
                        )
                    }
                }

            ) {

                Tab(
                    selected =
                        selectedTab == 0,

                    onClick = {
                        selectedTab = 0
                    },

                    text = {
                        Text(
                            "Logins",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                )

                Tab(
                    selected =
                        selectedTab == 1,

                    onClick = {
                        selectedTab = 1
                    },

                    text = {
                        Text(
                            "Expenses",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                )

                Tab(
                    selected =
                        selectedTab == 2,

                    onClick = {

                        selectedTab = 2

                        viewModel.refreshAttendance()
                    },

                    text = {
                        Text(
                            "Attendance",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                )

                Tab(
                    selected =
                        selectedTab == 3,

                    onClick = {
                        selectedTab = 3
                    },

                    text = {
                        Text(
                            "Settings",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                )
            }

            when (selectedTab) {

                0 ->
                    PendingLoginsList(
                        viewModel
                    )

                1 ->
                    PendingExpensesList(
                        viewModel
                    )

                2 ->
                    AdminAttendanceList(
                        viewModel
                    )

                3 ->
                    AdminSettingsList(

                        onNavigateToCompanySettings =
                            onNavigateToCompanySettings,

                        onNavigateToBankDetails =
                            onNavigateToBankDetails,

                        onNavigateToTerms =
                            onNavigateToTerms
                    )
            }
        }
    }

    if (showCreateUserDialog) {

        AlertDialog(

            onDismissRequest = {
                showCreateUserDialog = false
            },

            title = {
                Text("Add Employee")
            },

            text = {
                Text(
                    "Employee creation can be connected here."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showCreateUserDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    GlobalAsyncLoader(
        isLoading =
            viewModel.isLoading
    )
}


// =========================================================
// PENDING LOGINS
// =========================================================

@Composable
fun PendingLoginsList(
    viewModel: AdminViewModel
) {

    if (
        viewModel.pendingUsers.isEmpty() &&
        !viewModel.isLoading
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    "No pending login requests",
                color =
                    ShahMediumGrey
            )
        }

        return
    }

    LazyColumn(

        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(16.dp)
    ) {

        items(
            items =
                viewModel.pendingUsers,
            key = {
                it.uid
            }
        ) { user ->

            Card(

                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),

                shape =
                    RoundedCornerShape(12.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            ShahWhite
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            2.dp
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(16.dp)
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    user.name,
                                color =
                                    ShahBlack,
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize =
                                    18.sp
                            )

                            Text(
                                text =
                                    user.email,
                                color =
                                    ShahMediumGrey,
                                fontSize =
                                    12.sp
                            )

                            Text(
                                text =
                                    "Dept: ${user.department}",
                                color =
                                    ShahGreen,
                                fontSize =
                                    11.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                // Reject can be added later
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Close,
                                contentDescription =
                                    "Reject",
                                tint =
                                    ErrorRed
                            )
                        }

                        IconButton(

                            onClick = {

                                viewModel.approveUser(
                                    user.uid,
                                    user.access
                                )
                            }

                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Check,
                                contentDescription =
                                    "Approve",
                                tint =
                                    SuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}


// =========================================================
// PENDING EXPENSES
// =========================================================

@Composable
fun PendingExpensesList(
    viewModel: AdminViewModel
) {

    if (
        viewModel.pendingExpenses.isEmpty() &&
        !viewModel.isLoading
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    "No pending expense claims",
                color =
                    ShahMediumGrey
            )
        }

        return
    }

    LazyColumn(

        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(16.dp)
    ) {

        items(
            viewModel.pendingExpenses
        ) { expense ->

            Card(

                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),

                shape =
                    RoundedCornerShape(12.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            ShahWhite
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation =
                            2.dp
                    )
            ) {

                Row(

                    modifier =
                        Modifier.padding(16.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                expense.userName,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                ShahBlack
                        )

                        Text(
                            text =
                                expense.category,
                            color =
                                ShahGreen,
                            fontSize =
                                12.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                expense.description,
                            color =
                                ShahMediumGrey,
                            fontSize =
                                11.sp
                        )

                        Text(
                            text =
                                "₹ ${expense.amount}",
                            color =
                                ShahBlack,
                            fontWeight =
                                FontWeight.ExtraBold,
                            fontSize =
                                16.sp
                        )
                    }

                    IconButton(

                        onClick = {

                            viewModel.approveExpense(
                                expense.id,
                                "REJECTED"
                            )
                        }

                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Cancel,
                            contentDescription =
                                "Reject",
                            tint =
                                ErrorRed
                        )
                    }

                    IconButton(

                        onClick = {

                            viewModel.approveExpense(
                                expense.id,
                                "APPROVED"
                            )
                        }

                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.CheckCircle,
                            contentDescription =
                                "Approve",
                            tint =
                                SuccessGreen
                        )
                    }
                }
            }
        }
    }
}


// =========================================================
// ADMIN ATTENDANCE
// =========================================================

@Composable
fun AdminAttendanceList(
    viewModel: AdminViewModel
) {

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        // -----------------------------------------------------
        // HEADER STATS
        // -----------------------------------------------------

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            StatItem(
                label =
                    "Present",
                value =
                    viewModel.attendanceSummary.size.toString(),
                color =
                    SuccessGreen,
                modifier =
                    Modifier.weight(1f)
            )

            StatItem(
                label =
                    "Absent",
                value =
                    (
                            viewModel.allEmployees.count { it.active } -
                                    viewModel.attendanceSummary.size
                            ).coerceAtLeast(0).toString(),
                color =
                    ErrorRed,
                modifier =
                    Modifier.weight(1f)
            )

            StatItem(
                label =
                    "Total",
                value =
                    viewModel.allEmployees
                        .count {
                            it.active
                        }
                        .toString(),
                color =
                    ShahGreen,
                modifier =
                    Modifier.weight(1f)
            )
        }

        // -----------------------------------------------------
        // REFRESH
        // -----------------------------------------------------

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),

            horizontalArrangement =
                Arrangement.End
        ) {

            OutlinedButton(

                onClick = {
                    viewModel.fetchAdminData()
                }

            ) {

                Icon(
                    imageVector =
                        Icons.Default.Refresh,
                    contentDescription =
                        "Refresh"
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text("Refresh")
            }
        }

        HorizontalDivider(
            color =
                ShahLightGrey
        )

        // -----------------------------------------------------
        // ATTENDANCE LIST
        // -----------------------------------------------------

        if (
            viewModel.attendanceSummary.isEmpty() &&
            !viewModel.isLoading
        ) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.EventBusy,
                        contentDescription =
                            null,
                        tint =
                            ShahMediumGrey,
                        modifier =
                            Modifier.size(48.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "No attendance records today",
                        color =
                            ShahMediumGrey,
                        fontSize =
                            14.sp
                    )
                }
            }

            return@Column
        }

        LazyColumn(

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            items(
                items =
                    viewModel.attendanceSummary,
                key = {
                    it.id
                }
            ) { attendance ->

                AttendanceCard(
                    attendance =
                        attendance
                )
            }
        }
    }
}


// =========================================================
// ATTENDANCE CARD
// =========================================================

@Composable
fun AttendanceCard(
    attendance: AttendanceRecord
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(14.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    ShahWhite
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            // -------------------------------------------------
            // EMPLOYEE + STATUS
            // -------------------------------------------------

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            attendance.userName,
                        fontSize =
                            18.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            ShahBlack
                    )

                    Text(
                        text =
                            attendance.siteName,
                        fontSize =
                            12.sp,
                        color =
                            ShahGreen,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Surface(

                    color =
                        if (
                            attendance.punchOutTime != null
                        ) {
                            ShahLightGrey
                        } else {
                            SuccessGreen.copy(
                                alpha = 0.15f
                            )
                        },

                    shape =
                        RoundedCornerShape(20.dp)
                ) {

                    Text(

                        text =
                            if (
                                attendance.punchOutTime != null
                            ) {
                                "COMPLETED"
                            } else {
                                "PUNCHED IN"
                            },

                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),

                        fontSize =
                            10.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            if (
                                attendance.punchOutTime != null
                            ) {
                                ShahMediumGrey
                            } else {
                                SuccessGreen
                            }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            HorizontalDivider(
                color =
                    ShahLightGrey
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            // -------------------------------------------------
            // PUNCH TIMES
            // -------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                AttendanceTimeItem(
                    title =
                        "Punch In",
                    value =
                        formatAttendanceTime(
                            attendance.punchInTime
                        ),
                    modifier =
                        Modifier.weight(1f)
                )

                AttendanceTimeItem(
                    title =
                        "Punch Out",
                    value =
                        formatAttendanceTime(
                            attendance.punchOutTime
                        ),
                    modifier =
                        Modifier.weight(1f)
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            // -------------------------------------------------
            // GPS
            // -------------------------------------------------

            Text(
                text =
                    "📍 Punch In GPS",
                fontWeight =
                    FontWeight.Bold,
                fontSize =
                    12.sp,
                color =
                    ShahBlack
            )

            Text(
                text =
                    "${attendance.punchInLat}, ${attendance.punchInLng}",
                fontSize =
                    11.sp,
                color =
                    ShahMediumGrey
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            if (
                attendance.punchOutTime != null
            ) {

                Text(
                    text =
                        "📍 Punch Out GPS",
                    fontWeight =
                        FontWeight.Bold,
                    fontSize =
                        12.sp,
                    color =
                        ShahBlack
                )

                Text(
                    text =
                        "${attendance.punchOutLat ?: "--"}, ${attendance.punchOutLng ?: "--"}",
                    fontSize =
                        11.sp,
                    color =
                        ShahMediumGrey
                )
            }

            // -------------------------------------------------
            // SELFIE URL
            // -------------------------------------------------

            if (
                !attendance.selfieUrl.isNullOrBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    text =
                        "📷 Selfie uploaded",
                    fontSize =
                        12.sp,
                    color =
                        SuccessGreen,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// =========================================================
// TIME ITEM
// =========================================================

@Composable
fun AttendanceTimeItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Surface(

        modifier =
            modifier,

        color =
            ShahGrey,

        shape =
            RoundedCornerShape(8.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(10.dp)
        ) {

            Text(
                text =
                    title,
                fontSize =
                    10.sp,
                color =
                    ShahMediumGrey
            )

            Text(
                text =
                    value,
                fontSize =
                    14.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    ShahBlack
            )
        }
    }
}


// =========================================================
// FORMAT TIME
// =========================================================

private fun formatAttendanceTime(
    timestamp: com.google.firebase.Timestamp?
): String {

    if (timestamp == null) {
        return "--:--"
    }

    return SimpleDateFormat(
        "hh:mm a",
        Locale.ENGLISH
    ).format(
        timestamp.toDate()
    )
}


// =========================================================
// STAT ITEM
// =========================================================

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier
) {

    Surface(

        modifier =
            modifier,

        color =
            color.copy(
                alpha = 0.1f
            ),

        shape =
            RoundedCornerShape(8.dp)
    ) {

        Column(

            modifier =
                Modifier.padding(8.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    label,
                fontSize =
                    10.sp,
                color =
                    color,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    value,
                fontSize =
                    16.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    ShahBlack
            )
        }
    }
}


// =========================================================
// SETTINGS
// =========================================================

@Composable
fun AdminSettingsList(
    onNavigateToCompanySettings: () -> Unit,
    onNavigateToBankDetails: () -> Unit,
    onNavigateToTerms: () -> Unit
) {

    LazyColumn(

        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(16.dp)
    ) {

        item {

            AdminSettingItem(
                title =
                    "Company Profile",
                subtitle =
                    "Logo, Address, Contact Info",
                icon =
                    Icons.Default.Business,
                onClick =
                    onNavigateToCompanySettings
            )

            AdminSettingItem(
                title =
                    "Bank Details",
                subtitle =
                    "Account No, IFSC, GSTIN",
                icon =
                    Icons.Default.AccountBalance,
                onClick =
                    onNavigateToBankDetails
            )

            AdminSettingItem(
                title =
                    "Terms & Conditions",
                subtitle =
                    "Reusable document terms",
                icon =
                    Icons.Default.Gavel,
                onClick =
                    onNavigateToTerms
            )

            AdminSettingItem(
                title =
                    "Document Numbering",
                subtitle =
                    "Prefixes and sequences",
                icon =
                    Icons.Default.Numbers,
                onClick = {}
            )

            AdminSettingItem(
                title =
                    "Geo-Fence Settings",
                subtitle =
                    "Radius and allowed sites",
                icon =
                    Icons.Default.Map,
                onClick = {}
            )
        }
    }
}


// =========================================================
// SETTINGS ITEM
// =========================================================

@Composable
fun AdminSettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    Card(

        modifier =
            Modifier
                .padding(vertical = 6.dp)
                .fillMaxWidth()
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(12.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    ShahWhite
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    1.dp
            )
    ) {

        Row(

            modifier =
                Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                tint =
                    ShahGreen,
                modifier =
                    Modifier.size(24.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        title,
                    color =
                        ShahBlack,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize =
                        16.sp
                )

                Text(
                    text =
                        subtitle,
                    color =
                        ShahMediumGrey,
                    fontSize =
                        12.sp
                )
            }

            Icon(
                imageVector =
                    Icons.Default.ChevronRight,
                contentDescription =
                    null,
                tint =
                    ShahLightGrey
            )
        }
    }
}