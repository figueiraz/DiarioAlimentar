package com.example.diarioalimentar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val time: String, // HH:mm
    val date: String, // yyyy-MM-dd
    val timestamp: Long,
    val foodsJson: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val nutritionalFeedback: String
) {
    val foodsList: List<String>
        get() {
            return try {
                val array = JSONArray(foodsJson)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                list
            } catch (e: Exception) {
                if (foodsJson.isNotBlank()) listOf(foodsJson) else emptyList()
            }
        }
}
