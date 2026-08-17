package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AggregatorStats
import com.example.model.PlatformType
import com.example.model.SentimentType
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberPrimaryBright
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.TwitterCyan

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsDashboard(
    stats: AggregatorStats,
    selectedPlatformFilter: PlatformType?,
    selectedSentimentFilter: SentimentType?,
    isNoiseFilterEnabled: Boolean,
    onSelectPlatformFilter: (PlatformType?) -> Unit,
    onSelectSentimentFilter: (SentimentType?) -> Unit,
    onToggleNoiseFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isSummaryExpanded by remember { mutableStateOf(true) }

    val total = stats.totalResults.coerceAtLeast(1)
    val posPercent = ((stats.positiveCount.toFloat() / total) * 100).toInt()
    val negPercent = ((stats.negativeCount.toFloat() / total) * 100).toInt()
    val neuPercent = ((stats.neutralCount.toFloat() / total) * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("analytics_dashboard_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. Radar Live Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "رادار الاستماع الاجتماعي الموحد",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyberPrimaryBright)
                ) {
                    Text(
                        text = "${stats.totalResults} منشور مجمّع",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimaryBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Metric Cards Grid (4 KPI Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Facebook Metric Card
                MetricKpiCard(
                    title = "Facebook",
                    value = stats.facebookCount.toString(),
                    color = FacebookBlue,
                    modifier = Modifier.weight(1f)
                )

                // X/Twitter Metric Card
                MetricKpiCard(
                    title = "X / Twitter",
                    value = stats.xCount.toString(),
                    color = TwitterCyan,
                    modifier = Modifier.weight(1f)
                )

                // Positive Sentiment Metric Card
                MetricKpiCard(
                    title = "إيجابي 🟢",
                    value = "$posPercent%",
                    color = CyberGreen,
                    modifier = Modifier.weight(1f)
                )

                // Negative Sentiment Metric Card
                MetricKpiCard(
                    title = "سلبي 🔴",
                    value = "$negPercent%",
                    color = CyberRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Sentiment Balance Multi-Bar
            Text(
                text = "مؤشر الرأي العام والمشاعر:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (stats.positiveCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((stats.positiveCount.toFloat() / total).coerceAtLeast(0.02f))
                                .fillMaxHeight()
                                .background(CyberGreen)
                        )
                    }
                    if (stats.neutralCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((stats.neutralCount.toFloat() / total).coerceAtLeast(0.02f))
                                .fillMaxHeight()
                                .background(CyberYellow)
                        )
                    }
                    if (stats.negativeCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight((stats.negativeCount.toFloat() / total).coerceAtLeast(0.02f))
                                .fillMaxHeight()
                                .background(CyberRed)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🟢 إيجابي: ${stats.positiveCount} ($posPercent%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = CyberGreen
                )
                Text(
                    text = "🟡 محايد: ${stats.neutralCount} ($neuPercent%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = CyberYellow
                )
                Text(
                    text = "🔴 سلبي: ${stats.negativeCount} ($negPercent%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = CyberRed
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. AI Strategic Executive Intelligence Dossier
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            CyberPrimary.copy(alpha = 0.3f),
                            CyberSecondary.copy(alpha = 0.3f)
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSummaryExpanded = !isSummaryExpanded }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyberPrimaryBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "التقرير الاستخباراتي الذكي (AI Executive Summary)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberPrimaryBright,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(stats.aiExecutiveSummary ?: ""))
                                    Toast.makeText(context, "تم نسخ التقرير الاستخباراتي", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ التقرير",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = { isSummaryExpanded = !isSummaryExpanded },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSummaryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isSummaryExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = stats.aiExecutiveSummary ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp,
                                fontSize = 12.sp
                            )

                            if (stats.topTopics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "الكلمات الأكثر تكراراً:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                    stats.topTopics.forEach { topic ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CyberSecondary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "#$topic",
                                                color = CyberSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Interactive Feed Filters Row (Platform & Sentiment Pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All Filter
                val isAllSelected = selectedPlatformFilter == null && selectedSentimentFilter == null
                FilterChip(
                    selected = isAllSelected,
                    onClick = {
                        onSelectPlatformFilter(null)
                        onSelectSentimentFilter(null)
                    },
                    label = { Text("الكل (${stats.totalResults})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )

                // Facebook Only
                FilterChip(
                    selected = selectedPlatformFilter == PlatformType.FACEBOOK,
                    onClick = {
                        onSelectPlatformFilter(if (selectedPlatformFilter == PlatformType.FACEBOOK) null else PlatformType.FACEBOOK)
                    },
                    label = { Text("Facebook (${stats.facebookCount})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FacebookBlue.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White
                    )
                )

                // X Only
                FilterChip(
                    selected = selectedPlatformFilter == PlatformType.X_TWITTER,
                    onClick = {
                        onSelectPlatformFilter(if (selectedPlatformFilter == PlatformType.X_TWITTER) null else PlatformType.X_TWITTER)
                    },
                    label = { Text("X (${stats.xCount})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TwitterCyan.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White
                    )
                )

                // Positive Only
                FilterChip(
                    selected = selectedSentimentFilter == SentimentType.POSITIVE,
                    onClick = {
                        onSelectSentimentFilter(if (selectedSentimentFilter == SentimentType.POSITIVE) null else SentimentType.POSITIVE)
                    },
                    label = { Text("🟢 إيجابي (${stats.positiveCount})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberGreen.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White
                    )
                )

                // Negative Only
                FilterChip(
                    selected = selectedSentimentFilter == SentimentType.NEGATIVE,
                    onClick = {
                        onSelectSentimentFilter(if (selectedSentimentFilter == SentimentType.NEGATIVE) null else SentimentType.NEGATIVE)
                    },
                    label = { Text("🔴 سلبي (${stats.negativeCount})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberRed.copy(alpha = 0.25f),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun MetricKpiCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color,
                fontSize = 15.sp
            )
        }
    }
}
