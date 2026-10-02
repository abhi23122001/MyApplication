const admin = require("firebase-admin");
const crypto = require("crypto");

if (!process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
  throw new Error("FIREBASE_SERVICE_ACCOUNT_JSON environment variable is required");
}

const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON);
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

const auth = admin.auth();
const db = admin.firestore();

function randomPassword() {
  const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
  const bytes = crypto.randomBytes(18);
  let value = "";
  for (const byte of bytes) value += alphabet[byte % alphabet.length];
  return "Demo@" + value;
}

async function main() {
  const email = `demo.admin.${crypto.randomBytes(4).toString("hex")}@shahsurveyors.com`;
  const password = randomPassword();

  const user = await auth.createUser({
    email,
    password,
    displayName: "Demo Admin"
  });

  try {
    await db.collection("users").doc(user.uid).set({
      uid: user.uid,
      name: "Demo Admin",
      email,
      role: "ADMIN",
      active: true,
      approved: true,
      access: "ALL",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    console.log(JSON.stringify({
      success: true,
      email,
      password,
      uid: user.uid,
      warning: "Store these credentials securely. The password is shown only once."
    }, null, 2));
  } catch (error) {
    await auth.deleteUser(user.uid);
    throw error;
  }
}

main().catch((error) => {
  console.error(error.message || error);
  process.exit(1);
});
