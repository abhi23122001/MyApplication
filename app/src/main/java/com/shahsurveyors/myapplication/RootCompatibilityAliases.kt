package com.shahsurveyors.myapplication

/**
 * Compatibility aliases for the operational screens/models that live in ui.ops.
 * This keeps MainActivity's existing architecture intact without duplicating implementations.
 */
typealias ExpenseViewModel = com.shahsurveyors.myapplication.ui.ops.ExpenseViewModel
typealias ExpenseViewModelFactory = com.shahsurveyors.myapplication.ui.ops.ExpenseViewModelFactory
typealias ExpenseClaimsScreen = com.shahsurveyors.myapplication.ui.ops.ExpenseClaimsScreen

typealias DSRViewModel = com.shahsurveyors.myapplication.ui.ops.DSRViewModel
typealias DSRViewModelFactory = com.shahsurveyors.myapplication.ui.ops.DSRViewModelFactory
typealias DailyStatusReportScreen = com.shahsurveyors.myapplication.ui.ops.DailyStatusReportScreen
