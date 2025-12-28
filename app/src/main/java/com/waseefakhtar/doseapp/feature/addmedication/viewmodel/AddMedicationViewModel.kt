package com.waseefakhtar.doseapp.feature.addmedication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.waseefakhtar.doseapp.analytics.AnalyticsHelper
import com.waseefakhtar.doseapp.domain.model.Medication
import com.waseefakhtar.doseapp.feature.addmedication.model.CalendarInformation
import com.waseefakhtar.doseapp.util.Frequency
import com.waseefakhtar.doseapp.util.MedicationType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.Date
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.waseefakhtar.doseapp.domain.repository.MedicationRepository
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    @ApplicationContext private val context: Context,
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var medication = mutableStateOf<Medication?>(null)
        private set

    init {
        val medicationId = savedStateHandle.get<String>("id")?.toLongOrNull()
        if (medicationId != null) {
            viewModelScope.launch {
                medication.value = medicationRepository.getMedicationById(medicationId)
            }
        }
    }

    fun createMedications(
        name: String,
        dosage: Int,
        frequency: String,
        startDate: Date,
        endDate: Date,
        medicationTimes: List<CalendarInformation>,
        type: MedicationType,
        doctorName: String = "",
        rxNumber: String = "",
        pharmacyName: String = "",
        pharmacyPhone: String = "",
        instructions: String = "",
        medicationId: Long? = null
    ): List<Medication> {
        // If editing an existing medication
        if (medicationId != null && medication.value != null) {
            val originalMedication = medication.value!!
            val frequencyValue = Frequency.valueOf(frequency)
            val formattedFrequency = context.getString(frequencyValue.stringResId, frequencyValue.days)
            val medications = mutableListOf<Medication>()

            // Update the existing medication with the first time
            val updatedMedicationTime = if (medicationTimes.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                calendar.time = originalMedication.medicationTime
                calendar.set(Calendar.HOUR_OF_DAY, medicationTimes[0].dateInformation.hour)
                calendar.set(Calendar.MINUTE, medicationTimes[0].dateInformation.minute)
                calendar.time
            } else {
                originalMedication.medicationTime
            }

            medications.add(
                Medication(
                    id = medicationId,
                    name = name,
                    dosage = dosage,
                    frequency = formattedFrequency,
                    startDate = startDate,
                    endDate = endDate,
                    medicationTaken = originalMedication.medicationTaken,
                    medicationTime = updatedMedicationTime,
                    type = type,
                    doctorName = doctorName,
                    rxNumber = rxNumber,
                    pharmacyName = pharmacyName,
                    pharmacyPhone = pharmacyPhone,
                    instructions = instructions
                )
            )

            // If there are additional times, create new medication instances for them
            if (medicationTimes.size > 1) {
                val interval = frequencyValue.days
                val oneDayInMillis = 86400 * 1000
                val durationInDays = ((endDate.time + oneDayInMillis - startDate.time) / oneDayInMillis).toInt()
                val numOccurrences = if (durationInDays > 0) maxOf(1, durationInDays / interval) else 0

                val calendar = Calendar.getInstance()
                calendar.time = startDate

                for (i in 0 until numOccurrences) {
                    // Skip the first time since we already updated the existing medication
                    for (index in 1 until medicationTimes.size) {
                        val medicationTime = medicationTimes[index]
                        medications.add(
                            Medication(
                                id = 0, // New medication
                                name = name,
                                dosage = dosage,
                                frequency = formattedFrequency,
                                startDate = startDate,
                                endDate = endDate,
                                medicationTaken = false,
                                medicationTime = getMedicationTime(medicationTime, calendar),
                                type = type,
                                doctorName = doctorName,
                                rxNumber = rxNumber,
                                pharmacyName = pharmacyName,
                                pharmacyPhone = pharmacyPhone,
                                instructions = instructions
                            )
                        )
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, interval)
                }
            }

            return medications
        }

        // Creating new medications
        val frequencyValue = Frequency.valueOf(frequency)
        val interval = try {
            frequencyValue.days
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid frequency: $frequency")
        }

        val oneDayInMillis = 86400 * 1000 // Number of milliseconds in one day
        val durationInDays = ((endDate.time + oneDayInMillis - startDate.time) / oneDayInMillis).toInt()

        // Always create at least one occurrence if we have a valid duration
        val numOccurrences = if (durationInDays > 0) maxOf(1, durationInDays / interval) else 0

        // Create a Medication object for each occurrence and add it to a list
        val medications = mutableListOf<Medication>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val formattedFrequency = context.getString(frequencyValue.stringResId, frequencyValue.days)
        for (i in 0 until numOccurrences) {
            for (medicationTime in medicationTimes) {
                val medication = Medication(
                    id = 0,
                    name = name,
                    dosage = dosage,
                    frequency = formattedFrequency,
                    startDate = startDate,
                    endDate = endDate,
                    medicationTaken = false,
                    medicationTime = getMedicationTime(medicationTime, calendar),
                    type = type,
                    doctorName = doctorName,
                    rxNumber = rxNumber,
                    pharmacyName = pharmacyName,
                    pharmacyPhone = pharmacyPhone,
                    instructions = instructions
                )
                medications.add(medication)
            }

            // Increment the date based on the frequency interval
            calendar.add(Calendar.DAY_OF_YEAR, interval)
        }

        return medications
    }

    private fun getMedicationTime(medicationTime: CalendarInformation, calendar: Calendar): Date {
        calendar.set(Calendar.HOUR_OF_DAY, medicationTime.dateInformation.hour)
        calendar.set(Calendar.MINUTE, medicationTime.dateInformation.minute)
        return calendar.time
    }

    fun logEvent(eventName: String) {
        analyticsHelper.logEvent(eventName = eventName)
    }
}
