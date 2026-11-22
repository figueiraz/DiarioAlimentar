package com.example.diarioalimentar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentWeightKg: Float,
    val targetWeightKg: Float,
    val heightCm: Int,
    val age: Int,
    val gender: String, // "Masculino" ou "Feminino"
    val activityLevel: String, // "Sedentário", "Leve", "Moderado", "Intenso"
    val goalType: String, // "Perder Peso", "Manter Peso", "Ganhar Massa"
    val dailyCalorieGoal: Int,
    val dailyProteinGoal: Int,
    val dailyCarbsGoal: Int,
    val dailyFatGoal: Int
)
