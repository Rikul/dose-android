package com.waseefakhtar.doseapp

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Instrumented test for adding a medication and verifying it appears on the home screen.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddMedicationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // Helper functions for common test actions

    private fun waitForHomeScreen() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Home")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickAddMedicationFab() {
        composeTestRule.onNodeWithContentDescription("Add")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Add Medication")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun fillMedicationName(name: String) {
        composeTestRule.onNodeWithText("Medication Name")
            .performClick()
            .performTextInput(name)
    }

    private fun selectDurationDates(startDateMillis: Long, endDateMillis: Long) {
        // Click on Duration field to open date picker
        composeTestRule.onNodeWithText("Duration")
            .performClick()

        // Wait for date picker dialog
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Select Duration")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Switch to text input mode
        composeTestRule.onNode(
            hasContentDescription("Switch to text input mode"),
            useUnmergedTree = true
        ).performClick()

        Thread.sleep(1000)

        // Format dates as MMDDYYYY
        val startCalendar = Calendar.getInstance().apply { timeInMillis = startDateMillis }
        val startDate = String.format(
            "%02d%02d%d",
            startCalendar.get(Calendar.MONTH) + 1,
            startCalendar.get(Calendar.DAY_OF_MONTH),
            startCalendar.get(Calendar.YEAR)
        )

        val endCalendar = Calendar.getInstance().apply { timeInMillis = endDateMillis }
        val endDate = String.format(
            "%02d%02d%d",
            endCalendar.get(Calendar.MONTH) + 1,
            endCalendar.get(Calendar.DAY_OF_MONTH),
            endCalendar.get(Calendar.YEAR)
        )

        // Set start date
        composeTestRule.onNode(hasContentDescription("Start date, MM/DD/YYYY"), false)
            .performSemanticsAction(SemanticsActions.RequestFocus)

        composeTestRule.onNode(hasContentDescription("Start date, MM/DD/YYYY"), false)
            .performSemanticsAction(SemanticsActions.SetText) {
                it(AnnotatedString(startDate))
            }

        // Set end date
        composeTestRule.onNode(hasContentDescription("End date, MM/DD/YYYY"), false)
            .performSemanticsAction(SemanticsActions.RequestFocus)

        composeTestRule.onNode(hasContentDescription("End date, MM/DD/YYYY"), false)
            .performSemanticsAction(SemanticsActions.SetText) {
                it(AnnotatedString(endDate))
            }

        // Confirm dates
        composeTestRule.onNodeWithText("OK")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
    }

    private fun fillDosage(dosage: String) {
        composeTestRule.onNodeWithText("Dose (Optional)")
            .performClick()
            .performTextInput(dosage)

        Thread.sleep(200)
    }

    private fun selectMedicationType(type: String) {
        composeTestRule.onNodeWithText(type)
            .performClick()

        Thread.sleep(500)
    }

    private fun clickNext() {
        composeTestRule.onNodeWithText("Next")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Confirm")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickConfirm() {
        composeTestRule.onNodeWithText("Confirm")
            .performClick()
    }

    private fun verifyMedicationAppearsOnHomeScreen(medicationName: String) {
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText(medicationName)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(medicationName)
            .assertIsDisplayed()
    }

    @Test
    fun addMedication_appearsOnHomeScreen() {
        val medicationName = "Test Medication"

        // Calculate dates: today and today + 1 year
        val today = Calendar.getInstance().timeInMillis
        val oneYearLater = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.timeInMillis

        // Execute test flow using helper functions
        waitForHomeScreen()
        clickAddMedicationFab()
        fillMedicationName(medicationName)
        selectDurationDates(today, oneYearLater)
        fillDosage("1")
        selectMedicationType("Tablet")
        clickNext()
        clickConfirm()
        verifyMedicationAppearsOnHomeScreen(medicationName)
    }
}
