# Firebase Integration and Architecture Overhaul

This plan details the full implementation of Firebase services and the repair of the application architecture across all modules.

## User Review Required

> [!IMPORTANT]
> The application will transition from local/mock data to a real Firebase-backed infrastructure. This requires valid `google-services.json` (already present) and eventually manual configuration of the Firebase Console (Firestore rules, indexes, storage buckets).

## Proposed Changes

### 1. Data Layer & Repositories

Create/Refactor repositories to handle Firebase interactions.

- `AuthRepository`: Handles Firebase Auth (Login, Signup, Sign out, Session).
- `UserRepository`: Manages User profiles in Firestore (`users` collection).
- `AttendanceRepository`: Manages attendance logs, geofencing checks, and selfie uploads.
- `ProjectRepository`: CRUD operations for Projects.
- `EmployeeRepository`: Admin-side employee management.
- `ExpenseRepository`: Expense logging and receipt uploads.
- `BillingRepository`: Syncing Room-based billing with Firestore and Storage.
- `DSRRepository`: Daily Status Reports with photo support.
- `EquipmentRepository`: Equipment tracking and assignments.
- `TaskRepository`: Task management.
- `ClientRepository`: Client database.
- `StorageRepository`: Generic Firebase Storage handler for images/PDFs.

### 2. ViewModels & DI

Update all ViewModels to use the new repositories via proper `ViewModelFactory`.

- `DashboardViewModel`
- `AuthViewModel`
- `AttendanceViewModel`
- `AdminViewModel`
- `BillingViewModel`
- `ExpenseViewModel`
- and others...

### 3. Authentication Flow

- Implement full login/signup logic in `AuthViewModel`.
- Redirect new signups to a "Pending Approval" state.
- Implement session restoration in `MainActivity`.

### 4. Firestore Schema Implementation

Standardize model classes to be compatible with Firestore.

- `User`: UID-keyed profile.
- `Attendance`: Record with location and image URL.
- `Project`, `Expense`, `Invoice`, `DSR`, etc.

### 5. Module-Specific Repairs

- **Attendance**: Integrate `LocationHelper` and `CameraX` with `AttendanceRepository`. Implement geofencing logic.
- **Admin Hub**: Create the interface for approving users and managing roles.
- **Dashboard**: Aggregate Firestore data (e.g., counting active projects, today's attendance).
- **Billing**: Ensure Room persistence for offline and Firestore sync for cloud.
- **Survey**: Retain mathematical logic while adding cloud backup for calculations.

### 6. Navigation & UI Audit

- Verify all `navController.navigate` calls.
- Replace all "TODO", "Not Supported", and "Placeholder" UI with functional components.

## Verification Plan

### Automated Tests
- Build success verification: `gradlew assembleDebug`.
- Unit tests for Repository logic (mocking Firebase).

### Manual Verification
- Deploy to device/emulator.
- Verify Login/Signup flow.
- Verify Attendance marking with location/camera.
- Verify data persistence in Firestore (simulated via repository calls).
- Verify Navigation through all modules.
