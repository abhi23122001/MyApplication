# Implementation Plan: Shah Surveyors & Consultancy ERP

Build a clean production Enterprise Survey ERP with real-time Google Sheets sync, RBAC, and automated data seeding.

## User Review Required

> [!IMPORTANT]
> The application uses a Google Apps Script Webhook as a backend. Ensure the Apps Script is deployed as a Web App with "Anyone" access to allow the Android app to communicate with it.

> [!WARNING]
> PDF generation will use Android's native `PdfDocument` API to keep the app lightweight, which requires careful layout measurement.

## Proposed Changes

### Core Infrastructure

#### [NEW] [DataSeederManager.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/DataSeederManager.kt)
Handles initial demo data injection into the Google Sheet via the Webhook.

#### [MODIFY] [WebhookApi.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/network/WebhookApi.kt)
Expand to include `FETCH_ALL_SYNC_DATA` and specific mutation actions for all modules.

#### [NEW] [AppRepository.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/AppRepository.kt)
Single source of truth for all data, managing sync and optimistic UI updates.

---

### Authentication & RBAC

#### [MODIFY] [SessionManager.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/data/SessionManager.kt)
Store user roles and permissions locally for instant access control.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/auth/LoginScreen.kt)
Implement direct Google Sheet row validation.

---

### UI & UX Modules

#### [MODIFY] [SplashScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/splash/SplashScreen.kt)
Enhance with high-tech glowing pulse animation and metallic branding.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/dashboard/DashboardScreen.kt)
Implement 2-column glassmorphic grid with role-based visibility.

#### [NEW] [GlobalAsyncLoader.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/components/GlobalAsyncLoader.kt)
A reusable frosted glass overlay for network operations.

---

### Feature Modules

#### [NEW] [BillingDocumentGenerator.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/BillingDocumentGenerator.kt)
Utility for generating GST Invoices and Quotations as PDFs with seal overlay.

#### [MODIFY] [AttendanceScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/attendance/AttendanceScreen.kt)
Integrate watermarked selfie capture (CameraX + GPS + Timestamp).

#### [NEW] [GeodeticCalculator.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/GeodeticCalculator.kt)
Implementation of UTM Zone 44N and 2D Base-Shift logic.

## Verification Plan

### Automated Tests
- Unit tests for `GeodeticCalculator` to verify coordinate transformations.
- Repository tests to verify optimistic update logic.

### Manual Verification
- **Splash Screen**: Verify animation smoothness on device.
- **Data Seeding**: Trigger seeder from Admin settings and check Google Sheet for correct entries.
- **Attendance**: Capture selfie and verify the resulting image has the correct watermark details.
- **Billing**: Generate a sample GST invoice and verify tax calculations (CGST/SGST/IGST).
- **Sync**: Edit a row in Google Sheet and verify app updates after swipe-to-refresh.
