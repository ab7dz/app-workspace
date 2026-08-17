package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PlatformType
import com.example.model.SentimentType
import com.example.ui.components.AnalyticsDashboard
import com.example.ui.components.ApiSettingsDialog
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.DorkSearchBar
import com.example.ui.components.SocialPostCard
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberPrimaryBright
import com.example.ui.theme.CyberSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SocialListeningViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dorkQuery by viewModel.dorkQuery.collectAsState()
    val platformFilter by viewModel.selectedPlatformFilter.collectAsState()
    val sentimentFilter by viewModel.selectedSentimentFilter.collectAsState()
    val noiseFilterEnabled by viewModel.noiseFilterEnabled.collectAsState()
    val apiSettings by viewModel.apiSettings.collectAsState()
    val bookmarkedPosts by viewModel.bookmarkedPosts.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CyberPrimary, CyberSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SocialSense",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(CyberGreen)
                                )
                            }
                            Text(
                                text = "مُجمّع المحتوى الاجتماعي والرصد الذكي",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Bookmarks Action with Count Badge
                    IconButton(
                        onClick = { showBookmarksSheet = true },
                        modifier = Modifier.testTag("open_bookmarks_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (bookmarkedPosts.isNotEmpty()) {
                                    Badge(
                                        containerColor = CyberSecondary,
                                        contentColor = Color.Black
                                    ) {
                                        Text(text = bookmarkedPosts.size.toString(), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "المحفوظات",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // API Settings Dialog trigger
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات ومفاتيح الـ API",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Search Bar & Google Dorks Builder Header
                item {
                    DorkSearchBar(
                        query = dorkQuery,
                        onKeywordChange = viewModel::setKeyword,
                        onTogglePlatform = viewModel::togglePlatformInQuery,
                        onTimeRangeChange = viewModel::setTimeRange,
                        onExactPhraseChange = viewModel::setExactPhrase,
                        onExcludeTermsChange = viewModel::setExcludeTerms,
                        onMustIncludeChange = viewModel::setMustInclude,
                        onSearch = viewModel::performSearch
                    )
                }

                // 2. UI State handling
                when (val state = uiState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = CyberPrimaryBright,
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 3.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "جاري تنفيذ استعلامات الـ Dork وتجميع المحتوى وتحليل المشاعر...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }
                    }

                    is UiState.Error -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "تعذر إكمال الاستعلام",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = viewModel::performSearch,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("إعادة المحاولة")
                                    }
                                }
                            }
                        }
                    }

                    is UiState.Success -> {
                        // 3. Analytics Dashboard
                        item {
                            AnalyticsDashboard(
                                stats = state.stats,
                                selectedPlatformFilter = platformFilter,
                                selectedSentimentFilter = sentimentFilter,
                                isNoiseFilterEnabled = noiseFilterEnabled,
                                onSelectPlatformFilter = viewModel::setPlatformFeedFilter,
                                onSelectSentimentFilter = viewModel::setSentimentFeedFilter,
                                onToggleNoiseFilter = viewModel::toggleNoiseFilter
                            )
                        }

                        // Feed Filter application
                        val filteredPosts = state.posts.filter { post ->
                            val matchPlatform = platformFilter == null || post.platform == platformFilter
                            val matchSentiment = sentimentFilter == null || post.sentiment == sentimentFilter
                            matchPlatform && matchSentiment
                        }

                        // Header over feed
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "تغذية المنشورات الاجتماعية",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "${filteredPosts.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberPrimaryBright,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (platformFilter != null || sentimentFilter != null) {
                                    Text(
                                        text = "فلاتر مفعّلة ⚡",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (filteredPosts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "لا توجد منشورات تطابق الفلاتر المحددة.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredPosts, key = { it.id }) { post ->
                                SocialPostCard(
                                    post = post,
                                    onToggleBookmark = viewModel::toggleBookmark
                                )
                            }
                        }
                    }

                    UiState.Idle -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "أدخل كلمة مفتاحية واضغط على بحث لبدء التجميع.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showSettingsDialog) {
        ApiSettingsDialog(
            settings = apiSettings,
            onDismiss = { showSettingsDialog = false },
            onSave = { googleKey, cx, serpKey, geminiKey ->
                viewModel.saveApiSettings(googleKey, cx, serpKey, geminiKey)
            }
        )
    }

    if (showBookmarksSheet) {
        BookmarksSheet(
            bookmarkedPosts = bookmarkedPosts,
            onToggleBookmark = viewModel::toggleBookmark,
            onDismiss = { showBookmarksSheet = false }
        )
    }
}
