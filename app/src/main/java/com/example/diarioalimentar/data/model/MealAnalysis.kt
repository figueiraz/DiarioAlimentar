package com.example.diarioalimentar.data.model

data class MealAnalysis(
    val foods: List<String>,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val nutritionalFeedback: String
)
