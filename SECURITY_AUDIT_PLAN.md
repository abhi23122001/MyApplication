# Shah Surveyors ERP — Security Implementation Plan

## Scope
Implement strict per-user authorization and data isolation without deleting or resetting existing data.

## Order
1. Authentication identity and authorization foundation
2. Firebase Firestore/Storage security rules verification and enforcement
3. Expense ownership and access isolation
4. Attendance ownership and access isolation
5. Payroll ownership and access isolation
6. Leave/task/marketing/survey/equipment data isolation
7. Per-user module permissions and navigation guards
8. Admin permission management
9. IDOR and privilege-escalation testing
10. Final Admin vs Employee regression testing

## Rules
- Authenticated Firebase UID is the source of truth for normal-user ownership.
- Client-supplied userId/employeeId must never grant ownership or authorization.
- Normal employees are SELF ONLY for personal data unless an explicit business rule grants broader access.
- Admin access is controlled by authorization/permission, not merely UI visibility.
- Unauthorized module access must fail at UI, navigation, and backend/database layers.
- Existing production/test data must not be deleted, reset, or migrated destructively.

## Current confirmed risks
- ExpenseRepository accepts caller-provided expense.uid when saving.
- ExpenseRepository has a company-wide getAllExpenses() method without an authorization check in the repository layer.
- AttendanceRepository accepts caller-provided UID for punch-in/punch-out operations.
- AttendanceRepository exposes company-wide attendance query methods without authorization checks in the repository layer.
- SalaryRepository exposes getAllProfiles() and employeeUid-based history queries without authorization checks in the repository layer.

This file records the agreed implementation scope; application code is not modified by this plan itself.
