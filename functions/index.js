const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();

exports.sendShahErpNotification = onDocumentCreated("notifications/{notificationId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;

  const data = snapshot.data() || {};
  const title = String(data.title || "Shah ERP");
  const message = String(data.message || "New notification");
  const notificationId = event.params.notificationId;

  let recipients = [];
  if (String(data.recipientUid || "").trim()) {
    recipients.push(String(data.recipientUid).trim());
  } else if (String(data.targetRole || "").toLowerCase() === "admin") {
    const admins = await db.collection("users").where("role", "==", "admin").get();
    recipients = admins.docs.map((doc) => doc.id).filter(Boolean);
  }

  recipients = [...new Set(recipients)];
  if (recipients.length === 0) {
    await snapshot.ref.set({ deliveryStatus: "NO_RECIPIENT", deliveredAt: FieldValue.serverTimestamp() }, { merge: true });
    return;
  }

  const tokenDocs = await Promise.all(recipients.map((uid) => db.collection("users").doc(uid).get()));
  const tokens = tokenDocs.map((doc) => String(doc.get("fcmToken") || "").trim()).filter(Boolean);

  if (tokens.length === 0) {
    await snapshot.ref.set({ deliveryStatus: "NO_FCM_TOKEN", recipientCount: recipients.length, deliveredAt: FieldValue.serverTimestamp() }, { merge: true });
    return;
  }

  const response = await getMessaging().sendEachForMulticast({
    tokens,
    notification: { title, body: message },
    data: {
      notificationId,
      type: String(data.type || "GENERAL"),
      referenceId: String(data.referenceId || ""),
      route: String(data.route || "")
    },
    android: { priority: "high" }
  });

  await snapshot.ref.set({
    deliveryStatus: "SENT",
    recipientCount: recipients.length,
    tokenCount: tokens.length,
    successCount: response.successCount,
    failureCount: response.failureCount,
    deliveredAt: FieldValue.serverTimestamp()
  }, { merge: true });

  logger.info("Shah ERP notification sent", { notificationId, recipients: recipients.length, successCount: response.successCount, failureCount: response.failureCount });
});
