# Implementation Plan - Shah Surveyors & Consultancy Enterprise ERP

Rebuild and finalize the ERP system with high-contrast UI, 10 exhaustive modules, and real-time Google Sheets sync.

## User Review Required

> [!IMPORTANT]
> The PDF generation logic requires `iText` or a similar library. I will check the `build.gradle` to see if it's already included.
> Watermarking images for attendance requires custom canvas manipulation or a library.
> WGS84 to UTM transformation will use a mathematical implementation or a library if available.

## Proposed Changes

### Core Configuration & Assets

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/AndroidManifest.xml)
- Update application icons to `@drawable/app_logo`.
- Ensure all permissions are present.

#### [MODIFY] [AppConfig.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/config/AppConfig.kt)
- Verify Webhook, Drive Folder ID, and Spreadsheet ID.

---

### UI/UX & Theme (High Contrast)

#### [MODIFY] [Color.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/theme/Color.kt)
- Define Deep Midnight Slate (`#0B132B`) and Rich Slate (`#1E293B`).

#### [MODIFY] [Theme.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/theme/Theme.kt)
- Update `ShahTheme` to use the high-contrast color scheme.
- Ensure all Card backgrounds default to Rich Slate and text to White Bold.

#### [MODIFY] [SplashScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/splash/SplashScreen.kt)
- Refine animations with glowing scale-up pulse and metallic gradient text.

#### [NEW] [LoadingOverlay.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/components/LoadingOverlay.kt)
- Global frosted glass async loader.

---

### Dashboard & Navigation

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/dashboard/DashboardScreen.kt)
- Implement Header with greeting, Role Chip, and Live IST Clock.
- Add real-time Notice Board banner.
- Implement 2-column icon grid for modules.
- Add `SwipeRefreshLayout` support.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/MainActivity.kt)
- Update navigation routes and ensure all viewmodels are correctly initialized.

---

### Modules Implementation

#### [MODIFY] [AdminHubScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/admin/AdminHubScreen.kt)
- Create User form and Gatekeeper logic.

#### [MODIFY] [ExpenseScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/ops/ExpenseClaimsScreen.kt)
- Implement categories, remarks, and receipt upload with compression.

#### [MODIFY] [BillingScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/billing/BillingScreen.kt)
- Implement PDF generation, preview (Intent), and sharing logic.
- Integrate `BillingDocumentGenerator.kt`.

#### [MODIFY] [AttendanceScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/attendance/AttendanceScreen.kt)
- Front-camera selfie with hardcoded watermark and GPS stamping.

#### [MODIFY] [EquipmentScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/equipment/EquipmentTrackerScreen.kt)
- Full CRUD for Leica fleet and handover checklists.

#### [NEW] [CRMClientScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/crm/CRMClientScreen.kt)
- Zoho-style client address book.

#### [MODIFY] [TaskScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/tasks/TaskManagementScreen.kt)
- Task assignment with priority tags and proof uploads.

#### [MODIFY] [DSRScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/ops/DailyStatusReportScreen.kt)
- Detailed field logger for DSR.

#### [MODIFY] [CalculatorScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/survey/SurveyCalculatorScreen.kt)
- WGS84/UTM conversion and area calculator.

#### [MODIFY] [RadarScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/chat/RadarScreen.kt)
- Real-time GPS tracking map and messaging.

---

### Sync Engine

#### [MODIFY] [AppRepository.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/AppRepository.kt)
- Implement `FETCH_ALL_SYNC_DATA` and optimistic UI updates.

## Verification Plan

### Automated Tests
- `gradle build` to ensure zero compilation errors.

### Manual Verification
- Deploy to device/emulator.
- Verify high-contrast theme on all screens.
- Test PDF generation and preview.
- Test Attendance selfie with watermark.
- Verify GPS tracking on Radar screen.
