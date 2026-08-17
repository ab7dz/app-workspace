package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DorkQuery
import com.example.model.PlatformType
import com.example.model.TimeRangeFilter
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberPrimaryBright
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.TwitterCyan
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun DorkSearchBar(
    query: DorkQuery,
    onKeywordChange: (String) -> Unit,
    onTogglePlatform: (PlatformType) -> Unit,
    onTimeRangeChange: (TimeRangeFilter) -> Unit,
    onExactPhraseChange: (String) -> Unit,
    onExcludeTermsChange: (String) -> Unit,
    onMustIncludeChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showAdvancedDorks by remember { mutableStateOf(false) }

    val generatedDork = remember(query) { query.buildGoogleDork() }

    val trendingSuggestions = remember {
        listOf(
            "🤖 الذكاء الاصطناعي" to "الذكاء الاصطناعي",
            "⚽ دوري وكلاسيكو" to "مباراة الكلاسيكو",
            "📈 تداول وذهب" to "أسهم تداول الذهب",
            "☕ كافيهات ومطاعم" to "تجارب المقاهي والمطاعم",
            "🚗 سيارات تسلا" to "سيارات تسلا الكهربائية",
            "🇸🇦 رؤية 2030" to "مشاريع رؤية 2030",
            "🛡️ أمن سيبراني" to "الأمن السيبراني وحماية البيانات",
            "🎬 سينما وأفلام" to "مراجعات السينما والأفلام"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    CyberPrimary.copy(alpha = 0.6f),
                    CyberSecondary.copy(alpha = 0.6f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. Trending Quick Suggestion Chips (1-tap Instant Search)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSecondary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyberSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تريندات سريعة:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                trendingSuggestions.forEach { (label, kw) ->
                    val isSelected = query.keyword.trim() == kw.trim()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyberPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.dp, CyberPrimaryBright) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onKeywordChange(kw)
                                onSearch()
                            }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CyberPrimaryBright else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Primary Keyword Search Field
            OutlinedTextField(
                value = query.keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("keyword_search_input"),
                placeholder = {
                    Text(
                        text = "اكتب أي كلمة، علامة تجارية، أو موضوع...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = CyberPrimaryBright
                    )
                },
                trailingIcon = {
                    if (query.keyword.isNotBlank()) {
                        IconButton(onClick = { onKeywordChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح النص",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPrimaryBright,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Action Row: Run Search Button + Toggle Advanced Dorks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Search Trigger
                Button(
                    onClick = onSearch,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("execute_search_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بدء الاستماع والتحليل",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                // Advanced Dorks Toggle Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (showAdvancedDorks) CyberSecondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (showAdvancedDorks) BorderStroke(1.dp, CyberSecondary) else null,
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showAdvancedDorks = !showAdvancedDorks }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (showAdvancedDorks) CyberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showAdvancedDorks) "إخفاء الفلاتر" else "فلاتر الـ Dorks",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (showAdvancedDorks) CyberSecondary else MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (showAdvancedDorks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (showAdvancedDorks) CyberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 4. Advanced Google Dorks Filter Panel (Collapsible)
            AnimatedVisibility(
                visible = showAdvancedDorks,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Platform Check Chips
                    Text(
                        text = "المنصات المستهدفة (site:):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PlatformType.values().forEach { platform ->
                            val selected = query.selectedPlatforms.contains(platform)
                            FilterChip(
                                selected = selected,
                                onClick = { onTogglePlatform(platform) },
                                label = {
                                    Text(
                                        text = platform.displayNameAr,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (platform) {
                                        PlatformType.FACEBOOK -> FacebookBlue.copy(alpha = 0.25f)
                                        PlatformType.X_TWITTER -> TwitterCyan.copy(alpha = 0.25f)
                                        else -> CyberPrimary.copy(alpha = 0.25f)
                                    },
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Range Filter
                    Text(
                        text = "النطاق الزمني (Date Restrict):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TimeRangeFilter.values().forEach { timeRange ->
                            val selected = query.timeRange == timeRange
                            FilterChip(
                                selected = selected,
                                onClick = { onTimeRangeChange(timeRange) },
                                label = {
                                    Text(
                                        text = timeRange.labelAr,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Exact phrase, Exclude terms, Must include inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = query.exactPhrase,
                            onValueChange = onExactPhraseChange,
                            modifier = Modifier.weight(1f),
                            label = { Text("عبارة دقيقة \" \"", fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = query.excludeTerms,
                            onValueChange = onExcludeTermsChange,
                            modifier = Modifier.weight(1f),
                            label = { Text("استبعاد كلمات -", fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Generated Live Google Dork Query Box with 1-tap Browser Launcher
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "أمر الـ Dork المُولّد للمحركات:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                Row {
                                    // Copy Dork
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedDork))
                                            Toast.makeText(context, "تم نسخ أمر الـ Dork", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "نسخ",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Open directly in Google
                                    IconButton(
                                        onClick = {
                                            openUrlInBrowser(context, "https://www.google.com/search?q=${URLEncoder.encode(generatedDork, StandardCharsets.UTF_8.toString())}")
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = "فتح في Google",
                                            tint = CyberPrimaryBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = generatedDork,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun openUrlInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر فتح المتصفح", Toast.LENGTH_SHORT).show()
    }
}
