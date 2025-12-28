package com.waseefakhtar.doseapp.feature.medicationconfirm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waseefakhtar.doseapp.MedicationNotificationService
import com.waseefakhtar.doseapp.analytics.AnalyticsHelper
import com.waseefakhtar.doseapp.feature.medicationconfirm.usecase.AddMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationConfirmViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase,
    private val medicationNotificationService: MedicationNotificationService,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    private val _isMedicationSaved = MutableSharedFlow<Unit>()
    val isMedicationSaved = _isMedicationSaved.asSharedFlow()

    fun addMedication(state: MedicationConfirmState) {
        viewModelScope.launch {
            val medications = state.medications

            // Check if we're editing (first medication has an ID > 0)
            val isEditing = medications.isNotEmpty() && medications.first().id > 0

            if (isEditing) {
                // Update the existing medication (first one)
                val medicationToUpdate = medications.first()
                addMedicationUseCase.updateMedication(medicationToUpdate)

                // Reschedule notification for the updated medication
                medicationNotificationService.scheduleNotification(
                    medication = medicationToUpdate,
                    analyticsHelper = analyticsHelper
                )

                // If there are additional medications (new times added), insert them
                if (medications.size > 1) {
                    val newMedications = medications.drop(1) // All except the first
                    addMedicationUseCase.addMedication(newMedications).collect { savedMedications ->
                        // Schedule notifications for the newly saved medications
                        savedMedications.forEach { medication ->
                            medicationNotificationService.scheduleNotification(
                                medication = medication,
                                analyticsHelper = analyticsHelper
                            )
                        }
                    }
                }

                _isMedicationSaved.emit(Unit)
            } else {
                // Insert new medications
                addMedicationUseCase.addMedication(medications).collect { savedMedications ->
                    // Schedule notifications for saved medications that have proper IDs
                    savedMedications.forEach { medication ->
                        medicationNotificationService.scheduleNotification(
                            medication = medication,
                            analyticsHelper = analyticsHelper
                        )
                    }
                    _isMedicationSaved.emit(Unit)
                }
            }
        }
    }

    fun logEvent(eventName: String) {
        analyticsHelper.logEvent(eventName = eventName)
    }
}
