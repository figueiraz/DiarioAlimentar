package com.example.diarioalimentar.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diarioalimentar.DiarioAlimentarApp
import com.example.diarioalimentar.data.local.MealEntity
import com.example.diarioalimentar.data.local.UserProfileEntity
import com.example.diarioalimentar.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

data class DailyNutritionSummary(
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0
)

data class MainUiState(
    val meals: List<MealEntity> = emptyList(),
    val userProfile: UserProfileEntity? = null,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val summary: DailyNutritionSummary = DailyNutritionSummary()
)

class MainViewModel(
    application: Application,
    private val repository: MealRepository
) : AndroidViewModel(application) {

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val uiState: StateFlow<MainUiState> = combine(
        repository.getTodayMeals(),
        repository.getUserProfile(),
        _isAnalyzing,
        _errorMessage
    ) { meals, profile, isAnalyzing, errorMessage ->
        val totalCalories = meals.sumOf { it.calories }
        val totalProtein = meals.sumOf { it.proteinGrams }
        val totalCarbs = meals.sumOf { it.carbsGrams }
        val totalFat = meals.sumOf { it.fatGrams }

        MainUiState(
            meals = meals,
            userProfile = profile,
            isAnalyzing = isAnalyzing,
            errorMessage = errorMessage,
            summary = DailyNutritionSummary(
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun registerMeal(
        imageUri: Uri?,
        mealTime: LocalTime,
        userContext: String? = null
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null

            val result = repository.processAndSaveMeal(
                context = getApplication(),
                imageUri = imageUri,
                mealTime = mealTime,
                userContext = userContext,
                userProfile = uiState.value.userProfile
            )

            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage
                    ?: "Erro ao analisar e registrar a refeição."
            }

            _isAnalyzing.value = false
        }
    }

    fun saveUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun updateMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.updateMeal(meal)
        }
    }

    fun deleteMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }

    fun restoreMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.insertMeal(meal)
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as DiarioAlimentarApp
                    val repo = MealRepository(
                        mealDao = app.database.mealDao(),
                        userProfileDao = app.database.userProfileDao()
                    )
                    return MainViewModel(application, repo) as T
                }
            }
        }
    }
}
