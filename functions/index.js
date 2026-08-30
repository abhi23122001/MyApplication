const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
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
  if (recipients.length === 0) return { deliveryStatus: "NO_RECIPIENT", recipientCount: 0, tokenCount: 0, successCount: 0, failureCount: 0 };

  const tokenDocs = await Promise.all(recipients.map((uid) => db.collection("users").doc(uid).get()));
  const tokens = tokenDocs.map((doc) => String(doc.get("fcmToken") || "").trim()).filter(Boolean);
  if (tokens.length === 0) return { deliveryStatus: "NO_FCM_TOKEN", recipientCount: recipients.length, tokenCount: 0, successCount: 0, failureCount: 0 };

  const response = await getMessaging().sendEachForMulticast({
    tokens,
    notification: { title, body: message },
    data: {
      notificationId,
      type: String(data.type || "GENERAL"),
      referenceId: String(data.referenceId || notificationId),
      route: String(data.route || "")
    },
    android: { priority: "high" }
  });

  if (options.persistCopies !== false) {
    const batch = db.batch();
    recipients.forEach((uid) => {
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
  return { deliveryStatus: "SENT", recipientCount: recipients.length, tokenCount: tokens.length, successCount: response.successCount, failureCount: response.failureCount };
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
