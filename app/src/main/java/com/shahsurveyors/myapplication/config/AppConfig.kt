package com.shahsurveyors.myapplication.config

/**
 * Central configuration for Shah Surveyors ERP.
 *
 * IMPORTANT:
 * These values are configuration identifiers, not user credentials.
 * Do NOT put passwords, API keys, Firebase private keys, or access tokens here.
 */
object AppConfig {

    // Google Apps Script Web App endpoint
    const val WEBHOOK_URL =
        "https://script.google.com/macros/s/AKfycbyvyVeBGYxs2U8-X9QoFK19e5ahe6VIa2hUxtYuml60X60BbczOMgYTJ38Pctvf_sQAqw/exec"

    // Google Drive folder used by the application
    const val DRIVE_FOLDER_ID =
        "1SVlFkCactCKm7PApRJhDqYubTr573O_4"

    // Google Spreadsheet used by the application
    const val SPREADSHEET_ID =
        "1ZR7bJd-DSYNHNveyGmCxmiNCID1e8dCXCZGwy3Xt3JY"
}