/**
 * Shah Surveyors ERP - Firebase Cloud Functions (Blaze Plan)
 * Automatically syncs Firestore data in Real-Time to Google Sheets
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();

const GOOGLE_SCRIPT_WEBHOOK_URL =
  "https://script.google.com/macros/s/AKfycbxIkez5x0tAb7eSp2FgWBn43u-RKlz6Z997IHR7DtyqnblfIBOWBpeXRkSs1r8m6tfK/exec";

// 1. ATTENDANCE PUNCH SYNC
exports.onAttendanceCreated = functions.firestore
  .document("attendance/{docId}")
  .onCreate(async (snap, context) => {
    const data = snap.data();
    try {
      const lat = data.lat || data.Latitude || 0;
      const lng = data.lng || data.Longitude || 0;
      const mapsUrl = lat && lng ? `https://www.google.com/maps?q=${lat},${lng}` : "";

      await axios.post(GOOGLE_SCRIPT_WEBHOOK_URL, {
        action: "ATTENDANCE_PUNCH",
        date: data.date || new Date().toISOString().slice(0, 10),
        time: data.time || "",
        staffName: data.staffName || data.name || "Employee",
        EmployeeName: data.staffName || data.name || "Employee",
        EmployeeID: data.employeeId || data.empId || data.uid || "EMP001",
        punchType: data.type || data.action || "PUNCH",
        type: data.type || data.action || "PUNCH",
        workArea: data.workArea || data.siteName || "",
        siteName: data.workArea || data.siteName || "",
        lat: lat.toString(),
        lng: lng.toString(),
        googleMapsUrl: mapsUrl,
        mapsUrl: mapsUrl,
        status: data.status || "PRESENT"
      });
      console.log(`Synced attendance for ${data.staffName} to Google Sheet`);
    } catch (err) {
      console.error("Attendance sync error:", err.message);
    }
  });

// 2. EXPENSE CLAIM SYNC
exports.onExpenseCreated = functions.firestore
  .document("expenses/{docId}")
  .onWrite(async (change, context) => {
    const data = change.after.exists ? change.after.data() : null;
    if (!data) return;

    try {
      await axios.post(GOOGLE_SCRIPT_WEBHOOK_URL, {
        action: "EXPENSE_SYNC",
        date: data.date || new Date().toISOString().slice(0, 10),
        staffName: data.employeeName || data.name || "",
        EmployeeID: data.employeeId || data.employeeUid || "",
        title: data.title || "",
        category: data.category || "GENERAL",
        amount: data.amount || 0,
        status: data.status || "PENDING",
        receiptUrl: data.receiptUrl || ""
      });
      console.log(`Synced expense ${data.title} to Google Sheet`);
    } catch (err) {
      console.error("Expense sync error:", err.message);
    }
  });

// 3. LEAVE REQUEST SYNC
exports.onLeaveCreated = functions.firestore
  .document("leaves/{docId}")
  .onWrite(async (change, context) => {
    const data = change.after.exists ? change.after.data() : null;
    if (!data) return;

    try {
      await axios.post(GOOGLE_SCRIPT_WEBHOOK_URL, {
        action: "LEAVE_SYNC",
        staffName: data.employeeName || "",
        EmployeeID: data.employeeId || "",
        startDate: data.startDate || "",
        endDate: data.endDate || "",
        totalDays: data.totalDays || 1,
        leaveType: data.leaveType || "CASUAL",
        reason: data.reason || "",
        status: data.status || "PENDING"
      });
      console.log(`Synced leave request for ${data.employeeName} to Google Sheet`);
    } catch (err) {
      console.error("Leave sync error:", err.message);
    }
  });

// 4. ADVANCE SALARY REQUEST SYNC
exports.onAdvanceSalaryWritten = functions.firestore
  .document("advanceSalaryRequests/{docId}")
  .onWrite(async (change, context) => {
    const data = change.after.exists ? change.after.data() : null;
    if (!data) return;

    try {
      await axios.post(GOOGLE_SCRIPT_WEBHOOK_URL, {
        action: "ADVANCE_SALARY_SYNC",
        staffName: data.employeeName || "",
        EmployeeID: data.employeeId || "",
        requestedAmount: data.requestedAmount || 0,
        approvedAmount: data.approvedAmount || 0,
        installments: data.installments || 1,
        requestedMonth: data.requestedMonth || "",
        reason: data.reason || "",
        status: data.status || "PENDING"
      });
      console.log(`Synced advance request for ${data.employeeName} to Google Sheet`);
    } catch (err) {
      console.error("Advance salary sync error:", err.message);
    }
  });

// 5. PAYROLL RECORD SYNC
exports.onPayrollRecordWritten = functions.firestore
  .document("payrollRecords/{docId}")
  .onWrite(async (change, context) => {
    const data = change.after.exists ? change.after.data() : null;
    if (!data) return;

    try {
      await axios.post(GOOGLE_SCRIPT_WEBHOOK_URL, {
        action: "SYNC_PAYROLL",
        month: data.salaryMonth || "",
        monthName: data.monthName || "",
        employeeName: data.name || "",
        employeeId: data.employeeId || "",
        department: data.dept || "",
        role: data.role || "",
        baseMonthlySalary: data.baseMonthlySalary || 0,
        dailyRate: data.dailyRate || 0,
        totalDaysInMonth: data.totalDaysInMonth || 30,
        workingDaysInMonth: data.workingDaysInMonth || 26,
        presentDays: data.presentDays || 0,
        halfDays: data.halfDays || 0,
        approvedLeaveDays: data.approvedLeaveDays || 0,
        absentDays: data.absentDays || 0,
        grossSalaryEarned: data.grossSalaryEarned || 0,
        advanceDeduction: data.advanceDeduction || 0,
        absenceDeduction: data.absenceDeduction || 0,
        netSalary: data.netSalary || 0,
        status: data.status || "CALCULATED"
      });
      console.log(`Synced payroll record for ${data.name} to Google Sheet`);
    } catch (err) {
      console.error("Payroll sync error:", err.message);
    }
  });
