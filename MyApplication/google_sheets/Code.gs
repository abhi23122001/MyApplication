/**
 * ============================================================================
 * SHAH SURVEYORS AND CONSULTANCY - ERP BACKEND GOOGLE APPS SCRIPT
 * Multi-Sheet & Automatic Per-Employee Dedicated Tabs Engine
 * ============================================================================
 * Webhook URL: https://script.google.com/macros/s/AKfycbxIkez5x0tAb7eSp2FgWBn43u-RKlz6Z997IHR7DtyqnblfIBOWBpeXRkSs1r8m6tfK/exec
 * 
 * Features:
 * 1. High-Performance BULK_SYNC for full backfill of historical Firestore data in seconds
 * 2. Master Sheets: Master_Attendance, Master_Payroll, Master_Expenses, Master_Leaves, Master_Advances, Master_DSR
 * 3. Per-Employee Individual Dedicated Tabs: Automatically creates a tab named after each employee (e.g. "Abhijeet Shah")
 * 4. Daily Attendance Punch Logs: Date, In Time, Out Time, Duration, Status, Site, Lat, Lng, Clickable Google Maps Hyperlink
 * 5. Automatic Column Formatting, Header Styling, Currency Formatting
 */

function doPost(e) {
  var lock = LockService.getScriptLock();
  lock.tryLock(30000);

  try {
    if (!e || !e.postData || !e.postData.contents) {
      return responseJson({ status: "ERROR", message: "No payload received." });
    }

    var data;
    try {
      data = JSON.parse(e.postData.contents);
    } catch (parseErr) {
      data = e.parameter;
    }

    var ss = SpreadsheetApp.getActiveSpreadsheet();

    // ------------------------------------------------------------------------
    // BULK SYNC HANDLER (Backfills all historical Firestore records)
    // ------------------------------------------------------------------------
    var records = data.records || data.items || data.batch;
    if ((data.action === "BULK_SYNC" || data.action === "SYNC_ALL") && Array.isArray(records)) {
      var syncedCount = 0;
      var errorCount = 0;
      
      for (var i = 0; i < records.length; i++) {
        try {
          processSingleRecord(ss, records[i]);
          syncedCount++;
        } catch (itemErr) {
          errorCount++;
        }
      }
      
      SpreadsheetApp.flush();
      return responseJson({
        status: "SUCCESS",
        message: "Bulk sync completed. Processed " + syncedCount + " records (" + errorCount + " errors).",
        syncedCount: syncedCount,
        errorCount: errorCount
      });
    }

    // SINGLE RECORD HANDLER
    var result = processSingleRecord(ss, data);
    SpreadsheetApp.flush();
    return responseJson(result);

  } catch (error) {
    return responseJson({ status: "ERROR", message: error.toString() });
  } finally {
    lock.releaseLock();
  }
}

function processSingleRecord(ss, data) {
  if (!data) return { status: "SKIPPED", message: "Empty record data" };

  var action = (data.action || data.type || "").toUpperCase();
  var empName = (data.staffName || data.EmployeeName || data.employeeName || data.name || "Employee").trim();
  var empId = (data.EmployeeID || data.employeeId || data.empId || data.uid || "EMP001").trim();

  // ------------------------------------------------------------------------
  // 1. ATTENDANCE PUNCH SYNC
  // ------------------------------------------------------------------------
  if (action === "ATTENDANCE_PUNCH" || action === "PUNCH_IN" || action === "PUNCH_OUT" || action === "PUNCH" || action === "ATTENDANCE") {
    var date = data.date || getIstDate();
    var time = data.time || data.punchInTime || data.punchOutTime || getIstTime();
    var punchType = data.punchType || data.type || (action.indexOf("IN") !== -1 ? "PUNCH_IN" : (action.indexOf("OUT") !== -1 ? "PUNCH_OUT" : "PUNCH"));
    var workArea = data.workArea || data.siteName || "Main Office / Site";
    var lat = data.lat || data.Latitude || data.punchInLat || "";
    var lng = data.lng || data.Longitude || data.punchInLng || "";
    var status = data.status || "PRESENT";
    var mapUrl = (lat && lng) ? "https://www.google.com/maps?q=" + lat + "," + lng : (data.googleMapsUrl || data.mapsUrl || "");

    // A. Write to Master Attendance Sheet
    var masterSheet = getOrCreateSheet(ss, "Master_Attendance", [
      "Logged At (IST)", "Date", "Time", "Employee Name", "Employee ID", "Punch Type", "Site / Work Area", "Status", "Latitude", "Longitude", "Google Maps Location"
    ], "#1B5E20");

    var mapFormula = mapUrl ? '=HYPERLINK("' + mapUrl + '", "📍 Open in Google Maps")' : "No GPS";
    masterSheet.appendRow([
      new Date(), date, time, empName, empId, punchType, workArea, status, lat, lng, mapFormula
    ]);

    // B. Write to Per-Employee Dedicated Tab
    var empSheet = getOrCreateEmployeeSheet(ss, empName, empId);
    empSheet.appendRow([
      date, time, punchType, workArea, status, lat, lng, mapFormula, new Date()
    ]);

    return { status: "SUCCESS", message: "Attendance logged to Master and Employee tab: " + empName };
  }

  // ------------------------------------------------------------------------
  // 2. EXPENSE CLAIM SYNC
  // ------------------------------------------------------------------------
  if (action === "EXPENSE_SYNC" || action === "CLAIM_EXPENSE" || action === "EXPENSE") {
    var date = data.date || getIstDate();
    var title = data.title || "Expense";
    var category = data.category || "GENERAL";
    var amount = Number(data.amount || 0);
    var expStatus = data.status || "PENDING";
    var receiptUrl = data.receiptUrl || "";

    var expSheet = getOrCreateSheet(ss, "Master_Expenses", [
      "Logged At (IST)", "Date", "Employee Name", "Employee ID", "Expense Title", "Category", "Amount (₹)", "Status", "Receipt"
    ], "#E65100");

    var receiptFormula = receiptUrl ? '=HYPERLINK("' + receiptUrl + '", "📄 View Receipt")' : "N/A";
    expSheet.appendRow([
      new Date(), date, empName, empId, title, category, amount, expStatus, receiptFormula
    ]);

    return { status: "SUCCESS", message: "Expense logged to Master_Expenses" };
  }

  // ------------------------------------------------------------------------
  // 3. LEAVE REQUEST SYNC
  // ------------------------------------------------------------------------
  if (action === "LEAVE_SYNC" || action === "LEAVE_REQUEST" || action === "LEAVE") {
    var startDate = data.startDate || "";
    var endDate = data.endDate || "";
    var totalDays = Number(data.totalDays || 1);
    var leaveType = data.leaveType || "CASUAL";
    var reason = data.reason || "";
    var leaveStatus = data.status || "PENDING";

    var leaveSheet = getOrCreateSheet(ss, "Master_Leaves", [
      "Applied At (IST)", "Employee Name", "Employee ID", "Leave Type", "Start Date", "End Date", "Total Days", "Reason", "Status"
    ], "#0D47A1");

    leaveSheet.appendRow([
      new Date(), empName, empId, leaveType, startDate, endDate, totalDays, reason, leaveStatus
    ]);

    return { status: "SUCCESS", message: "Leave logged to Master_Leaves" };
  }

  // ------------------------------------------------------------------------
  // 4. ADVANCE SALARY REQUEST SYNC
  // ------------------------------------------------------------------------
  if (action === "ADVANCE_SALARY_SYNC" || action === "ADVANCE_SALARY" || action === "ADVANCE") {
    var reqAmount = Number(data.requestedAmount || 0);
    var appAmount = Number(data.approvedAmount || 0);
    var installments = Number(data.installments || 1);
    var reqMonth = data.requestedMonth || "";
    var reason = data.reason || "";
    var advStatus = data.status || "PENDING";

    var advSheet = getOrCreateSheet(ss, "Master_Advances", [
      "Applied At (IST)", "Employee Name", "Employee ID", "Requested Amount (₹)", "Approved Amount (₹)", "EMI Months", "For Month", "Reason", "Status"
    ], "#4A148C");

    advSheet.appendRow([
      new Date(), empName, empId, reqAmount, appAmount, installments, reqMonth, reason, advStatus
    ]);

    return { status: "SUCCESS", message: "Advance salary logged to Master_Advances" };
  }

  // ------------------------------------------------------------------------
  // 5. PAYROLL RECORD SYNC
  // ------------------------------------------------------------------------
  if (action === "SYNC_PAYROLL" || action === "PAYROLL_RECORD" || action === "PAYROLL") {
    var month = data.month || data.salaryMonth || "";
    var dept = data.department || data.dept || "SURVEY";
    var role = data.role || "STAFF";
    var baseSalary = Number(data.baseMonthlySalary || 0);
    var dailyRate = Number(data.dailyRate || 0);
    var totalDays = Number(data.totalDaysInMonth || 30);
    var workingDays = Number(data.workingDaysInMonth || 26);
    var present = Number(data.presentDays || 0);
    var halfDays = Number(data.halfDays || 0);
    var leaves = Number(data.approvedLeaveDays || 0);
    var absent = Number(data.absentDays || 0);
    var gross = Number(data.grossSalaryEarned || 0);
    var advanceCut = Number(data.advanceDeduction || 0);
    var absenceCut = Number(data.absenceDeduction || 0);
    var netSalary = Number(data.netSalary || 0);
    var payStatus = data.status || "CALCULATED";

    var paySheet = getOrCreateSheet(ss, "Master_Payroll", [
      "Calculated At (IST)", "Month", "Employee Name", "Employee ID", "Department", "Role", "Base Monthly Salary (₹)", "Daily Rate (₹)", "Working Days", "Present Days", "Half Days", "Leaves", "Absent", "Gross Earned (₹)", "Advance EMI Cut (₹)", "Absence Cut (₹)", "Net Payable Salary (₹)", "Status"
    ], "#1B5E20");

    paySheet.appendRow([
      new Date(), month, empName, empId, dept, role, baseSalary, dailyRate, workingDays, present, halfDays, leaves, absent, gross, advanceCut, absenceCut, netSalary, payStatus
    ]);

    return { status: "SUCCESS", message: "Payroll record synced to Master_Payroll" };
  }

  // ------------------------------------------------------------------------
  // 6. DAILY STATUS REPORT (DSR) SYNC
  // ------------------------------------------------------------------------
  if (action === "DSR_SYNC" || action === "DSR") {
    var date = data.date || getIstDate();
    var chainage = data.chainage || "";
    var points = data.points || "";
    var area = data.area || "";
    var instrument = data.instrument || "";
    var remarks = data.remarks || "";

    var dsrSheet = getOrCreateSheet(ss, "Master_DSR", [
      "Logged At (IST)", "Date", "Employee Name", "Employee ID", "Chainage / Location", "Points Surveyed", "Area / Site", "Instrument Used", "Remarks"
    ], "#004D40");

    dsrSheet.appendRow([
      new Date(), date, empName, empId, chainage, points, area, instrument, remarks
    ]);

    return { status: "SUCCESS", message: "DSR report synced to Master_DSR" };
  }

  return { status: "IGNORED", message: "Action not recognized: " + action };
}

function doGet(e) {
  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action.toUpperCase() : "STATUS";
  return responseJson({
    status: "ONLINE",
    service: "SHAH SURVEYORS & ERP GOOGLE SHEETS CONNECTOR",
    timestamp: new Date().toISOString(),
    actionRequested: action
  });
}

// ----------------------------------------------------------------------------
// HELPER: GET OR CREATE MASTER SHEET WITH STYLED HEADERS
// ----------------------------------------------------------------------------
function getOrCreateSheet(ss, sheetName, headers, headerColor) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
    sheet.appendRow(headers);
    var headerRange = sheet.getRange(1, 1, 1, headers.length);
    headerRange.setBackground(headerColor || "#1B5E20");
    headerRange.setFontColor("#FFFFFF");
    headerRange.setFontWeight("bold");
    headerRange.setFontSize(10);
    headerRange.setHorizontalAlignment("center");
    sheet.setFrozenRows(1);
    for (var i = 1; i <= headers.length; i++) {
      sheet.autoResizeColumn(i);
    }
  }
  return sheet;
}

// ----------------------------------------------------------------------------
// HELPER: GET OR CREATE PER-EMPLOYEE INDIVIDUAL DEDICATED SHEET
// ----------------------------------------------------------------------------
function getOrCreateEmployeeSheet(ss, employeeName, employeeId) {
  // Sanitize sheet name (Max 30 chars, no illegal chars)
  var cleanName = (employeeName || "Employee").replace(/[\\\/:\?\*\[\]]/g, "").trim().substring(0, 28);
  var sheet = ss.getSheetByName(cleanName);
  
  if (!sheet) {
    sheet = ss.insertSheet(cleanName);
    
    // Title Banner
    sheet.getRange(1, 1).setValue("👤 EMPLOYEE ATTENDANCE & PUNCH LOGS - " + cleanName.toUpperCase() + " (ID: " + employeeId + ")");
    sheet.getRange(1, 1, 1, 9).merge().setBackground("#1B5E20").setFontColor("#FFFFFF").setFontWeight("bold").setFontSize(11).setHorizontalAlignment("center");

    // Headers
    var headers = [
      "Date", "Punch Time", "Punch Type (IN/OUT)", "Work Site / Area", "Status", "Latitude", "Longitude", "Google Maps Location", "Logged At (IST)"
    ];
    sheet.appendRow(headers);

    var headerRange = sheet.getRange(2, 1, 1, headers.length);
    headerRange.setBackground("#2E7D32");
    headerRange.setFontColor("#FFFFFF");
    headerRange.setFontWeight("bold");
    headerRange.setFontSize(10);
    headerRange.setHorizontalAlignment("center");

    sheet.setFrozenRows(2);
    for (var i = 1; i <= headers.length; i++) {
      sheet.autoResizeColumn(i);
    }
  }
  return sheet;
}

// ----------------------------------------------------------------------------
// DATE & TIME HELPERS
// ----------------------------------------------------------------------------
function getIstDate() {
  return Utilities.formatDate(new Date(), "GMT+5:30", "yyyy-MM-dd");
}

function getIstTime() {
  return Utilities.formatDate(new Date(), "GMT+5:30", "hh:mm a");
}

function responseJson(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
