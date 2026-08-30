package com.shahsurveyors.myapplication.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Demo / development data helper.
 *
 * IMPORTANT:
 * - This class does NOT create Firebase Authentication users.
 * - It does NOT store passwords.
 * - It does NOT modify the protected `users` collection.
 * - Demo records are stored only in demo collections.
 *
 * For production, this seeder should not be called automatically.
 */
object DataSeederManager {

    private const val TAG = "DataSeederManager"

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()


    // ============================================================
    // CHECK CURRENT USER
    // ============================================================

    private fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }


    // ============================================================
    // SEED DEMO DATA IF EMPTY
    // ============================================================

    suspend fun seedDemoDataIfEmpty() {

        withContext(Dispatchers.IO) {

            try {

                /*
                 * Do not attempt Firestore writes when there
                 * is no authenticated Firebase user.
                 */

                if (!isUserSignedIn()) {

                    Log.w(
                        TAG,
                        "Demo seeding skipped: no authenticated user."
                    )

                    return@withContext
                }


                // ------------------------------------------------
                // Check the actual demo collection.
                // ------------------------------------------------

                val demoUsersSnapshot =
                    firestore
                        .collection("demoUsers")
                        .limit(1)
                        .get()
                        .await()


                /*
                 * Only seed when demoUsers is empty.
                 */

                if (demoUsersSnapshot.isEmpty) {

                    executeSeeding()

                } else {

                    Log.d(
                        TAG,
                        "Demo data already exists. Seeding skipped."
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Unable to check demo data.",
                    e
                )
            }
        }
    }


    // ============================================================
    // EXECUTE SEEDING
    // ============================================================

    suspend fun executeSeeding() {

        withContext(Dispatchers.IO) {

            try {

                if (!isUserSignedIn()) {

                    Log.w(
                        TAG,
                        "Demo seeding skipped: user is not signed in."
                    )

                    return@withContext
                }


                // ------------------------------------------------
                // FIRESTORE BATCH
                // ------------------------------------------------

                val batch =
                    firestore.batch()


                // =================================================
                // DEMO USERS
                // =================================================

                /*
                 * These are NOT Firebase Authentication accounts.
                 *
                 * They are only demo profile documents.
                 */

                val demoUsers =
                    listOf(

                        mapOf(
                            "name" to "Demo Admin",
                            "email" to "demo.admin@example.com",
                            "role" to "ADMIN",
                            "department" to "MANAGEMENT",
                            "access" to "ALL",
                            "approved" to true,
                            "active" to true
                        ),

                        mapOf(
                            "name" to "Demo Surveyor",
                            "email" to "demo.surveyor@example.com",
                            "role" to "SURVEYOR",
                            "department" to "SURVEY",
                            "access" to
                                    "ATTENDANCE,TASKS,CALCULATOR,DSR",
                            "approved" to true,
                            "active" to true
                        ),

                        mapOf(
                            "name" to "Demo Marketing",
                            "email" to "demo.marketing@example.com",
                            "role" to "MARKETING",
                            "department" to "MARKETING",
                            "access" to
                                    "CLIENTS,TASKS,CHAT",
                            "approved" to true,
                            "active" to true
                        )
                    )


                // =================================================
                // DEMO EQUIPMENT
                // =================================================

                val equipment =
                    listOf(

                        mapOf(
                            "type" to "DGPS",
                            "model" to "Leica GS16",
                            "equipmentId" to "GS16_001",
                            "site" to "NTPC Singrauli Site",
                            "status" to "AVAILABLE"
                        ),

                        mapOf(
                            "type" to "Total Station",
                            "model" to "Leica TS04",
                            "equipmentId" to "TS04_U1",
                            "site" to "Main Lab",
                            "status" to "AVAILABLE"
                        )
                    )


                // =================================================
                // DEMO CLIENTS
                // =================================================

                val clients =
                    listOf(

                        mapOf(
                            "name" to
                                    "M/s Northern Coalfields Ltd",

                            "phone" to
                                    "9425000000",

                            "service" to
                                    "Topographical Survey",

                            "location" to
                                    "NCL Singrauli"
                        ),

                        mapOf(
                            "name" to
                                    "M/s Reliance Power Sasan",

                            "phone" to
                                    "9826000000",

                            "service" to
                                    "Boundary Demarcation",

                            "location" to
                                    "Sasan"
                        )
                    )


                // =================================================
                // ADD DEMO USERS
                // =================================================

                demoUsers.forEach { user ->

                    val email =
                        user["email"]
                            ?.toString()
                            ?: return@forEach


                    val documentId =
                        email
                            .replace(".", "_")
                            .replace("@", "_")


                    val reference =
                        firestore
                            .collection("demoUsers")
                            .document(documentId)


                    batch.set(
                        reference,
                        user
                    )
                }


                // =================================================
                // ADD EQUIPMENT
                // =================================================

                equipment.forEach { item ->

                    val equipmentId =
                        item["equipmentId"]
                            ?.toString()
                            ?: return@forEach


                    val reference =
                        firestore
                            .collection("equipment")
                            .document(equipmentId)


                    batch.set(
                        reference,
                        item
                    )
                }


                // =================================================
                // ADD CLIENTS
                // =================================================

                clients.forEach { client ->

                    val reference =
                        firestore
                            .collection("demoClients")
                            .document()


                    batch.set(
                        reference,
                        client
                    )
                }


                // =================================================
                // COMMIT
                // =================================================

                batch.commit().await()


                Log.d(
                    TAG,
                    "Demo data created successfully."
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Demo data creation failed.",
                    e
                )
            }
        }
    }
}