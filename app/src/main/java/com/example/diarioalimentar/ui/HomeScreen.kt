package com.example.diarioalimentar.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.diarioalimentar.data.local.MealEntity
import com.example.diarioalimentar.ui.components.AddMealOptionsSheet
import com.example.diarioalimentar.ui.components.DailySummaryCard
import com.example.diarioalimentar.ui.components.EditMealDialog
import com.example.diarioalimentar.ui.components.PreAnalysisDialog
import com.example.diarioalimentar.ui.components.SwipeableMealItem
import com.example.diarioalimentar.ui.components.TextMealDialog
import com.example.diarioalimentar.ui.theme.DarkBackground
import com.example.diarioalimentar.ui.theme.DarkSurface
import com.example.diarioalimentar.ui.theme.GreenOnPrimary
import com.example.diarioalimentar.ui.theme.GreenPrimary
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados de diálogo e bottom sheets
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var showPreAnalysisDialog by remember { mutableStateOf(false) }
    var showTextMealDialog by remember { mutableStateOf(false) }
    var selectedMealForEdit by remember { mutableStateOf<MealEntity?>(null) }

    // Estado da captura / seleção pendente
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingMealTime by remember { mutableStateOf(LocalTime.now()) }

    // Launcher para tirar foto com a câmera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempCameraUri
        if (success && uri != null) {
            pendingImageUri = uri
            showPreAnalysisDialog = true
        }
    }

    // Função para abrir câmera
    fun launchCamera() {
        pendingMealTime = LocalTime.now()
        val photoFile = createTempImageFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        tempCameraUri = uri
        takePictureLauncher.launch(uri)
    }

    // Launcher para selecionar da Galeria
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingMealTime = LocalTime.now()
            pendingImageUri = uri
            showPreAnalysisDialog = true
        }
    }

    // Launcher de permissão de câmera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                "Permissão de câmera necessária para fotografar refeições.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Exclusão com ação de Desfazer
    fun handleDeleteMeal(meal: MealEntity) {
        viewModel.deleteMeal(meal)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Refeição excluída",
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreMeal(meal)
            }
        }
    }

    // Exibir erros via Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            val result = snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "OK",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                viewModel.dismissError()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Diário Alimentar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Monitoramento Nutricional com IA",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimary
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddOptionsSheet = true },
                containerColor = GreenPrimary,
                contentColor = GreenOnPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar Refeição",
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(
                        text = "Registrar Refeição",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.meals.isEmpty()) {
                // Estado vazio (nenhuma refeição hoje)
                EmptyMealsPlaceholder(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Lista de refeições do dia com resumo no topo
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 88.dp // Espaço para não cobrir o FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DailySummaryCard(
                            summary = uiState.summary,
                            mealCount = uiState.meals.size,
                            profile = uiState.userProfile,
                            onDefineGoalsClick = onNavigateToProfile
                        )
                    }

                    item {
                        Text(
                            text = "Refeições de Hoje (Deslize para excluir)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(
                        items = uiState.meals,
                        key = { it.id }
                    ) { meal ->
                        SwipeableMealItem(
                            meal = meal,
                            onClick = { selectedMealForEdit = meal },
                            onDelete = { handleDeleteMeal(it) }
                        )
                    }
                }
            }

            // Diálogo de Carregamento durante análise do Gemini
            AnimatedVisibility(
                visible = uiState.isAnalyzing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AnalyzingOverlay()
            }
        }
    }

    // 1. BottomSheet com as 3 opções de registro
    if (showAddOptionsSheet) {
        AddMealOptionsSheet(
            onDismiss = { showAddOptionsSheet = false },
            onTakePhoto = {
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasCameraPermission) {
                    launchCamera()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onPickGallery = {
                pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRegisterByText = {
                pendingMealTime = LocalTime.now()
                showTextMealDialog = true
            }
        )
    }

    // 2. Modal de Pré-análise (Foto da Câmera ou Galeria com Contexto Opcional)
    val currentImageUri = pendingImageUri
    if (showPreAnalysisDialog && currentImageUri != null) {
        PreAnalysisDialog(
            imageUri = currentImageUri,
            mealTime = pendingMealTime,
            onDismiss = {
                showPreAnalysisDialog = false
                pendingImageUri = null
            },
            onConfirm = { userContext ->
                showPreAnalysisDialog = false
                viewModel.registerMeal(
                    imageUri = currentImageUri,
                    mealTime = pendingMealTime,
                    userContext = userContext.ifBlank { null }
                )
                pendingImageUri = null
            }
        )
    }

    // 3. Modal de Registro por Texto (Sem Foto)
    if (showTextMealDialog) {
        TextMealDialog(
            mealTime = pendingMealTime,
            onDismiss = { showTextMealDialog = false },
            onConfirm = { description ->
                showTextMealDialog = false
                viewModel.registerMeal(
                    imageUri = null,
                    mealTime = pendingMealTime,
                    userContext = description
                )
            }
        )
    }

    // 4. Modal de Edição Manual de Refeição
    selectedMealForEdit?.let { mealToEdit ->
        EditMealDialog(
            meal = mealToEdit,
            onDismiss = { selectedMealForEdit = null },
            onSave = { updatedMeal ->
                selectedMealForEdit = null
                viewModel.updateMeal(updatedMeal)
            }
        )
    }
}

@Composable
private fun EmptyMealsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NoPhotography,
                contentDescription = null,
                tint = GreenPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Nenhuma refeição registrada hoje",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque em 'Registrar Refeição' para fotografar com a câmera, escolher da galeria ou descrever por texto com auxílio do Gemini!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AnalyzingOverlay() {
    Dialog(onDismissRequest = { /* Não fecha tocando fora durante a análise */ }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Analisando com Gemini...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Identificando alimentos, estimando calorias e macronutrientes...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun createTempImageFile(context: Context): File {
    val tempDir = context.cacheDir
    return File.createTempFile("meal_capture_", ".jpg", tempDir)
}
