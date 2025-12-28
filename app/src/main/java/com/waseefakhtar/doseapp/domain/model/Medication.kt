package com.waseefakhtar.doseapp.domain.model

import android.os.Parcelable
import com.waseefakhtar.doseapp.util.MedicationType
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: Int,
    val frequency: String,
    val startDate: Date,
    val endDate: Date,
    val medicationTaken: Boolean,
    val medicationTime: Date,
    val type: MedicationType = MedicationType.getDefault(),
    val doctorName: String? = null,
    val rxNumber: String? = null,
    val pharmacyName: String? = null,
    val pharmacyPhone: String? = null,
    val instructions: String? = null
) : Parcelable
