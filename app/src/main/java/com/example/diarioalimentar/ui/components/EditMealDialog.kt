package com.example.diarioalimentar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.diarioalimentar.data.local.MealEntity
import com.example.diarioalimentar.ui.theme.DarkSurface
import com.example.diarioalimentar.ui.theme.DarkSurfaceVariant
import com.example.diarioalimentar.ui.theme.GreenOnPrimary
import com.example.diarioalimentar.ui.theme.GreenPrimary
import org.json.JSONArray
import java.util.Locale

@Composable
fun EditMealDialog(
    meal: MealEntity,
    onDismiss: () -> Unit,
    onSave: (MealEntity) -> Unit
) {
    var foodsText by remember { mutableStateOf(meal.foodsList.joinToString(", ")) }
    var caloriesText by remember { mutableStateOf(meal.calories.toString()) }
    var proteinText by remember {
        mutableStateOf(String.format(Locale.US, "%.1f", meal.proteinGrams))
    }
    var carbsText by remember {
        mutableStateOf(String.format(Locale.US, "%.1f", meal.carbsGrams))
    }
    var fatText by remember {
        mutableStateOf(String.format(Locale.US, "%.1f", meal.fatGrams))
    }
    var feedbackText by remember { mutableStateOf(meal.nutritionalFeedback) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GreenPrimary,
        unfocusedBorderColor = DarkSurfaceVariant,
        focusedLabelColor = GreenPrimary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = GreenPrimary
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Text(
                            text = "Editar Refeição",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Horário: ${meal.time} • Data: ${meal.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Alimentos identificados
                OutlinedTextField(
                    value = foodsText,
                    onValueChange = { foodsText = it },
                    label = { Text("Alimentos (separados por vírgula)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Calorias e Proteínas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Calorias (kcal)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it.replace(',', '.') },
                        label = { Text("Proteínas (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Carboidratos e Gorduras
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it.replace(',', '.') },
                        label = { Text("Carboidratos (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it.replace(',', '.') },
                        label = { Text("Gorduras (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feedback nutricional
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Avaliação / Feedback Nutricional") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            val items = foodsText.split(",", "\n")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            val foodsJson = JSONArray(items).toString()

                            val updated = meal.copy(
                                foodsJson = foodsJson,
                                calories = caloriesText.toIntOrNull() ?: meal.calories,
                                proteinGrams = proteinText.toDoubleOrNull() ?: meal.proteinGrams,
                                carbsGrams = carbsText.toDoubleOrNull() ?: meal.carbsGrams,
                                fatGrams = fatText.toDoubleOrNull() ?: meal.fatGrams,
                                nutritionalFeedback = feedbackText.trim()
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = GreenOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "Salvar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
