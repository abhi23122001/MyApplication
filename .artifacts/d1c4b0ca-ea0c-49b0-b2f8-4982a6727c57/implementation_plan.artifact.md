# Implementation Plan - Enterprise Survey ERP (Shah Surveyors & Consultancy)

Full-scale deployment of the ERP system including error fixes, authentication persistence, attendance tracking with selfies, equipment CRUD, financial ledgers, and team communication.

## User Review Required

> [!IMPORTANT]
> The Webhook URL provided (`https://script.google.com/macros/s/AKfycbxjvqJKxsh2fOpqp8T-JRm2X5iL0W4_y9mqjw1pzihQ6Gtozruv1Jx7fENFWEwBnLwzZQ/exec`) will be used for all Cloud Sync operations. Ensure the Google Apps Script is deployed and accessible.

## Proposed Changes

### Core Infrastructure & Auth

#### [MODIFY] [MainAppViewModel.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/MainAppViewModel.kt)
- Integrate `SessionManager` into `login` and `logout` flows for persistent sessions.
- Implement missing Equipment CRUD methods: `addEquipment`, `deleteEquipment`.
- Ensure all repository calls are wrapped in `viewModelScope.launch` with error handling.

#### [MODIFY] [CloudSyncApiService.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/CloudSyncApiService.kt)
- Verify OkHttp configuration for redirects and 60s timeouts (already mostly present, will ensure full compliance).

---

### Features & Screens

#### [MODIFY] [EquipmentTrackerScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/EquipmentTrackerScreen.kt)
- Add "Add New Equipment" Floating Action Button for Admin.
- Add "Delete" button to Equipment items for Admin.
- Implement Add Equipment dialog with checklist chips.

#### [MODIFY] [FinanceScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/FinanceScreen.kt)
- Integrate a simple image picker or camera trigger for "Capture Receipt" and "Upload Payment Proof".
- Pass the captured image Base64 to `addExpense` and `addIncome`.

#### [MODIFY] [FinancialAnalyticsScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/FinancialAnalyticsScreen.kt)
- Add a clickable action to the "Monthly Net Profit" card that opens a detailed dialog with Inflow/Outflow breakdown.

#### [MODIFY] [ChatHubScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/ChatHubScreen.kt)
- Implement "Attach File" placeholder that simulates a file selection and Base64 upload.

#### [MODIFY] [AttendanceCaptureScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/AttendanceCaptureScreen.kt)
- Ensure high-accuracy GPS coordinates are captured before enabling the "Capture Selfie" button.

---

### UI & Branding

#### [MODIFY] [EmployeeDashboard.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/EmployeeDashboard.kt)
#### [MODIFY] [AdminDashboard.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/AdminDashboard.kt)
- Ensure the app logo (`R.drawable.app_logo`) is present in the `TopAppBar`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/AndroidManifest.xml)
- Verify `android:usesCleartextTraffic="true"` and all requested permissions (INTERNET, LOCATION, CAMERA).

## Verification Plan

### Automated Tests
- Run `app:assembleDebug` to ensure 0 compilation errors.

### Manual Verification
- **Auth:** Login, close app, reopen -> Should go to Dashboard. Logout -> Should go to Login.
- **Attendance:** Mark attendance -> Verify Base64 payload in logs.
- **Equipment:** Admin adds equipment -> Verify it appears in list. Admin deletes -> Verify removal.
- **Finance:** Add expense with "photo" -> Verify payload.
- **Navigation:** Click through all tabs in Dashboards and secondary screens to ensure no crashes.
