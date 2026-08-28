const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();

async function resolveRecipients(data) {
  const directUid = String(data.recipientUid || "").trim();
  if (directUid) return [directUid];

  const targetRole = String(data.targetRole || "").trim().toLowerCase();
  const target = String(data.target || "").trim();
  const normalizedTarget = target.toUpperCase();

  if (targetRole === "admin" || normalizedTarget === "ADMIN") {
    const users = await db.collection("users").get();
    return users.docs
      .filter((doc) => String(doc.get("role") || "").trim().toLowerCase() === "admin")
      .map((doc) => doc.id)
      .filter(Boolean);
  }

  if (normalizedTarget === "ALL") {
    const users = await db.collection("users").get();
    return users.docs
      .filter((doc) => doc.get("active") !== false)
      .map((doc) => doc.id)
      .filter(Boolean);
  }

  if (normalizedTarget === "EMPLOYEE") {
    const users = await db.collection("users").get();
    return users.docs
      .filter((doc) => {
        if (doc.get("active") === false) return false;
        return String(doc.get("role") || "").trim().toLowerCase() !== "admin";
      })
      .map((doc) => doc.id)
      .filter(Boolean);
  }

  if (normalizedTarget === "ADMIN") {
    const users = await db.collection("users").get();
    return users.docs
      .filter((doc) => String(doc.get("role") || "").trim().toLowerCase() === "admin")
      .map((doc) => doc.id)
      .filter(Boolean);
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

  const tokenDocs = await Promise.all(
    recipients.map((uid) => db.collection("users").doc(uid).get())
  );
  const tokens = tokenDocs
    .map((doc) => String(doc.get("fcmToken") || "").trim())
    .filter(Boolean);

  if (tokens.length === 0) {
    return { deliveryStatus: "NO_FCM_TOKEN", recipientCount: recipients.length, tokenCount: 0, successCount: 0, failureCount: 0 };
  }

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
        type: String(data.type || "GENERAL"),
        title,
        message,
        actorUid: String(data.actorUid || ""),
        actorName: String(data.actorName || ""),
        referenceId: String(data.referenceId || notificationId),
        route: String(data.route || ""),
        recipientUid: uid,
        read: false,
        fanout: true,
        sourceNotificationId: notificationId,
        createdAt: data.createdAt || FieldValue.serverTimestamp()
      });
    });
    await batch.commit();
  }

  return {
    deliveryStatus: "SENT",
    recipientCount: recipients.length,
    tokenCount: tokens.length,
    successCount: response.successCount,
    failureCount: response.failureCount
  };
}

exports.sendShahErpNotification = onDocumentCreated("notifications/{notificationId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;

  const data = snapshot.data() || {};
  if (data.fanout === true) return;

  const notificationId = event.params.notificationId;
  const result = await sendNotification(data, notificationId);

  await snapshot.ref.set({
    ...result,
    deliveredAt: FieldValue.serverTimestamp()
  }, { merge: true });

  logger.info("Shah ERP notification processed", {
    notificationId,
    ...result
  });
});

exports.sendShahErpAnnouncement = onDocumentCreated("announcements/{announcementId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;

  const data = snapshot.data() || {};
  const announcementId = event.params.announcementId;
  const notificationData = {
    ...data,
    type: "ANNOUNCEMENT",
    referenceId: announcementId,
    route: "announcements"
  };

  const result = await sendNotification(notificationData, announcementId);

  await snapshot.ref.set({
    deliveryStatus: result.deliveryStatus,
    recipientCount: result.recipientCount,
    tokenCount: result.tokenCount,
    successCount: result.successCount,
    failureCount: result.failureCount,
    deliveredAt: FieldValue.serverTimestamp()
  }, { merge: true });

  logger.info("Shah ERP announcement processed", {
    announcementId,
    ...result
  });
});
