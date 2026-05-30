package com.example.monthnameandnotes

fun getMonthName(monthNumber: Int?): String? {
    return when (monthNumber) {
        1 -> "Січень"
        2 -> "Лютий"
        3 -> "Березень"
        4 -> "Квітень"
        5 -> "Травень"
        6 -> "Червень"
        7 -> "Липень"
        8 -> "Серпень"
        9 -> "Вересень"
        10 -> "Жовтень"
        11 -> "Листопад"
        12 -> "Грудень"
        else -> null
    }
}
