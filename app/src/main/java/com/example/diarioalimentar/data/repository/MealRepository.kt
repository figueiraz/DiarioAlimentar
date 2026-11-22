package com.example.diarioalimentar.data.repository

import android.content.Context
import android.net.Uri
import com.example.diarioalimentar.data.local.MealDao
import com.example.diarioalimentar.data.local.MealEntity
import com.example.diarioalimentar.data.local.UserProfileDao
import com.example.diarioalimentar.data.local.UserProfileEntity
import com.example.diarioalimentar.data.remote.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class MealRepository(
    private val mealDao: MealDao,
    private val userProfileDao: UserProfileDao
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun getTodayMeals(): Flow<List<MealEntity>> {
        val today = LocalDate.now().format(dateFormatter)
        return mealDao.getMealsForDate(today)
    }

    fun getAllMeals(): Flow<List<MealEntity>> {
        return mealDao.getAllMeals()
    }

    fun getUserProfile(): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        userProfileDao.upsertProfile(profile)
    }

    suspend fun processAndSaveMeal(
        context: Context,
        imageUri: Uri?,
        mealTime: LocalTime,
        userContext: String? = null,
        userProfile: UserProfileEntity? = null
    ): Result<MealEntity> = withContext(Dispatchers.IO) {
        val analysisResult = GeminiRepository.analyzeMeal(
            context = context,
            imageUri = imageUri,
            mealTime = mealTime,
            userContext = userContext,
            userProfile = userProfile
        )
        if (analysisResult.isFailure) {
            return@withContext Result.failure(analysisResult.exceptionOrNull() ?: Exception("Falha na análise"))
        }

        val analysis = analysisResult.getOrThrow()

        // Se houver foto, salva uma cópia permanente no armazenamento interno do app
        val storedImageUri = if (imageUri != null) {
            savePermanentImage(context, imageUri).toString()
        } else {
            ""
        }

        val entity = MealEntity(
            imageUri = storedImageUri,
            time = mealTime.format(timeFormatter),
            date = LocalDate.now().format(dateFormatter),
            timestamp = System.currentTimeMillis(),
            foodsJson = JSONArray(analysis.foods).toString(),
            calories = analysis.calories,
            proteinGrams = analysis.proteinGrams,
            carbsGrams = analysis.carbsGrams,
            fatGrams = analysis.fatGrams,
            nutritionalFeedback = analysis.nutritionalFeedback
        )

        val id = mealDao.insertMeal(entity)
        Result.success(entity.copy(id = id))
    }

    suspend fun updateMeal(meal: MealEntity) = withContext(Dispatchers.IO) {
        mealDao.updateMeal(meal)
    }

    suspend fun insertMeal(meal: MealEntity): Long = withContext(Dispatchers.IO) {
        mealDao.insertMeal(meal)
    }

    suspend fun deleteMeal(meal: MealEntity) = withContext(Dispatchers.IO) {
        mealDao.deleteMeal(meal)
        // Opcional: remover arquivo de foto associado se for arquivo interno
        try {
            if (meal.imageUri.isNotBlank()) {
                val uri = Uri.parse(meal.imageUri)
                if (uri.scheme == "file") {
                    uri.path?.let { File(it).delete() }
                }
            }
        } catch (_: Exception) {}
    }

    private fun savePermanentImage(context: Context, sourceUri: Uri): Uri {
        val mealsDir = File(context.filesDir, "meals")
        if (!mealsDir.exists()) {
            mealsDir.mkdirs()
        }

        val destinationFile = File(mealsDir, "meal_${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(destinationFile)
    }
}
