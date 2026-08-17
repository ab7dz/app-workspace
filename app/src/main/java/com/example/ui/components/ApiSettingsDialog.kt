package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ApiSettings

@Composable
fun ApiSettingsDialog(
    settings: ApiSettings,
    onDismiss: () -> Unit,
    onSave: (googleKey: String, cx: String, serpKey: String, geminiKey: String) -> Unit
) {
    var googleKey by remember { mutableStateOf(settings.googleApiKey) }
    var searchEngineId by remember { mutableStateOf(settings.googleSearchEngineId) }
    var serpKey by remember { mutableStateOf(settings.serpApiKey) }
    var geminiKey by remember { mutableStateOf(settings.customGeminiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إعدادات الربط ومحركات البحث",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Box explaining the Google Dorks Architecture
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "كيف يعمل الاستماع الاجتماعي عبر Google Dorks؟",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "لتجاوز قيود وتكلفة X API و Facebook API، يقوم التطبيق بإرسال استعلامات ذكية مثل: [keyword] site:facebook.com OR site:x.com إلى محرك البحث، ثم يقوم بمعالجة وتحويل النتائج إلى بطاقات سوشيال ميديا تفاعلية مع تحليل المشاعر.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Google Custom Search JSON API:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = googleKey,
                    onValueChange = { googleKey = it },
                    label = { Text("Google API Key", fontSize = 12.sp) },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("google_api_key_input")
                )

                OutlinedTextField(
                    value = searchEngineId,
                    onValueChange = { searchEngineId = it },
                    label = { Text("Search Engine ID (cx)", fontSize = 12.sp) },
                    placeholder = { Text("0123456789:abcdefg") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("google_cx_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "أو SerpApi (محرك بحث وسيط):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = serpKey,
                    onValueChange = { serpKey = it },
                    label = { Text("SerpApi Key", fontSize = 12.sp) },
                    placeholder = { Text("serpapi_...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("serp_api_key_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "مفتاح Gemini AI (اختياري لتحليل المشاعر):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("Gemini API Key", fontSize = 12.sp) },
                    placeholder = { Text("مفتاح الذكاء الاصطناعي...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("gemini_key_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(googleKey, searchEngineId, serpKey, geminiKey)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_api_settings_button")
            ) {
                Text("حفظ الإعدادات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
