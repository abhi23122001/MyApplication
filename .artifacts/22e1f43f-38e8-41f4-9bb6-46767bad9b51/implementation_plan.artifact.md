# Implementation Plan - SSC App Fixes & Enhancements

This plan covers branding updates, dashboard card activations, live location fixes, and chat engine reactivity for "Shah Surveyors & Consultancy".

## Proposed Changes

### 1. App Launcher Icon & Branding
- **[MODIFY] [AndroidManifest.xml](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/AndroidManifest.xml)**: Update `android:label` to "Shah Surveyors".
- **[MODIFY] [ic_launcher.xml](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/res/mipmap-anydpi/ic_launcher.xml)**: Set foreground to `@drawable/app_logo`.
- **[MODIFY] [ic_launcher_round.xml](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/res/mipmap-anydpi/ic_launcher_round.xml)**: Set foreground to `@drawable/app_logo`.

---

### 2. Admin Dashboard Enhancements
- **[MODIFY] [AdminDashboard.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/AdminDashboard.kt)**:
    - Remove the "Initialize All Cloud Sheets & Demo Data" card.
    - Implement `TodayPunchesDialog` and `FinancialSummaryDialog`.
    - Make "Today Punch" and "Net Balance" metrics clickable to open these dialogs.
    - Ensure all navigation cards point to the correct composables.

---

### 3. Live Location & Radar Fix
- **[MODIFY] [LiveLocationTrackerScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/LiveLocationTrackerScreen.kt)**:
    - Integrate `FusedLocationProviderClient` for real GPS capturing.
    - Implement battery level capture using `BatteryManager`.
    - Staff Broadcast: Send real coordinates + battery level to the webhook.
    - Admin Radar: Show surveyors list with battery % and "Open in Google Maps" button.
- **[MODIFY] [MainAppViewModel.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/MainAppViewModel.kt)**:
    - Ensure `shareLiveLocation` correctly sends all required fields in the payload.
    - Add a `StateFlow` for `liveStaffLocations` to track real-time updates from the webhook (simulated or fetched).

---

### 4. Chat Engine Reactivity
- **[MODIFY] [ChatHubScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/ChatHubScreen.kt)**:
    - Implement local optimistic state for instant message display.
    - Update tabs to use "COMMUNITY" (receiverId: "ALL_TEAM") and "DIRECT" (per user).
    - Enhance "Surveillance" tab to show all messages with Drive file links.
- **[MODIFY] [MainAppViewModel.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/presentation/MainAppViewModel.kt)**:
    - Update `sendChatMessage` to add messages locally before API call.
    - Improve message fetching logic to filter by receiver/community.

---

## Verification Plan

### Automated Tests
- Run `gradle build` to ensure 0 compilation errors.
- (Optional) Add unit tests for `MainAppViewModel` location and chat logic.

### Manual Verification
- Deploy to an Android device.
- Verify the app icon and name in the launcher.
- Check if "Today Punch" and "Net Balance" open the new dialogs.
- Test "Start Sharing" in Live Radar and verify location is sent (check logs).
- Send a message in Chat and verify it appears instantly.
- Verify "Open in Google Maps" works for surveyors in the Admin view.
