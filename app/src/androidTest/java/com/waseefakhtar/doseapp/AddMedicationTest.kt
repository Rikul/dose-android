package com.waseefakhtar.doseapp

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.waseefakhtar.doseapp.domain.repository.MedicationRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import javax.inject.Inject

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

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Before
    fun setup() {
        hiltRule.inject()

        // Clear all medications before each test
        runBlocking {
            val medications = medicationRepository.getAllMedications().first()
            medications.forEach { medication ->
                medicationRepository.deleteMedication(medication)
            }
        }
    }

    // Helper functions for common test actions

    private fun dismissNotificationPermissionDialogIfPresent() {
        // Try to dismiss notification permission dialog if it appears (Android 13+)
        try {
            composeTestRule.waitUntil(timeoutMillis = 1000) {
                composeTestRule.onAllNodesWithText("Allow", substring = true, ignoreCase = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Allow", substring = true, ignoreCase = true)
                .performClick()
        } catch (e: Exception) {
            // Permission dialog didn't appear or already handled, continue
        }
    }

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

    private fun fillProviderName(providerName: String) {
        composeTestRule.onNodeWithText("Provider Name")
            .performClick()
            .performTextInput(providerName)
    }

    private fun fillRxNumber(rxNumber: String) {
        composeTestRule.onNodeWithText("Rx Number")
            .performClick()
            .performTextInput(rxNumber)
    }

    private fun fillPharmacyName(pharmacyName: String) {
        composeTestRule.onNodeWithText("Pharmacy")
            .performClick()
            .performTextInput(pharmacyName)
    }

    private fun fillPharmacyPhone(pharmacyPhone: String) {
        composeTestRule.onNodeWithText("Pharmacy Phone #")
            .performClick()
            .performTextInput(pharmacyPhone)
    }

    private fun fillInstructions(instructions: String) {
        composeTestRule.onNodeWithText("Instructions/Notes")
            .performClick()
            .performTextInput(instructions)
    }

    private fun updateProviderName(newProviderName: String) {
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Provider Name")
        ).performScrollTo()
            .performTextReplacement(newProviderName)
    }

    private fun updateRxNumber(newRxNumber: String) {
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Rx Number")
        ).performScrollTo()
            .performTextReplacement(newRxNumber)
    }

    private fun updatePharmacyName(newPharmacyName: String) {
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Pharmacy")
        ).performScrollTo()
            .performTextReplacement(newPharmacyName)
    }

    private fun updatePharmacyPhone(newPharmacyPhone: String) {
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Pharmacy Phone #")
        ).performScrollTo()
            .performTextReplacement(newPharmacyPhone)
    }

    private fun updateInstructions(newInstructions: String) {
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Instructions/Notes")
        ).performScrollTo()
            .performTextReplacement(newInstructions)
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
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Confirm")
            .performClick()

        // Dismiss notification permission dialog if it appears
        //dismissNotificationPermissionDialogIfPresent()
    }

    private fun clickDone() {
        composeTestRule.onNodeWithText("Done")
            .assertIsDisplayed()
            .performClick()

        // Wait to return to home screen
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Home")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickBackButton() {
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .performClick()
    }

    private fun updateMedicationName(newName: String) {
        composeTestRule.onNodeWithText("Medication Name")
            .performClick()
            .performTextReplacement(newName)
    }

    private fun verifyMedicationAppearsOnHomeScreen(medicationName: String) {
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(medicationName)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(medicationName)
            .assertIsDisplayed()
    }

    private fun clickMedication(medicationName: String) {
        composeTestRule.onNodeWithText(medicationName)
            .assertIsDisplayed()
            .performClick()

        // Wait for medication detail screen to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Done")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun verifyEditIconIsVisible() {
        composeTestRule.onNodeWithContentDescription("Edit")
            .assertIsDisplayed()
    }

    private fun clickEditIcon() {
        composeTestRule.onNodeWithContentDescription("Edit")
            .assertIsDisplayed()
            .performClick()

        // Wait for Edit Medication screen to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Edit Medication")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun verifyEditMedicationScreenIsDisplayed() {
        composeTestRule.onNodeWithText("Edit Medication")
            .assertIsDisplayed()
    }

    private fun verifyMedicationNameIsDisplayed(expectedName: String) {
        composeTestRule.onAllNodesWithText(expectedName)
            .assertAny(hasText(expectedName))
    }

    private fun verifyProviderNameIsDisplayed(expectedProviderName: String) {
        composeTestRule.onNode(
            hasText(expectedProviderName) and hasSetTextAction()
        ).performScrollTo()
            .assertIsDisplayed()
    }

    private fun verifyRxNumberIsDisplayed(expectedRxNumber: String) {
        composeTestRule.onNode(
            hasText(expectedRxNumber) and hasSetTextAction()
        ).performScrollTo()
            .assertIsDisplayed()
    }

    private fun verifyPharmacyNameIsDisplayed(expectedPharmacyName: String) {
        composeTestRule.onNode(
            hasText(expectedPharmacyName) and hasSetTextAction()
        ).performScrollTo()
            .assertIsDisplayed()
    }

    private fun verifyPharmacyPhoneIsDisplayed(expectedPharmacyPhone: String) {
        composeTestRule.onNode(
            hasText(expectedPharmacyPhone) and hasSetTextAction()
        ).performScrollTo()
            .assertIsDisplayed()
    }

    private fun verifyInstructionsIsDisplayed(expectedInstructions: String) {
        composeTestRule.onNode(
            hasText(expectedInstructions) and hasSetTextAction()
        ).performScrollTo()
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

    @Test
    fun addMedicationWithPrescription_appearsOnHomeScreen() {
        val medicationName = "Test Prescription Medication"
        val providerName = "Dr. Johnson"
        val rxNumber = "RX98765"
        val pharmacyName = "Corner Pharmacy"
        val pharmacyPhone = "555-9876"
        val instructions = "Take twice daily"

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
        fillDosage("2")
        selectMedicationType("Capsule")
        fillProviderName(providerName)
        fillRxNumber(rxNumber)
        fillPharmacyName(pharmacyName)
        fillPharmacyPhone(pharmacyPhone)
        fillInstructions(instructions)
        clickNext()
        clickConfirm()
        verifyMedicationAppearsOnHomeScreen(medicationName)
    }

    @Test
    fun medicationDetail_editIconIsVisible() {
        val medicationName = "Test Edit Icon Medication"

        // Calculate dates: today and today + 1 year
        val today = Calendar.getInstance().timeInMillis
        val oneYearLater = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.timeInMillis

        // Add a medication first
        waitForHomeScreen()
        clickAddMedicationFab()
        fillMedicationName(medicationName)
        selectDurationDates(today, oneYearLater)
        fillDosage("1")
        selectMedicationType("Tablet")
        clickNext()
        clickConfirm()
        verifyMedicationAppearsOnHomeScreen(medicationName)

        // Navigate to medication detail screen
        clickMedication(medicationName)

        // Verify edit icon is visible
        verifyEditIconIsVisible()
    }

    @Test
    fun medicationDetail_clickEditIcon_navigatesToEditMedicationScreen() {
        val medicationName = "Test Edit Navigation Medication"
        val providerName = "Dr. Smith"
        val rxNumber = "RX12345"
        val pharmacyName = "Main Street Pharmacy"
        val pharmacyPhone = "555-1234"
        val instructions = "Take with food"

        // Calculate dates: today and today + 1 year
        val today = Calendar.getInstance().timeInMillis
        val oneYearLater = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.timeInMillis

        // Add a medication with prescription info
        waitForHomeScreen()
        clickAddMedicationFab()
        fillMedicationName(medicationName)
        selectDurationDates(today, oneYearLater)
        fillDosage("1")
        selectMedicationType("Tablet")
        fillProviderName(providerName)
        fillRxNumber(rxNumber)
        fillPharmacyName(pharmacyName)
        fillPharmacyPhone(pharmacyPhone)
        fillInstructions(instructions)
        clickNext()
        clickConfirm()
        verifyMedicationAppearsOnHomeScreen(medicationName)

        // Navigate to medication detail screen
        clickMedication(medicationName)

        // Click the edit icon
        clickEditIcon()

        // Verify navigation to Edit Medication screen
        verifyEditMedicationScreenIsDisplayed()

        // Verify all fields are displayed with correct values
        verifyMedicationNameIsDisplayed(medicationName)
        verifyProviderNameIsDisplayed(providerName)
        verifyRxNumberIsDisplayed(rxNumber)
        verifyPharmacyNameIsDisplayed(pharmacyName)
        verifyPharmacyPhoneIsDisplayed(pharmacyPhone)
        verifyInstructionsIsDisplayed(instructions)
    }

    @Test
    fun addMedicationWithPrescription_verifyEdit() {
        val medicationName = "Test Edit Prescription Medication"
        val providerName = "Dr. Smith"
        val rxNumber = "RX12345"
        val pharmacyName = "Main Street Pharmacy"
        val pharmacyPhone = "555-1234"
        val instructions = "Take with food\nAvoid alcohol\nDrink plenty of water"

        // Updated prescription info
        val updatedProviderName = "Dr. Jones"
        val updatedRxNumber = "RX99999"
        val updatedPharmacyName = "Downtown Pharmacy"
        val updatedPharmacyPhone = "555-5678"
        val updatedInstructions = "Take on empty stomach\nDo not crush or chew\nMay cause drowsiness"

        // Calculate dates: today and today + 1 year
        val today = Calendar.getInstance().timeInMillis
        val oneYearLater = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.timeInMillis

        // Add a medication with prescription info
        waitForHomeScreen()
        clickAddMedicationFab()
        fillMedicationName(medicationName)
        selectDurationDates(today, oneYearLater)
        fillDosage("1")
        selectMedicationType("Tablet")
        fillProviderName(providerName)
        fillRxNumber(rxNumber)
        fillPharmacyName(pharmacyName)
        fillPharmacyPhone(pharmacyPhone)
        fillInstructions(instructions)
        clickNext()
        clickConfirm()
        verifyMedicationAppearsOnHomeScreen(medicationName)

        // Navigate to medication detail screen and click edit
        clickMedication(medicationName)
        clickEditIcon()

        // Update all prescription fields
        updateProviderName(updatedProviderName)
        updateRxNumber(updatedRxNumber)
        updatePharmacyName(updatedPharmacyName)
        updatePharmacyPhone(updatedPharmacyPhone)
        updateInstructions(updatedInstructions)

        // Save the changes
        clickNext()
        clickConfirm()

        // Wait to return to home screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Home")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate back to edit screen to verify changes were saved
        clickMedication(medicationName)
        clickEditIcon()

        // Verify all updated fields are displayed with new values
        verifyProviderNameIsDisplayed(updatedProviderName)
        verifyRxNumberIsDisplayed(updatedRxNumber)
        verifyPharmacyNameIsDisplayed(updatedPharmacyName)
        verifyPharmacyPhoneIsDisplayed(updatedPharmacyPhone)
        verifyInstructionsIsDisplayed(updatedInstructions)
    }

    @Test
    fun editMedication_backWithoutSaving_changesNotPersisted() {
        val medicationName = "Original Medication"
        val providerName = "Dr. Original"
        val rxNumber = "RX00000"
        val pharmacyName = "Original Pharmacy"
        val pharmacyPhone = "555-0000"
        val instructions = "Original instructions"

        // Modified values (that should NOT be saved)
        val modifiedMedicationName = "Modified Medication"
        val modifiedProviderName = "Dr. Modified"
        val modifiedRxNumber = "RX11111"
        val modifiedPharmacyName = "Modified Pharmacy"
        val modifiedPharmacyPhone = "555-1111"
        val modifiedInstructions = "Modified instructions"

        // Calculate dates: today and today + 1 year
        val today = Calendar.getInstance().timeInMillis
        val oneYearLater = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.timeInMillis

        // Add a medication with prescription info
        waitForHomeScreen()
        clickAddMedicationFab()
        fillMedicationName(medicationName)
        selectDurationDates(today, oneYearLater)
        fillDosage("1")
        selectMedicationType("Tablet")
        fillProviderName(providerName)
        fillRxNumber(rxNumber)
        fillPharmacyName(pharmacyName)
        fillPharmacyPhone(pharmacyPhone)
        fillInstructions(instructions)
        clickNext()
        clickConfirm()
        verifyMedicationAppearsOnHomeScreen(medicationName)

        // Navigate to medication detail screen
        clickMedication(medicationName)

        // Verify original medication name is displayed on detail screen
        composeTestRule.onNodeWithText(medicationName)
            .assertIsDisplayed()

        // Click edit
        clickEditIcon()

        // Modify all fields
        updateMedicationName(modifiedMedicationName)
        updateProviderName(modifiedProviderName)
        updateRxNumber(modifiedRxNumber)
        updatePharmacyName(modifiedPharmacyName)
        updatePharmacyPhone(modifiedPharmacyPhone)
        updateInstructions(modifiedInstructions)

        // Navigate back WITHOUT saving (using back button instead of Next/Confirm)
        clickBackButton()

        // Wait for medication detail screen to appear again
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Done")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify original medication name is still displayed on detail screen
        composeTestRule.onNodeWithText(medicationName)
            .assertIsDisplayed()

        // Click edit again to verify all fields remain unchanged
        clickEditIcon()

        // Verify all original values are still displayed (changes were NOT saved)
        verifyMedicationNameIsDisplayed(medicationName)
        verifyProviderNameIsDisplayed(providerName)
        verifyRxNumberIsDisplayed(rxNumber)
        verifyPharmacyNameIsDisplayed(pharmacyName)
        verifyPharmacyPhoneIsDisplayed(pharmacyPhone)
        verifyInstructionsIsDisplayed(instructions)
    }
}
