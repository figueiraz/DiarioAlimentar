package com.example.diarioalimentar.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diarioalimentar.data.local.UserProfileEntity
import com.example.diarioalimentar.ui.theme.DarkSurface
import com.example.diarioalimentar.ui.theme.DarkSurfaceVariant
import com.example.diarioalimentar.ui.theme.GreenOnPrimary
import com.example.diarioalimentar.ui.theme.GreenPrimary
import java.util.Locale

@Composable
fun AppDrawerContent(
    currentScreen: AppScreen,
    userProfile: UserProfileEntity?,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = DarkSurface
    ) {
        // Cabeçalho do Drawer com resumo do perfil e metas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = GreenOnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Diário Alimentar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Nutrição Inteligente",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Card de Perfil / Meta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .padding(14.dp)
            ) {
                if (userProfile != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Evolução do Peso",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Peso atual -> Peso objetivo (ex: 82,0 kg -> 75,0 kg)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", userProfile.currentWeightKg)} kg",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", userProfile.targetWeightKg)} kg",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }

                        // Objetivo ativo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Objetivo: ${userProfile.goalType} (${userProfile.dailyCalorieGoal} kcal/dia)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Perfil não configurado",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toque em 'Perfil e Metas' para calcular suas calorias e macronutrientes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = DarkSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        // Itens de Navegação
        NavigationDrawerItem(
            label = {
                Text(
                    text = "Diário de Hoje",
                    fontWeight = FontWeight.Medium
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null
                )
            },
            selected = currentScreen == AppScreen.DAILY_MEALS,
            onClick = { onScreenSelected(AppScreen.DAILY_MEALS) },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = GreenPrimary.copy(alpha = 0.15f),
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                unselectedContainerColor = DarkSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp)
        )

        NavigationDrawerItem(
            label = {
                Text(
                    text = "Perfil e Metas",
                    fontWeight = FontWeight.Medium
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { onScreenSelected(AppScreen.PROFILE) },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = GreenPrimary.copy(alpha = 0.15f),
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                unselectedContainerColor = DarkSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp)
        )

        NavigationDrawerItem(
            label = {
                Text(
                    text = "Métricas e Evolução",
                    fontWeight = FontWeight.Medium
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null
                )
            },
            selected = currentScreen == AppScreen.METRICS,
            onClick = { onScreenSelected(AppScreen.METRICS) },
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = GreenPrimary.copy(alpha = 0.15f),
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                unselectedContainerColor = DarkSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
