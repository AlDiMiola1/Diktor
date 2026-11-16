package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.model.AiEvaluationDoc
import com.example.model.CustomDictionTableDoc
import com.example.model.CustomScriptDoc
import com.example.model.CustomTwisterDoc
import com.example.model.UserProgressDoc
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class DiktorRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeProgress(userId: String): Flow<UserProgressDoc?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProgressDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureProgressDocument(): UserProgressDoc {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(
                    UserProgressDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (existing != null) return existing
            }
            val payload = mapOf(
                "userId" to uid,
                "completedExercises" to emptyList<String>(),
                "totalExerciseRuns" to 0L,
                "completedPrograms" to 0L,
                "egorkiBestCount" to 0L,
                "streakDays" to 0L,
                "courseDayNumber" to 1L,
                "completedDates" to emptyList<String>(),
                "lastCompletedCourseDate" to "",
                "lastAdvancedDate" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return UserProgressDoc(id = uid, userId = uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun saveProgress(progress: UserProgressDoc) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            if (progress.createdAt != null) {
                docRef.set(progress.copy(userId = uid, updatedAt = null)).await()
            } else {
                val snap = docRef.get().await()
                if (snap.exists()) {
                    val updates = mapOf(
                        "completedExercises" to progress.completedExercises,
                        "totalExerciseRuns" to progress.totalExerciseRuns,
                        "completedPrograms" to progress.completedPrograms,
                        "egorkiBestCount" to progress.egorkiBestCount,
                        "streakDays" to progress.streakDays,
                        "courseDayNumber" to progress.courseDayNumber,
                        "completedDates" to progress.completedDates,
                        "lastCompletedCourseDate" to progress.lastCompletedCourseDate,
                        "lastAdvancedDate" to progress.lastAdvancedDate,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    docRef.update(updates).await()
                } else {
                    val payload = mapOf(
                        "userId" to uid,
                        "completedExercises" to progress.completedExercises,
                        "totalExerciseRuns" to progress.totalExerciseRuns,
                        "completedPrograms" to progress.completedPrograms,
                        "egorkiBestCount" to progress.egorkiBestCount,
                        "streakDays" to progress.streakDays,
                        "courseDayNumber" to progress.courseDayNumber,
                        "completedDates" to progress.completedDates,
                        "lastCompletedCourseDate" to progress.lastCompletedCourseDate,
                        "lastAdvancedDate" to progress.lastAdvancedDate,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    docRef.set(payload).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Custom Scripts ---

    fun observeCustomScripts(userId: String): Flow<List<CustomScriptDoc>> = flow {
        val path = "users/$userId/customScripts"
        emitAll(
            db.collection("users").document(userId).collection("customScripts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        CustomScriptDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addCustomScript(title: String, genre: String, genreLabel: String, text: String): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("customScripts").document()
        val payload = mapOf(
            "userId" to uid,
            "genre" to genre,
            "genreLabel" to genreLabel,
            "title" to title,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCustomScript(scriptId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("customScripts").document(scriptId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Custom Twisters ---

    fun observeCustomTwisters(userId: String): Flow<List<CustomTwisterDoc>> = flow {
        val path = "users/$userId/customTwisters"
        emitAll(
            db.collection("users").document(userId).collection("customTwisters")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        CustomTwisterDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addCustomTwister(soundTag: String, difficulty: String, text: String): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("customTwisters").document()
        val payload = mapOf(
            "userId" to uid,
            "soundTag" to soundTag,
            "difficulty" to difficulty,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCustomTwister(twisterId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("customTwisters").document(twisterId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Custom Diction Tables ---

    fun observeCustomDictionTables(userId: String): Flow<List<CustomDictionTableDoc>> = flow {
        val path = "users/$userId/customDictionTables"
        emitAll(
            db.collection("users").document(userId).collection("customDictionTables")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        CustomDictionTableDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addCustomDictionTable(title: String, syllables: List<String>): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("customDictionTables").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "syllables" to syllables,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCustomDictionTable(tableId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("customDictionTables").document(tableId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- AI Evaluations ---

    fun observeAiEvaluations(userId: String): Flow<List<AiEvaluationDoc>> = flow {
        val path = "users/$userId/aiEvaluations"
        emitAll(
            db.collection("users").document(userId).collection("aiEvaluations")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        AiEvaluationDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addAiEvaluation(doc: AiEvaluationDoc): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("aiEvaluations").document()
        val payload = mapOf(
            "userId" to uid,
            "dateLabel" to doc.dateLabel,
            "scriptTitle" to doc.scriptTitle,
            "modeLabel" to doc.modeLabel,
            "headline" to doc.headline,
            "totalScore" to doc.totalScore,
            "chestScore" to doc.chestScore,
            "supportScore" to doc.supportScore,
            "pauseScore" to doc.pauseScore,
            "dictionScore" to doc.dictionScore,
            "critique" to doc.critique,
            "advice" to doc.advice,
            "recommendedExId" to doc.recommendedExId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun clearAiEvaluations(ids: List<String>) {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("aiEvaluations")
        try {
            for (id in ids) {
                collectionRef.document(id).delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, collectionRef.path)
            throw e
        }
    }
}
