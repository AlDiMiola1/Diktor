package com.example.data.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.AiEvaluationDoc
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class DiktorRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureProgressDocument_authenticatedOwner_createsAndUpdatesProgress() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = DiktorRepository(firestore, auth)

        val doc = withTimeout(DEFAULT_TIMEOUT_MS) { repo.ensureProgressDocument() }
        assertEquals(uid, doc.userId)
        assertTrue(doc.courseDayNumber >= 1L)

        val updated = doc.copy(
            completedExercises = (doc.completedExercises + "hidden_yawn").distinct(),
            totalExerciseRuns = doc.totalExerciseRuns + 1L,
            egorkiBestCount = maxOf(doc.egorkiBestCount, 18L)
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { repo.saveProgress(updated) }

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProgress(uid).first { it != null && it.egorkiBestCount >= 18L }
        }
        assertNotNull(observed)
        assertTrue(observed!!.completedExercises.contains("hidden_yawn"))
    }

    @Test
    fun customCollections_authenticatedOwner_createObserveAndDelete() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = DiktorRepository(firestore, auth)
        val marker = UUID.randomUUID().toString().take(8)

        val scriptId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomScript(
                title = "Скрипт $marker",
                genre = "news",
                genreLabel = "Новости",
                text = "Проверка *телесуфлера* ↘"
            )
        }
        val twisterId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomTwister(
                soundTag = "Р / Л",
                difficulty = "Базовая",
                text = "Карл у Клары $marker"
            )
        }
        val tableId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addCustomDictionTable(
                title = "Таблица $marker",
                syllables = listOf("КПТА", "КПТО", "КПТУ")
            )
        }
        val evalId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addAiEvaluation(
                AiEvaluationDoc(
                    dateLabel = "15.11 16:00",
                    scriptTitle = "Скрипт $marker",
                    modeLabel = "Gemini AI Режиссер",
                    headline = "Отличная опора",
                    totalScore = 9L,
                    chestScore = 9L,
                    supportScore = 8L,
                    pauseScore = 9L,
                    dictionScore = 9L,
                    critique = "Следите за концовкой второй фразы.",
                    advice = "Добавьте скрытый зевок перед вдохом.",
                    recommendedExId = "hidden_yawn"
                )
            )
        }

        val scripts = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomScripts(uid).first { list -> list.any { it.id == scriptId } }
        }
        val twisters = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomTwisters(uid).first { list -> list.any { it.id == twisterId } }
        }
        val tables = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCustomDictionTables(uid).first { list -> list.any { it.id == tableId } }
        }
        val evals = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAiEvaluations(uid).first { list -> list.any { it.id == evalId } }
        }

        assertTrue(scripts.any { it.id == scriptId })
        assertTrue(twisters.any { it.id == twisterId })
        assertTrue(tables.any { it.id == tableId })
        assertTrue(evals.any { it.id == evalId })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.deleteCustomScript(scriptId)
            repo.deleteCustomTwister(twisterId)
            repo.deleteCustomDictionTable(tableId)
            repo.clearAiEvaluations(listOf(evalId))
        }
    }

    @Test
    fun crossUserReadAndWrite_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = DiktorRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.ensureProgressDocument() }
        val aliceScriptId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addCustomScript("Alice Script", "custom", "Мой сценарий", "Текст Алисы")
        }

        signInTestUser(BOB_EMAIL)

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
            fail("Expected cross-user read to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val ffe = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", ffe)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ffe!!.code)
        }

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("customScripts").document(aliceScriptId).delete().await()
            }
            fail("Expected cross-user delete to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val ffe = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", ffe)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ffe!!.code)
        }
    }

    @Test
    fun observeProgress_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = DiktorRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) { repo.observeProgress("alice_123").first() }
            fail("Expected unauthenticated snapshot listener to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val ffe = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", ffe)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ffe!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_diktor@test.com"
        const val BOB_EMAIL = "bob_diktor@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
