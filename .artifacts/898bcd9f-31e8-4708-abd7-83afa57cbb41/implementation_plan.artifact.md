# Enterprise Survey ERP Implementation Plan

Finalize and deploy the Enterprise Survey ERP for "Shah Surveyors & Consultancy" with full Google Apps Script integration.

## User Review Required

> [!IMPORTANT]
> - The Webhook URL is already configured in `NetworkClient.kt`.
> - "Digital Seal & Signature" requires `R.drawable.seal_signature` to be present. I will assume it exists or use a placeholder if not.
> - Branding requires `R.drawable.app_logo`.

## Proposed Changes

### 1. Global UI & Loading State
- **[MODIFY] [AppNavigation.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/AppNavigation.kt)**: Wrap the `NavHost` in a `Box` to show a full-screen semi-transparent overlay with `CircularProgressIndicator` when `viewModel.isLoading` is true.
- **[MODIFY] [MainAppViewModel.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/MainAppViewModel.kt)**: Ensure `isLoading` is correctly managed across all API calls.

### 2. Admin Gatekeeper (Auth & User Approval)
- **[MODIFY] [LoginScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/LoginScreen.kt)**: Update login logic to verify `APPROVED` status and use `VERIFY_USER_LOGIN` action.
- **[MODIFY] [RegisterScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/RegisterScreen.kt)**: Rename UI to "Signup" and use `USER_SIGNUP_REQUEST` action with Department selection.
- **[MODIFY] [StaffApprovalScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/StaffApprovalScreen.kt)**: Update to `UserApprovalScreen.kt` logic (Fetch pending users via `GET_PENDING_USERS`, Approve/Reject via `ADMIN_APPROVE_USER`).
- **[MODIFY] [AppScriptModels.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/AppScriptModels.kt)**: Update `CloudSyncPayload` with all necessary fields.

### 3. Expense Reimbursement
- **[NEW] [SubmitExpenseScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/SubmitExpenseScreen.kt)**: Form for category, amount, site, and receipt capture.
- **[NEW] [ExpenseApprovalScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/ExpenseApprovalScreen.kt)**: Admin screen to review and approve expenses.
- **[MODIFY] [MainAppViewModel.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/MainAppViewModel.kt)**: Add methods for submitting and approving expenses.

### 4. Attendance & Watermarking
- **[MODIFY] [AttendanceCaptureScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/AttendanceCaptureScreen.kt)**: Integrate watermarking logic.
- **[MODIFY] [ImageUtils.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/ImageUtils.kt)**: Add function to draw watermark on Bitmap (Date, Time, GPS, Name).
- **[MODIFY] [AdminAttendanceMonitorScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/AdminAttendanceMonitorScreen.kt)**: Update for "Today's Punch Inspector" and manual override.

### 5. Equipment Tracking
- **[MODIFY] [EquipmentTrackerScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/EquipmentTrackerScreen.kt)**: Pre-populate Leica fleet and add hardware checklist.

### 6. Task Management
- **[MODIFY] [TaskManagementScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/TaskManagementScreen.kt)**: Add Department tabs (Survey/Marketing) and priority/site location fields.

### 7. Coordinate Transformation
- **[MODIFY] [SurveyCalculatorScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/SurveyCalculatorScreen.kt)**: Add UTM Zone 44N conversion and Polygon Area/Perimeter calculator.
- **[MODIFY] [CoordinateConverter.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/CoordinateConverter.kt)**: Implement WGS84 to UTM 44N logic.

### 8. PDF Exports with Seal
- **[MODIFY] [PdfExporter.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/PdfExporter.kt)**: Add `R.drawable.seal_signature` to PDF generation.

### 9. Chat & Live Radar
- **[MODIFY] [ChatHubScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/ChatHubScreen.kt)**: Optimistic rendering and file attachments.
- **[MODIFY] [LiveLocationRadarScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/LiveLocationRadarScreen.kt)**: Add "Open in Google Maps" Intent.

### 10. Branding & Persistence
- **[MODIFY] [SplashScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/SplashScreen.kt)**: Add branding and animation.
- **[MODIFY] [SessionManager.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/SessionManager.kt)**: Ensure role and credentials are persisted.

## Verification Plan

### Automated Tests
- Run `gradle assembleDebug` to ensure compilation.
- Unit tests for `CoordinateConverter`.

### Manual Verification
- Deploy to device/emulator.
- Test Login/Signup flow.
- Test Attendance capture with watermark.
- Test Expense submission.
- Verify PDF generation layout.
