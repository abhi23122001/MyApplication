# SHAH ERP - Premium Redesign & Functional Implementation Plan

This plan outlines the complete overhaul of the SHAH ERP application to meet professional corporate standards, following the "Green + White + Black + Neutral Grey" design target while preserving the original SHAH logo and core functionality.

## User Review Required

> [!IMPORTANT]
> **Logo Consistency**: The original logo will be used without any modifications. The UI theme will adapt to complement it.
> **Backend Integration**: The current Google Apps Script (Webhook) backend will be extended to support new features (Salary, Advances, Quotations).
> **Permissions**: Role-based access control will be strictly enforced as per requirements.

## Proposed Changes

### 1. Core Design System & Theming
Update the application theme to a professional corporate style.

#### [MODIFY] [Color.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/theme/Color.kt)
- Define `ShahGreen` (Primary), `ShahDarkGreen` (AppBars), `ShahWhite`, `ShahBlack`, and `ShahNeutralGrey`.
- Define status colors: `SuccessGreen`, `ErrorRed`, `PendingAmber`.

#### [MODIFY] [Theme.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/theme/Theme.kt)
- Configure `ShahTheme` with the new color palette and professional typography.

---

### 2. Navigation & Architecture
Implement a clean bottom navigation and a structured "More" modules screen.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/MainActivity.kt)
- Update `NavHost` to support Bottom Navigation.
- Add routes for "More", "Employees", and "Chat".

#### [NEW] [MainScaffold.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/components/MainScaffold.kt)
- Create a shared scaffold with the Bottom Navigation bar.

---

### 3. Identity & Authentication
Redesign Splash and Login for a premium first impression.

#### [MODIFY] [SplashScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/splash/SplashScreen.kt)
- Implement professional animation with the original logo.
- Add "All-in-One Business Management" and versioning.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/auth/LoginScreen.kt)
- Corporate layout with "Welcome Back", Employee ID, and "Pending Approval" status.

---

### 4. Dashboards & Module Categorization
Implement the "More Modules" screen and a data-driven Admin Dashboard.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/dashboard/DashboardScreen.kt)
- Redesign with summary cards (Employees, Attendance, Projects, Expenses).
- Implement "Admin Broadcast" section (dynamic data).

#### [NEW] [MoreModulesScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/more/MoreModulesScreen.kt)
- Categorized modules (People & HR, Finance, Project & Survey, etc.) with professional cards.

---

### 5. Field Operations & Attendance
Strict GPS + Selfie attendance for transparency.

#### [MODIFY] [AttendanceScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/attendance/AttendanceScreen.kt)
- Enforce Selfie + GPS + Area selection before Punch IN.
- Display "Working Duration" and "Total Hours" on Punch OUT.

---

### 6. Financial & Document Management
Official document generation and financial tracking.

#### [NEW] [SalaryManagement.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/finance/SalaryManagement.kt)
- Automated salary calculation based on attendance and advances.
- PDF Salary Slip generation with official branding.

#### [MODIFY] [BillingScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/billing/BillingScreen.kt)
- Redesign Quotation/Invoice to follow SHAH official format (ISO 9001:2015).
- Add "Convert to Invoice" functionality.

---

### 7. Administrative Control
Comprehensive settings and audit logs.

#### [MODIFY] [AdminHubScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/admin/AdminHubScreen.kt)
- Redesign for better management of users, roles, and global settings (Geo-fence, Grace period, etc.).

## Verification Plan

### Automated Tests
- Run `app:assembleDebug` to ensure compilation.
- Verify Navigation graph completeness.

### Manual Verification
- **Attendance**: Test Punch IN/OUT with simulated GPS and Camera.
- **Documents**: Verify PDF generation layout for Quotation and Salary Slip.
- **Responsive**: Test UI on different device configurations in the emulator.
