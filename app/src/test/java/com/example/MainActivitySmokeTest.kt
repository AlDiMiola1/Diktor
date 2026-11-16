package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityOpensDirectlyIntoTrainingsAndNavigatesAllTabs() {
        composeRule.waitForIdle()

        // 1. Direct entry into Диктор PRO without auth wall & compact TopAppBar icons visible
        composeRule.onNodeWithText("Диктор PRO").assertIsDisplayed()
        composeRule.onNodeWithTag("open_help_button").assertIsDisplayed()
        composeRule.onNodeWithTag("sound_mute_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_status_menu_button").assertIsDisplayed()
        composeRule.onNodeWithTag("trainings_list")
            .performScrollToNode(hasTestTag("start_daily_course_button"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("start_daily_course_button").assertIsDisplayed()

        // 2. Scroll down LazyColumn to exercise catalog and open an exercise modal
        composeRule.onNodeWithTag("trainings_list")
            .performScrollToNode(hasTestTag("exercise_card_hidden_yawn"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("exercise_card_hidden_yawn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("close_exercise_modal_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // 3. Switch to Studio & AI tab
        composeRule.onNodeWithTag("nav_tab_studio").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("СТУДИЙНЫЙ ТЕЛЕСУФЛЕР").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_list")
            .performScrollToNode(hasTestTag("studio_record_button"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_record_button").assertIsDisplayed()

        // 4. Switch to Diction & Tempo tab
        composeRule.onNodeWithTag("nav_tab_diction").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("СТУДИЙНЫЙ МЕТРОНОМ").assertIsDisplayed()

        // 5. Switch to Progress tab and scroll to optional Google Sign-In card
        composeRule.onNodeWithTag("nav_tab_progress").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ДНЕВНИК РЕГУЛЯРНОСТИ").assertIsDisplayed()
        composeRule.onNodeWithTag("progress_list")
            .performScrollToNode(hasTestTag("progress_google_sign_in_button"))
        composeRule.waitForIdle()
        val hasSyncBtn = composeRule.onAllNodesWithTag("progress_google_sign_in_button")
            .fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodesWithTag("progress_sign_out_button").fetchSemanticsNodes().isNotEmpty()
        assertTrue("Expected optional sync control in Progress tab", hasSyncBtn)
    }
}
