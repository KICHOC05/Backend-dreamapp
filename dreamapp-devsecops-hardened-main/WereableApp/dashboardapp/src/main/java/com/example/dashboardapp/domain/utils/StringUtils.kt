package com.example.dashboardapp.domain.utils

// Function to format names
fun formatName(name: String): String {
    return name.trim()
        .split("\\s+".toRegex())
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}
// Function to format names with + simbol
fun formatNameWithPlus(name: String): String {
    return name
        .split("+")
        .joinToString(" ") { parte ->
            parte.lowercase().replaceFirstChar { it.uppercase() }
        }
}