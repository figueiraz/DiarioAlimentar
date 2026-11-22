package com.example.diarioalimentar.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.diarioalimentar.BuildConfig
import com.example.diarioalimentar.data.local.UserProfileEntity
import com.example.diarioalimentar.data.model.MealAnalysis
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object GeminiRepository {

    // Chave de API lida de forma segura a partir do local.properties (BuildConfig)
    val API_KEY: String get() = BuildConfig.GEMINI_API_KEY

    // Modelos oficiais suportados no Google AI Studio
    const val PRIMARY_MODEL = "gemini-3.5-flash-lite"
    const val REQUESTED_MODEL = "gemini-3.8-flash"

    private fun createGenerativeModel(modelName: String): GenerativeModel {
        return GenerativeModel(
            modelName = modelName,
            apiKey = API_KEY,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.4f
            }
        )
    }

    suspend fun analyzeMeal(
        context: Context,
        imageUri: Uri?,
        mealTime: LocalTime,
        userContext: String? = null,
        userProfile: UserProfileEntity? = null
    ): Result<MealAnalysis> = withContext(Dispatchers.IO) {
        if (API_KEY.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Por favor, configure sua GEMINI_API_KEY no arquivo local.properties.")
            )
        }

        val bitmap = if (imageUri != null) {
            loadAndScaleBitmap(context, imageUri)
                ?: return@withContext Result.failure(IllegalArgumentException("Não foi possível carregar a imagem da refeição."))
        } else {
            null
        }

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedTime = mealTime.format(timeFormatter)

        val prompt = buildString {
            appendLine("Você é um nutricionista especialista em avaliação, cálculo calórico e monitoramento alimentar.")
            appendLine("A refeição foi realizada exatamente às $formattedTime.")
            if (!userContext.isNullOrBlank()) {
                appendLine("Detalhes e contexto fornecidos pelo usuário: \"$userContext\".")
                appendLine("Importante: Use essas informações com alta prioridade para estimar quantidades, ingredientes, peso aproximado e modo de preparo.")
            }
            if (userProfile != null) {
                appendLine("Perfil e Metas do Usuário:")
                appendLine("- Objetivo: ${userProfile.goalType}")
                appendLine("- Peso atual: ${userProfile.currentWeightKg} kg (Meta de peso: ${userProfile.targetWeightKg} kg)")
                appendLine("- Metas diárias planejadas: ${userProfile.dailyCalorieGoal} kcal | ${userProfile.dailyProteinGoal}g proteínas | ${userProfile.dailyCarbsGoal}g carboidratos | ${userProfile.dailyFatGoal}g gorduras")
                appendLine("Instrução adicional: No campo 'nutritionalFeedback', avalie expressamente se esta refeição está adequada e alinhada com o objetivo '${userProfile.goalType}' e as metas do usuário.")
            }
            if (bitmap != null) {
                appendLine("Analise cuidadosamente os itens alimentares visíveis na imagem combinados com o contexto fornecido pelo usuário.")
            } else {
                appendLine("Analise cuidadosamente os alimentos descritos no texto de contexto do usuário.")
            }
            appendLine("""
                Retorne OBRIGATORIAMENTE uma resposta estrita em JSON com os seguintes campos:
                {
                  "foods": ["nome do alimento 1", "nome do alimento 2"],
                  "calories": 450,
                  "proteinGrams": 25.0,
                  "carbsGrams": 50.0,
                  "fatGrams": 12.0,
                  "nutritionalFeedback": "Feedback nutricional curto e profissional, levando em consideração o horário da refeição ($formattedTime), o equilíbrio de macronutrientes e o alinhamento com os objetivos do usuário."
                }

                Importante: Forneça apenas o JSON válido, sem texto introdutório ou markdown extra.
            """.trimIndent())
        }

        val modelsToTry = listOf(
            PRIMARY_MODEL,
            REQUESTED_MODEL,
            "gemini-3.5-flash-lite",
            "gemini-3.8-flash",
            "gemini-3.5-flash"
        ).distinct()
        var lastError: Exception? = null

        for (modelName in modelsToTry) {
            try {
                val model = createGenerativeModel(modelName)
                val response = if (bitmap != null) {
                    model.generateContent(
                        content {
                            image(bitmap)
                            text(prompt)
                        }
                    )
                } else {
                    model.generateContent(
                        content {
                            text(prompt)
                        }
                    )
                }

                val responseText = response.text ?: ""
                val analysis = parseMealResponse(responseText)
                return@withContext Result.success(analysis)
            } catch (e: Exception) {
                lastError = e
            }
        }

        val cleanErrorMessage = lastError?.let { extractCleanErrorMessage(it) }
            ?: "Erro ao processar análise da refeição."
        Result.failure(Exception(cleanErrorMessage, lastError))
    }

    private fun extractCleanErrorMessage(throwable: Throwable): String {
        val raw = throwable.message ?: return "Erro desconhecido ao processar com Gemini."
        val regex = """"message"\s*:\s*"([^"]+)"""".toRegex()
        val match = regex.find(raw)
        return if (match != null) {
            match.groupValues[1]
        } else if (raw.contains("MissingFieldException")) {
            "Erro de comunicação com o serviço Gemini. Verifique a chave de API e a conexão."
        } else {
            raw
        }
    }

    private fun parseMealResponse(jsonText: String): MealAnalysis {
        val clean = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(clean)
        val foodsArray = json.optJSONArray("foods")
        val foods = mutableListOf<String>()
        if (foodsArray != null) {
            for (i in 0 until foodsArray.length()) {
                foods.add(foodsArray.getString(i))
            }
        }

        val calories = json.optInt("calories", 0)
        val protein = json.optDouble("proteinGrams", 0.0)
        val carbs = json.optDouble("carbsGrams", 0.0)
        val fat = json.optDouble("fatGrams", 0.0)
        val feedback = json.optString("nutritionalFeedback", "Refeição registrada com sucesso.")

        return MealAnalysis(
            foods = foods,
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            nutritionalFeedback = feedback
        )
    }

    private fun loadAndScaleBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val original = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            scaleDown(original, 1280)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
