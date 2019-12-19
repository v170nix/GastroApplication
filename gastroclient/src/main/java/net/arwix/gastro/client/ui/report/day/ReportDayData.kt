package net.arwix.gastro.client.ui.report.day

import org.threeten.bp.LocalDate

data class ReportDayData(
    val day: LocalDate,
    val totalChecks: Int,
    val totalItems: Long,
    val totalPrice: Long
)