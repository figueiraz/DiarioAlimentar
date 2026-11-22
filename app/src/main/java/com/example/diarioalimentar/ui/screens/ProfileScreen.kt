package com.example.diarioalimentar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diarioalimentar.data.local.UserProfileEntity
import com.example.diarioalimentar.ui.MainViewModel
import com.example.diarioalimentar.ui.theme.CalorieColor
import com.example.diarioalimentar.ui.theme.CarbsColor
import com.example.diarioalimentar.ui.theme.DarkBackground
import com.example.diarioalimentar.ui.theme.DarkSurface
import com.example.diarioalimentar.ui.theme.DarkSurfaceVariant
import com.example.diarioalimentar.ui.theme.FatColor
import com.example.diarioalimentar.ui.theme.GreenOnPrimary
import com.example.diarioalimentar.ui.theme.GreenPrimary
import com.example.diarioalimentar.ui.theme.ProteinColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados do formulário
    var currentWeightText by remember { mutableStateOf("75.0") }
    var targetWeightText by remember { mutableStateOf("70.0") }
    var heightText by remember { mutableStateOf("175") }
    var ageText by remember { mutableStateOf("28") }
    var gender by remember { mutableStateOf("Masculino") }
    var activityLevel by remember { mutableStateOf("Moderado") }
    var goalType by remember { mutableStateOf("Perder Peso") }

    // Metas
    var calorieGoalText by remember { mutableStateOf("2000") }
    var proteinGoalText by remember { mutableStateOf("150") }
    var carbsGoalText by remember { mutableStateOf("200") }
    var fatGoalText by remember { mutableStateOf("65") }

    // Inicializa campos com perfil existente do banco se houver
    LaunchedEffect(uiState.userProfile) {
        uiState.userProfile?.let { profile ->
            currentWeightText = profile.currentWeightKg.toString()
            targetWeightText = profile.targetWeightKg.toString()
            heightText = profile.heightCm.toString()
            ageText = profile.age.toString()
            gender = profile.gender
            activityLevel = profile.activityLevel
            goalType = profile.goalType
            calorieGoalText = profile.dailyCalorieGoal.toString()
            proteinGoalText = profile.dailyProteinGoal.toString()
            carbsGoalText = profile.dailyCarbsGoal.toString()
            fatGoalText = profile.dailyFatGoal.toString()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GreenPrimary,
        unfocusedBorderColor = DarkSurfaceVariant,
        focusedLabelColor = GreenPrimary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = GreenPrimary
    )

    // Cálculo TMB Mifflin-St Jeor + Ajuste de Metas
    fun calculateGoalsAutomatically() {
        val weight = currentWeightText.replace(',', '.').toFloatOrNull() ?: 70f
        val height = heightText.toIntOrNull() ?: 170
        val age = ageText.toIntOrNull() ?: 25

        // Fórmula Mifflin-St Jeor
        val bmr = if (gender == "Masculino") {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }

        val activityMultiplier = when (activityLevel) {
            "Sedentário" -> 1.2
            "Leve" -> 1.375
            "Moderado" -> 1.55
            "Intenso" -> 1.725
            else -> 1.375
        }

        val tdee = bmr * activityMultiplier

        val targetCalories = when (goalType) {
            "Perder Peso" -> (tdee - 500).coerceAtLeast(1200.0).toInt()
            "Ganhar Massa" -> (tdee + 350).toInt()
            else -> tdee.toInt() // Manter Peso
        }

        // Distribuição de macronutrientes recomendada
        val proteinPerKg = when (goalType) {
            "Perder Peso" -> 2.0
            "Ganhar Massa" -> 2.0
            else -> 1.6
        }
        val targetProtein = (weight * proteinPerKg).toInt()
        val proteinCalories = targetProtein * 4

        // Gorduras: ~25% das calorias totais
        val fatCalories = targetCalories * 0.25
        val targetFat = (fatCalories / 9).toInt()

        // Carboidratos: calorias restantes
        val remainingCaloriesForCarbs = (targetCalories - proteinCalories - fatCalories).coerceAtLeast(200.0)
        val targetCarbs = (remainingCaloriesForCarbs / 4).toInt()

        calorieGoalText = targetCalories.toString()
        proteinGoalText = targetProtein.toString()
        carbsGoalText = targetCarbs.toString()
        fatGoalText = targetFat.toString()

        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Metas calculadas com sucesso via Mifflin-St Jeor!",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil e Metas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Dados Corporais
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Text(
                            text = "Dados Corporais",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = currentWeightText,
                            onValueChange = { currentWeightText = it },
                            label = { Text("Peso Atual (kg)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = targetWeightText,
                            onValueChange = { targetWeightText = it },
                            label = { Text("Peso Objetivo (kg)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Altura (cm)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Idade") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sexo Biológico",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Masculino", "Feminino").forEach { opt ->
                            SelectableChip(
                                label = opt,
                                selected = gender == opt,
                                onSelect = { gender = opt },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Nível de Atividade Física",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Sedentário", "Leve", "Moderado", "Intenso").forEach { level ->
                            SelectableChip(
                                label = level,
                                selected = activityLevel == level,
                                onSelect = { activityLevel = level },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Card 2: Objetivo Atual
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Text(
                            text = "Objetivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Perder Peso", "Manter Peso", "Ganhar Massa").forEach { opt ->
                            SelectableChip(
                                label = opt,
                                selected = goalType == opt,
                                onSelect = { goalType = opt },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { calculateGoalsAutomatically() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                            Text(
                                text = "Calcular Metas Automaticamente",
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Card 3: Metas Nutricionais Diárias
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Metas Diárias de Nutrição",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ajuste fino manual dos seus alvos de calorias e macros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = calorieGoalText,
                        onValueChange = { calorieGoalText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Meta de Calorias (kcal)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = proteinGoalText,
                            onValueChange = { proteinGoalText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Proteínas (g)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = carbsGoalText,
                            onValueChange = { carbsGoalText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Carboidratos (g)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = fatGoalText,
                            onValueChange = { fatGoalText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Gorduras (g)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }

            // Botão Salvar Perfil e Metas
            Button(
                onClick = {
                    val profile = UserProfileEntity(
                        id = 1,
                        currentWeightKg = currentWeightText.replace(',', '.').toFloatOrNull() ?: 70f,
                        targetWeightKg = targetWeightText.replace(',', '.').toFloatOrNull() ?: 65f,
                        heightCm = heightText.toIntOrNull() ?: 170,
                        age = ageText.toIntOrNull() ?: 25,
                        gender = gender,
                        activityLevel = activityLevel,
                        goalType = goalType,
                        dailyCalorieGoal = calorieGoalText.toIntOrNull() ?: 2000,
                        dailyProteinGoal = proteinGoalText.toIntOrNull() ?: 140,
                        dailyCarbsGoal = carbsGoalText.toIntOrNull() ?: 220,
                        dailyFatGoal = fatGoalText.toIntOrNull() ?: 60
                    )
                    viewModel.saveUserProfile(profile)

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Perfil e Metas salvos com sucesso!",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = GreenOnPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                    Text(
                        text = "Salvar Perfil e Metas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) GreenPrimary.copy(alpha = 0.2f) else DarkSurfaceVariant
    val textColor = if (selected) GreenPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onSelect)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
