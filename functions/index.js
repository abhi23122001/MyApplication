const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated, onDocumentWritten } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();
const adminAuth = getAuth();

const EMPLOYEE_ACCESS = new Set([
  "ATTENDANCE", "TASKS", "CHAT", "LEAVE", "EXPENSE", "SALARY", "ADVANCE", "DSR",
  "SURVEY", "MARKETING", "REPORTS"
]);
const DEFAULT_EMPLOYEE_ACCESS = "ATTENDANCE,TASKS,CHAT,LEAVE,EXPENSE,SALARY,ADVANCE,DSR";

async function requireActiveAdmin(callerUid) {
  if (!callerUid) throw new HttpsError("unauthenticated", "Authentication required");
  const caller = await db.collection("users").doc(callerUid).get();
  const callerData = caller.data() || {};
  if (callerData.active !== true || String(callerData.role || "").trim().toUpperCase() !== "ADMIN") {
    throw new HttpsError("permission-denied", "Admin authorization required");
  }
}

function normalizeAccess(access, fallback = DEFAULT_EMPLOYEE_ACCESS) {
  const raw = String(access ?? "").trim();
  if (!raw) return fallback;
  const values = raw.split(/[,;|]/).map((value) => value.trim().toUpperCase()).filter(Boolean);
  return [...new Set(values)].join(",");
}

function normalizeEmployeeAccess(access) {
  const raw = String(access ?? "").trim();
  if (!raw) return DEFAULT_EMPLOYEE_ACCESS;
  const values = raw.split(/[,;|]/)
    .map((value) => value.trim().toUpperCase())
    .filter((value) => EMPLOYEE_ACCESS.has(value));
  return [...new Set(values)].join(",");
}

async function resolveRecipients(data) {
  const directUid = String(data.recipientUid || "").trim();
  if (directUid) return [directUid];
  const targetRole = String(data.targetRole || "").trim().toLowerCase();
  const target = String(data.target || "").trim();
  const normalizedTarget = target.toUpperCase();

  if (targetRole === "admin" || normalizedTarget === "ADMIN") {
    const users = await db.collection("users").get();
    return users.docs.filter((doc) => String(doc.get("role") || "").trim().toLowerCase() === "admin").map((doc) => doc.id).filter(Boolean);
  }
  if (normalizedTarget === "ALL") {
    const users = await db.collection("users").get();
    return users.docs.filter((doc) => doc.get("active") !== false).map((doc) => doc.id).filter(Boolean);
  }
  if (normalizedTarget === "EMPLOYEE") {
    const users = await db.collection("users").get();
    return users.docs.filter((doc) => doc.get("active") !== false && String(doc.get("role") || "").trim().toLowerCase() !== "admin").map((doc) => doc.id).filter(Boolean);
  }
  if (normalizedTarget.startsWith("USER:")) {
    const uid = target.substring(target.indexOf(":") + 1).trim();
    return uid ? [uid] : [];
  }
  return [];
}

async function sendNotification(data, sourceId, options = {}) {
  const recipients = [...new Set(await resolveRecipients(data))];
  const title = String(data.title || "Shah ERP");
  const message = String(data.message || "New notification");
  const notificationId = String(sourceId || "");
  if (recipients.length === 0) {
    return { deliveryStatus: "NO_RECIPIENT", recipientCount: 0, tokenCount: 0, successCount: 0, failureCount: 0 };
  }

  const tokenDocs = await Promise.all(recipients.map((uid) => db.collection("users").doc(uid).get()));
  const recipientTokens = tokenDocs
    .map((doc) => ({ uid: doc.id, token: String(doc.get("fcmToken") || "").trim() }))
    .filter((item) => item.token);

  if (recipientTokens.length === 0) {
    return { deliveryStatus: "NO_FCM_TOKEN", recipientCount: recipients.length, tokenCount: 0, successCount: 0, failureCount: 0 };
  }

  let successCount = 0;
  let failureCount = 0;
  for (let i = 0; i < recipientTokens.length; i += 500) {
    const chunk = recipientTokens.slice(i, i + 500);
    const response = await getMessaging().sendEachForMulticast({
      tokens: chunk.map((item) => item.token),
      notification: { title, body: message },
      data: {
        notificationId,
        type: String(data.type || "GENERAL"),
        referenceId: String(data.referenceId || notificationId),
        route: String(data.route || "")
      },
      android: { priority: "high" }
    });
    successCount += response.successCount;
    failureCount += response.failureCount;
  }

  if (options.persistCopies !== false) {
    // Firestore batch writes are limited to 500 operations.
    for (let i = 0; i < recipients.length; i += 500) {
      const chunk = recipients.slice(i, i + 500);
      const batch = db.batch();
      chunk.forEach((uid) => {
        const ref = db.collection("notifications").doc();
        batch.set(ref, {
          type: String(data.type || "GENERAL"), title, message,
          actorUid: String(data.actorUid || ""), actorName: String(data.actorName || ""),
          referenceId: String(data.referenceId || notificationId), route: String(data.route || ""),
          recipientUid: uid, read: false, fanout: true, sourceNotificationId: notificationId,
          createdAt: data.createdAt || FieldValue.serverTimestamp()
        });
      });
      await batch.commit();
    }
  }

  return {
    deliveryStatus: "SENT",
    recipientCount: recipients.length,
    tokenCount: recipientTokens.length,
    successCount,
    failureCount
  };
}

exports.sendShahErpNotification = onDocumentCreated("notifications/{notificationId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;
  const data = snapshot.data() || {};
  if (data.fanout === true) return;
  const notificationId = event.params.notificationId;
  const result = await sendNotification(data, notificationId);
  await snapshot.ref.set({ ...result, deliveredAt: FieldValue.serverTimestamp() }, { merge: true });
  logger.info("Shah ERP notification processed", { notificationId, ...result });
});

exports.sendShahErpAnnouncement = onDocumentCreated("announcements/{announcementId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;
  const data = snapshot.data() || {};
  const announcementId = event.params.announcementId;
  const result = await sendNotification({ ...data, type: "ANNOUNCEMENT", referenceId: announcementId, route: "announcements" }, announcementId);
  await snapshot.ref.set({ deliveryStatus: result.deliveryStatus, recipientCount: result.recipientCount, tokenCount: result.tokenCount, successCount: result.successCount, failureCount: result.failureCount, deliveredAt: FieldValue.serverTimestamp() }, { merge: true });
  logger.info("Shah ERP announcement processed", { announcementId, ...result });
});

exports.createEmployeeAccountAsAdmin = onCall(async (request) => {
  const callerUid = request.auth?.uid;
  await requireActiveAdmin(callerUid);

  const data = request.data || {};
  const name = String(data.name || "").trim();
  const email = String(data.email || "").trim().toLowerCase();
  const password = String(data.password || "");
  const department = String(data.department || "SURVEY").trim().toUpperCase();
  const access = normalizeEmployeeAccess(data.access);

  if (!name || !email || !email.includes("@") || !password || password.length < 6 || !department) {
    throw new HttpsError("invalid-argument", "Valid employee name, email, password and department are required");
  }

  let createdUid = "";
  try {
    const existing = await adminAuth.getUserByEmail(email).catch((error) => {
      if (error.code === "auth/user-not-found") return null;
      throw error;
    });
    if (existing) throw new HttpsError("already-exists", "An account already exists for this email");

    const userRecord = await adminAuth.createUser({ email, password, displayName: name });
    createdUid = userRecord.uid;

    await db.collection("users").doc(createdUid).set({
      uid: createdUid, name, email, role: "employee", department, access,
      approved: true, active: true,
      createdAt: FieldValue.serverTimestamp(), updatedAt: FieldValue.serverTimestamp()
    }, { merge: true });

    const profile = await db.collection("users").doc(createdUid).get();
    if (!profile.exists || String(profile.get("uid") || "") !== createdUid || String(profile.get("role") || "").toLowerCase() !== "employee") {
      throw new Error("Employee profile verification failed");
    }
    return { success: true, uid: createdUid };
  } catch (error) {
    if (createdUid) {
      try { await db.collection("users").doc(createdUid).delete(); } catch (cleanupError) { logger.error("Employee profile cleanup failed", cleanupError); }
      try { await adminAuth.deleteUser(createdUid); } catch (cleanupError) { logger.error("Employee Auth cleanup failed", cleanupError); }
    }
    if (error instanceof HttpsError) throw error;
    logger.error("Employee account creation failed", error);
    throw new HttpsError("internal", "Unable to create employee account");
  }
});

exports.saveEmployeeProfileAsAdmin = onCall(async (request) => {
  const callerUid = request.auth?.uid;
  await requireActiveAdmin(callerUid);

  const data = request.data || {};
  const uid = String(data.uid || "").trim();
  const name = String(data.name || "").trim();
  const department = String(data.department || "SURVEY").trim().toUpperCase();
  const requestedEmail = String(data.email || "").trim().toLowerCase();
  const access = normalizeEmployeeAccess(data.access);

  if (!uid || !name || !requestedEmail || !requestedEmail.includes("@")) {
    throw new HttpsError("invalid-argument", "Employee uid, name and email are required");
  }
  if (uid === callerUid) throw new HttpsError("invalid-argument", "An admin cannot create a profile for their own UID");

  let authUser;
  try {
    authUser = await adminAuth.getUser(uid);
  } catch (error) {
    throw new HttpsError("failed-precondition", "Firebase Authentication user does not exist");
  }

  const authEmail = String(authUser.email || "").trim().toLowerCase();
  if (!authEmail || authEmail !== requestedEmail) {
    throw new HttpsError("invalid-argument", "Employee email does not match Firebase Authentication");
  }

  const existingProfile = await db.collection("users").doc(uid).get();
  if (existingProfile.exists && String(existingProfile.get("role") || "").trim().toUpperCase() === "ADMIN") {
    throw new HttpsError("permission-denied", "An existing Admin profile cannot be converted to Employee");
  }

  await db.collection("users").doc(uid).set({
    uid, name, email: authEmail, role: "employee", department, access,
    approved: true, active: true, updatedAt: FieldValue.serverTimestamp()
  }, { merge: true });

  return { success: true, uid };
});


const SHEETS_WEBHOOK_URL = "https://script.google.com/macros/s/AKfycbxIkez5x0tAb7eSp2FgWBn43u-RKlz6Z997IHR7DtyqnblfIBOWBpeXRkSs1r8m6tfK/exec";
const SHEETS_WEBHOOK_KEY = defineSecret("SHEETS_WEBHOOK_KEY");

exports.syncGoogleSheets = require("firebase-functions/v2/https").onRequest({ secrets: [SHEETS_WEBHOOK_KEY] }, async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).json({ status: "ERROR", message: "POST required" });
  }

  try {
    const authorization = String(req.get("authorization") || "");
    const match = authorization.match(/^Bearer\s+(.+)$/i);
    if (!match) return res.status(401).json({ status: "ERROR", message: "Authentication required" });

    const decoded = await adminAuth.verifyIdToken(match[1]);
    await requireActiveAdmin(decoded.uid);

    const response = await fetch(SHEETS_WEBHOOK_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json", "X-ERP-Webhook-Key": SHEETS_WEBHOOK_KEY.value() },
      body: JSON.stringify({ ...(req.body || {}), webhookKey: SHEETS_WEBHOOK_KEY.value() })
    });

    const text = await response.text();
    let body;
    try {
      body = JSON.parse(text);
    } catch (_) {
      body = { status: "ERROR", message: "Invalid response from Sheets backend" };
    }

    if (!response.ok || body.status !== "SUCCESS") {
      logger.error("Google Sheets sync rejected", {
        httpStatus: response.status,
        responseStatus: body.status
      });
      return res.status(502).json({
        status: "ERROR",
        message: body.message || "Google Sheets sync failed"
      });
    }

    return res.status(200).json(body);
  } catch (error) {
    if (error.code === "auth/id-token-expired" || error.code === "auth/argument-error") {
      return res.status(401).json({ status: "ERROR", message: "Invalid authentication token" });
    }
    if (error instanceof HttpsError && error.code === "permission-denied") {
      return res.status(403).json({ status: "ERROR", message: error.message });
    }
    logger.error("Google Sheets proxy failed", error);
    return res.status(500).json({ status: "ERROR", message: "Google Sheets sync unavailable" });
  }
});


// -------------------------------------------------------------------------
// REAL-TIME FIRESTORE -> GOOGLE SHEETS SYNC
// -------------------------------------------------------------------------

function sheetsDate_(value) {
  if (!value) return "";
  if (typeof value === "string") return value;
  if (value.toDate) {
    const d = value.toDate();
    return new Intl.DateTimeFormat("en-CA", {
      timeZone: "Asia/Kolkata", year: "numeric", month: "2-digit", day: "2-digit"
    }).format(d);
  }
  return "";
}

function sheetsTime_(value) {
  if (!value) return "";
  const d = value.toDate ? value.toDate() : (value instanceof Date ? value : null);
  if (!d) return String(value);
  return new Intl.DateTimeFormat("en-IN", {
    timeZone: "Asia/Kolkata", hour: "2-digit", minute: "2-digit", hour12: true
  }).format(d);
}

async function userForSheets_(uid) {
  if (!uid) return {};
  const snap = await db.collection("users").doc(uid).get();
  return snap.exists ? (snap.data() || {}) : {};
}

async function postSheetsAction_(payload) {
  const response = await fetch(SHEETS_WEBHOOK_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...payload, webhookKey: SHEETS_WEBHOOK_KEY.value() })
  });
  const text = await response.text();
  let body = {};
  try { body = JSON.parse(text); } catch (_) {}
  if (!response.ok || body.status !== "SUCCESS") {
    throw new Error(body.message || ("Sheets webhook HTTP " + response.status));
  }
  return body;
}

exports.syncAttendanceToGoogleSheets = onDocumentWritten(
  { document: "attendance/{attendanceId}", secrets: [SHEETS_WEBHOOK_KEY] },
  async (event) => {
    const after = event.data?.after;
    const before = event.data?.before;
    const data = after?.exists ? (after.data() || {}) : (before?.data() || {});
    const uid = String(data.uid || data.employeeUid || data.userUid || "").trim();
    const user = await userForSheets_(uid);
    const empId = String(data.employeeId || user.employeeId || user.empId || uid).trim();
    const empName = String(data.userName || data.staffName || data.employeeName || user.name || "Employee").trim();
    const date = String(data.date || sheetsDate_(data.punchInTime) || "").trim();

    if (!after?.exists) {
      return postSheetsAction_({
        action: "ATTENDANCE_DELETE", EmployeeID: empId, EmployeeName: empName, date
      });
    }

    const punchIn = sheetsTime_(data.punchInTime);
    const punchOut = sheetsTime_(data.punchOutTime);
    const minutes = Number(data.workingMinutes || 0);
    const workingHours = minutes > 0
      ? Math.floor(minutes / 60) + "h " + (minutes % 60) + "m"
      : "";

    return postSheetsAction_({
      action: "ATTENDANCE_SYNC",
      EmployeeID: empId,
      employeeId: empId,
      EmployeeName: empName,
      staffName: empName,
      date,
      status: String(data.status || "PRESENT"),
      punchType: punchOut ? "PUNCH_OUT" : "PUNCH_IN",
      checkIn: punchIn,
      checkOut: punchOut,
      workingHours,
      siteName: String(data.siteName || data.workArea || ""),
      remarks: String(data.remarks || ""),
      lat: String(data.punchInLat || ""),
      lng: String(data.punchInLng || "")
    });
  }
);

exports.syncExpenseToGoogleSheets = onDocumentWritten(
  { document: "expenses/{expenseId}", secrets: [SHEETS_WEBHOOK_KEY] },
  async (event) => {
    const after = event.data?.after;
    const before = event.data?.before;
    const data = after?.exists ? (after.data() || {}) : (before?.data() || {});
    const uid = String(data.uid || data.employeeUid || "").trim();
    const user = await userForSheets_(uid);
    const empId = String(data.employeeId || user.employeeId || user.empId || uid).trim();
    const empName = String(data.userName || data.employeeName || data.staffName || user.name || "Employee").trim();
    const expenseId = event.params.expenseId;

    if (!after?.exists) {
      return postSheetsAction_({
        action: "EXPENSE_DELETE", expenseId, EmployeeID: empId, EmployeeName: empName
      });
    }

    return postSheetsAction_({
      action: "EXPENSE_SYNC",
      expenseId,
      EmployeeID: empId,
      employeeId: empId,
      EmployeeName: empName,
      date: String(data.dateText || data.date || sheetsDate_(data.createdAt) || ""),
      category: String(data.category || ""),
      description: String(data.title || data.description || data.remarks || ""),
      amount: Number(data.amount || 0),
      paymentMode: String(data.paymentMode || ""),
      status: String(data.status || "PENDING"),
      receiptUrl: String(data.receiptUrl || "")
    });
  }
);

exports.syncLeaveToGoogleSheets = onDocumentWritten(
  { document: "leaveRequests/{leaveId}", secrets: [SHEETS_WEBHOOK_KEY] },
  async (event) => {
    const after = event.data?.after;
    if (!after?.exists) return null;
    const data = after.data() || {};
    const uid = String(data.uid || data.employeeUid || "").trim();
    const user = await userForSheets_(uid);
    const empId = String(data.employeeId || user.employeeId || user.empId || uid).trim();
    const empName = String(data.employeeName || data.userName || data.staffName || user.name || "Employee").trim();
    return postSheetsAction_({
      action: "LEAVE_SYNC", leaveId: event.params.leaveId,
      EmployeeID: empId, EmployeeName: empName,
      startDate: String(data.startDate || ""), endDate: String(data.endDate || ""),
      totalDays: Number(data.totalDays || 0), leaveType: String(data.leaveType || ""),
      reason: String(data.reason || ""), status: String(data.status || "PENDING")
    });
  }
);

exports.syncAdvanceToGoogleSheets = onDocumentWritten(
  { document: "advanceSalaryRequests/{advanceId}", secrets: [SHEETS_WEBHOOK_KEY] },
  async (event) => {
    const after = event.data?.after;
    if (!after?.exists) return null;
    const data = after.data() || {};
    const uid = String(data.uid || data.employeeUid || "").trim();
    const user = await userForSheets_(uid);
    const empId = String(data.employeeId || user.employeeId || user.empId || uid).trim();
    const empName = String(data.employeeName || data.userName || user.name || "Employee").trim();
    return postSheetsAction_({
      action: "ADVANCE_SALARY_SYNC", advanceId: event.params.advanceId,
      EmployeeID: empId, EmployeeName: empName,
      requestedMonth: String(data.requestedMonth || ""),
      requestedAmount: Number(data.requestedAmount || 0),
      approvedAmount: Number(data.approvedAmount || 0),
      installments: Number(data.installments || 0),
      reason: String(data.reason || ""), status: String(data.status || "PENDING")
    });
  }
);

exports.syncPayrollToGoogleSheets = onDocumentWritten(
  { document: "payrollRecords/{payrollId}", secrets: [SHEETS_WEBHOOK_KEY] },
  async (event) => {
    const after = event.data?.after;
    if (!after?.exists) return null;
    const data = after.data() || {};
    const uid = String(data.uid || data.employeeUid || "").trim();
    const user = await userForSheets_(uid);
    return postSheetsAction_({
      action: "SYNC_PAYROLL",
      EmployeeID: String(data.employeeId || user.employeeId || user.empId || uid),
      EmployeeName: String(data.employeeName || data.name || user.name || "Employee"),
      month: String(data.salaryMonth || data.month || ""),
      department: String(data.department || data.dept || user.department || ""),
      role: String(data.role || user.role || ""),
      baseMonthlySalary: Number(data.baseMonthlySalary || 0),
      dailyRate: Number(data.dailyRate || 0),
      totalDaysInMonth: Number(data.totalDaysInMonth || 0),
      workingDaysInMonth: Number(data.workingDaysInMonth || 0),
      presentDays: Number(data.presentDays || 0), halfDays: Number(data.halfDays || 0),
      approvedLeaveDays: Number(data.approvedLeaveDays || 0), absentDays: Number(data.absentDays || 0),
      grossSalaryEarned: Number(data.grossSalaryEarned || 0),
      advanceDeduction: Number(data.advanceDeduction || 0),
      absenceDeduction: Number(data.absenceDeduction || 0),
      netSalary: Number(data.netSalary || 0), status: String(data.status || "")
    });
  }
);

exports.syncDsrToGoogleSheets = onDocumentWritten(
  { document: "daily_reports/{dsrId}", secrets: [SHEETS_WEBHOOK_KEY] },
  async (event) => {
    const after = event.data?.after;
    if (!after?.exists) return null;
    const data = after.data() || {};
    const uid = String(data.uid || data.employeeUid || "").trim();
    const user = await userForSheets_(uid);
    return postSheetsAction_({
      action: "DSR_SYNC", dsrId: event.params.dsrId,
      EmployeeID: String(data.employeeId || user.employeeId || user.empId || uid),
      EmployeeName: String(data.employeeName || data.userName || user.name || "Employee"),
      date: String(data.date || ""), chainage: String(data.chainage || ""),
      points: String(data.points || ""), area: String(data.area || ""),
      instrument: String(data.instrument || ""), remarks: String(data.remarks || "")
    });
  }
);
