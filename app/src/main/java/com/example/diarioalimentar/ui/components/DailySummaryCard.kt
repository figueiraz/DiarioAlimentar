package com.example.diarioalimentar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diarioalimentar.data.local.UserProfileEntity
import com.example.diarioalimentar.ui.DailyNutritionSummary
import com.example.diarioalimentar.ui.theme.CalorieColor
import com.example.diarioalimentar.ui.theme.CarbsColor
import com.example.diarioalimentar.ui.theme.DarkSurface
import com.example.diarioalimentar.ui.theme.FatColor
import com.example.diarioalimentar.ui.theme.GreenPrimary
import com.example.diarioalimentar.ui.theme.ProteinColor
import java.util.Locale

@Composable
fun DailySummaryCard(
    summary: DailyNutritionSummary,
    mealCount: Int,
    profile: UserProfileEntity? = null,
    onDefineGoalsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasGoals = profile != null && profile.dailyCalorieGoal > 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resumo do Dia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$mealCount ${if (mealCount == 1) "refeição registrada" else "refeições registradas"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.height(16.dp)
                        )
                        Text(
                            text = "Hoje",
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seção de Calorias
            if (hasGoals) {
                val goal = profile.dailyCalorieGoal
                val consumed = summary.totalCalories
                val progress = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                val remaining = goal - consumed

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CalorieColor.copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
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
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = CalorieColor
                            )
                            Text(
                                text = "Calorias",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "$consumed / $goal kcal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CalorieColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CalorieColor,
                        trackColor = CalorieColor.copy(alpha = 0.25f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (remaining >= 0) "Restam $remaining kcal para sua meta" else "Meta ultrapassada em ${-remaining} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else CalorieColor
                    )
                }
            } else {
                // Calorias sem meta configurada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CalorieColor.copy(alpha = 0.12f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CalorieColor
                        )
                        Text(
                            text = "Total de Calorias",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${summary.totalCalories} kcal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = CalorieColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Seção de Macronutrientes (com ou sem barra de progresso)
            if (hasGoals) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MacroProgressItem(
                        label = "Proteínas",
                        consumed = summary.totalProtein,
                        goal = profile.dailyProteinGoal.toDouble(),
                        color = ProteinColor,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressItem(
                        label = "Carboidratos",
                        consumed = summary.totalCarbs,
                        goal = profile.dailyCarbsGoal.toDouble(),
                        color = CarbsColor,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressItem(
                        label = "Gorduras",
                        consumed = summary.totalFat,
                        goal = profile.dailyFatGoal.toDouble(),
                        color = FatColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MacroSummaryItem(
                        label = "Proteínas",
                        value = "${String.format(Locale.getDefault(), "%.1f", summary.totalProtein)}g",
                        color = ProteinColor,
                        modifier = Modifier.weight(1f)
                    )
                    MacroSummaryItem(
                        label = "Carboidratos",
                        value = "${String.format(Locale.getDefault(), "%.1f", summary.totalCarbs)}g",
                        color = CarbsColor,
                        modifier = Modifier.weight(1f)
                    )
                    MacroSummaryItem(
                        label = "Gorduras",
                        value = "${String.format(Locale.getDefault(), "%.1f", summary.totalFat)}g",
                        color = FatColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botão discreto para definir metas
                OutlinedButton(
                    onClick = onDefineGoalsClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.height(18.dp)
                        )
                        Text(
                            text = "Definir metas e objetivos corporais",
                            color = GreenPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.height(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroProgressItem(
    label: String,
    consumed: Double,
    goal: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (consumed / goal).toFloat().coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${consumed.toInt()} / ${goal.toInt()}g",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MacroSummaryItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
