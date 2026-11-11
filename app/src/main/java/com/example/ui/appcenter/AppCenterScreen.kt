package com.example.ui.appcenter

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.home.MainViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TealAccent

data class AppModuleItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AppCenterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val modules = remember {
        listOf(
            AppModuleItem("education", "Eğitim Asistanı", "Ders planlama, konu anlatımı, soru çözümü ve sınav hazırlığı.", Icons.Default.School, CyanAccent),
            AppModuleItem("travel", "Seyahat Asistanı", "Rota çıkarma, bütçe ve gezi programı hazırlama.", Icons.Default.FlightTakeoff, TealAccent),
            AppModuleItem("shopping", "Alışveriş Asistanı", "İhtiyaç analizi, ürün karşılaştırması ve bütçe tasarrufu.", Icons.Default.ShoppingCart, AmberAccent),
            AppModuleItem("career", "Kariyer Danışmanı", "CV hazırlama, mülakat simülasyonu ve iş stratejileri.", Icons.Default.Work, Color(0xFF818CF8)),
            AppModuleItem("finance", "Finans Danışmanı", "Harcama kontrolü, tasarruf stratejileri ve bütçe disiplini.", Icons.Default.Payments, Color(0xFF34D399)),
            AppModuleItem("real_estate", "Emlak Asistanı", "İlan metni hazırlama, kira analizi ve mülk yönetimi.", Icons.Default.Home, Color(0xFFF472B6)),
            AppModuleItem("creative", "Yaratıcı Asistan", "Metin yazarlığı, içerik üretimi ve sosyal medya kurgusu.", Icons.Default.Lightbulb, Color(0xFFFBBF24)),
            AppModuleItem("software", "Yazılım Asistanı", "Kod yazma, kod inceleme, hata ayıklama ve mimari tavsiyeler.", Icons.Default.Code, Color(0xFF60A5FA))
        )
    }

    var selectedModule by remember { mutableStateOf<AppModuleItem?>(null) }
    var moduleInput by remember { mutableStateOf("") }
    val isAppModuleLoading by viewModel.isAppModuleLoading.collectAsState()
    val activeAppModuleResult by viewModel.activeAppModuleResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HAYATIM AI Uygulama Merkezi",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Farklı uzmanlık alanlarına sahip AI modülleri",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = CyanAccent.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Hub",
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(modules, key = { it.id }) { module ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedModule = module
                            viewModel.clearAppModuleResult()
                            moduleInput = ""
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = module.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = module.icon,
                                    contentDescription = module.title,
                                    tint = module.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = module.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }
                }
            }
        }
    }

    // Module Interaction Dialog
    selectedModule?.let { module ->
        Dialog(onDismissRequest = {
            selectedModule = null
            viewModel.clearAppModuleResult()
        }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = module.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = module.icon,
                                    contentDescription = module.title,
                                    tint = module.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = moduleInput,
                        onValueChange = { moduleInput = it },
                        label = { Text("Sorunuz veya isteğiniz...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isAppModuleLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = module.color)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini uzmanı çalışıyor...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (activeAppModuleResult != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(12.dp)) {
                                item {
                                    Text(
                                        text = activeAppModuleResult ?: "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            selectedModule = null
                            viewModel.clearAppModuleResult()
                        }) {
                            Text("Kapat")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.runSpecializedAppModule(module.id, module.title, moduleInput) },
                            enabled = moduleInput.isNotBlank() && !isAppModuleLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = module.color)
                        ) {
                            Text("Çalıştır", color = NavyDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
