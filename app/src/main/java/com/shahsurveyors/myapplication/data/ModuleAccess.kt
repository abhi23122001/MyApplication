package com.shahsurveyors.myapplication.data

/** Centralized client-side route/module permission mapping. Backend authorization remains authoritative. */
object ModuleAccess {
    private val routePermissions = mapOf(
        "attendance" to setOf("ATTENDANCE"),
        "expense" to setOf("EXPENSE", "EXPENSES"),
        "salary" to setOf("PAYROLL", "SALARY"),
        "advance_salary" to setOf("ADVANCE", "ADVANCE_SALARY"),
        "tasks" to setOf("TASKS", "TASK"),
        "marketing" to setOf("MARKETING"),
        "survey" to setOf("SURVEY"),
        "equipment" to setOf("EQUIPMENT"),
        "leave" to setOf("LEAVE", "LEAVES"),
        "dsr" to setOf("DSR", "DAILY_STATUS_REPORT"),
        "chat" to setOf("CHAT"),
        "clients" to setOf("CRM", "CLIENTS"),
        "add_client" to setOf("CRM", "CLIENTS"),
        "projects" to setOf("PROJECTS", "PROJECT"),
        "billing" to setOf("BILLING", "FINANCE"),
        "quotes" to setOf("BILLING", "FINANCE"),
        "reports_finance" to setOf("REPORTS", "FINANCE"),
        "reports_work" to setOf("REPORTS"),
        "employee_reports" to setOf("REPORTS", "EMPLOYEE_REPORTS"),
        "admin_hub" to setOf("ADMIN_HUB", "ADMIN"),
        "employees" to setOf("ADMIN_HUB", "ADMIN", "EMPLOYEES"),
        "employee_permissions" to setOf("ADMIN_HUB", "ADMIN", "EMPLOYEES"),
        "communication" to setOf("ADMIN_HUB", "ADMIN"),
        "company_settings" to setOf("ADMIN_HUB", "ADMIN"),
        "bank_details" to setOf("ADMIN_HUB", "ADMIN"),
        "terms_conditions" to setOf("ADMIN_HUB", "ADMIN"),
        "settings" to setOf("ADMIN_HUB", "ADMIN")
    )

    fun requiredPermissions(route: String): Set<String> = routePermissions[route].orEmpty()

    fun isAllowed(route: String, role: String, access: String): Boolean {
        if (role.trim().equals("admin", ignoreCase = true)) return true
        val required = requiredPermissions(route)
        if (required.isEmpty()) return true
        val granted = access.split(',', ';', '|')
            .map { it.trim().uppercase().replace(' ', '_') }
            .filter { it.isNotBlank() }
            .toSet()
        return "ALL" in granted || required.any(granted::contains)
    }
}
