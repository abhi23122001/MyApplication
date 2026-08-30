# Enterprise Survey ERP for "Shah Surveyors & Consultancy"

Complete build of the Survey ERP app with Google Sheet integration, fleet tracking, task management, survey tools, and official branding.

## User Review Required

> [!IMPORTANT]
> - **Missing Assets**: `R.drawable.seal_signature` is not found in the project. I will use a placeholder or text-based seal for PDF generation until the asset is provided.
> - **Webhook URL**: The provided URL will be used in `NetworkClient.kt`.
> - **Package Cleanup**: MISPLACED `ui/` folder at `src/main/java/ui/` will be deleted. Package structure will be strictly followed.

## Proposed Changes

---

### Data & Network Layer

#### [MODIFY] [AppScriptModels.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/AppScriptModels.kt)
Consolidate all data models (`User`, `Equipment`, `Task`, `Attendance`, `KPI`, `Payloads`) into this file for a cleaner structure.

#### [NEW] [NetworkClient.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/NetworkClient.kt)
Define Retrofit service and OkHttpClient with the provided Google Apps Script URL.

#### [MOVE] [SessionManager.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/SessionManager.kt)
Move from `utils/` to `data/` and ensure persistent auto-login using DataStore.

---

### Presentation Layer (Screens)

#### [MODIFY] [EquipmentTrackerScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/EquipmentTrackerScreen.kt)
- Pre-populate fleet: 1x Leica GS16, 8x TS04 Total Stations, AutoLevels.
- Implement Full Admin CRUD (Add/Edit/Handover/Delete) with Sheet sync.

#### [MODIFY] [TaskManagementScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/TaskManagementScreen.kt)
- Add Tabs: All, Survey Fieldwork, Marketing CRM.
- Implement assignment with Priority, Deadline, and Status updates (`action: ASSIGN_TASK`).

#### [MODIFY] [SurveyCalculatorScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/SurveyCalculatorScreen.kt)
- Implement WGS84 <-> UTM Zone 44N transformation.
- Add Helmert 2D transformation for local grids.
- Implement Polygon Area Calculator (Acres/Hectares).

#### [MODIFY] [ChatHubScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/ChatHubScreen.kt)
- Support Community Broadcast and Direct Messaging.
- Optimistic UI updates for chat bubbles.
- Google Drive attachment support.

#### [MODIFY] [LiveLocationRadarScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/LiveLocationRadarScreen.kt)
- Real-time tracking list with battery % and Google Maps Intent integration.

#### [MODIFY] [AdminDashboardScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/AdminDashboardScreen.kt)
- Universal Attendance monitoring with override dialogs.
- Daily KPI logging (`action: RECORD_DAILY_KPI`).

---

### Utilities

#### [NEW] [CoordinateConverter.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/CoordinateConverter.kt)
Math logic for UTM Zone 44N and local grid shifts.

#### [NEW] [PdfGeneratorWithSeal.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/PdfGeneratorWithSeal.kt)
PDF generation using `PdfDocument` with official seal overlay.

---

### UI & Branding

#### [MODIFY] [MainActivity.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/MainActivity.kt)
- Setup Navigation and ensure TopAppBar shows "Shah Surveyors & Consultancy" with `app_logo`.

#### [MODIFY] [SplashScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/SplashScreen.kt)
- Display `app_logo` prominently.

## Verification Plan

### Automated Tests
- Build project using Gradle to ensure 0 compilation errors.
- Run `app:assembleDebug` to verify APK generation.

### Manual Verification
- Deploy to device/emulator.
- Test "Punch In" with GPS simulation.
- Test "Leica GS16 UTM Transformation" with known coordinates.
- Verify PDF export generates a file in external storage.
