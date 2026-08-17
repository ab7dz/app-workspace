package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SocialRepository
import com.example.model.AggregatorStats
import com.example.model.DorkQuery
import com.example.model.PlatformType
import com.example.model.SentimentType
import com.example.model.SocialPost
import com.example.model.TimeRangeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val posts: List<SocialPost>, val stats: AggregatorStats) : UiState
    data class Error(val message: String) : UiState
}

data class ApiSettings(
    val googleApiKey: String = "",
    val googleSearchEngineId: String = "",
    val serpApiKey: String = "",
    val customGeminiKey: String = ""
)

class SocialListeningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SocialRepository(application)
    private val prefs = application.getSharedPreferences("social_listening_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _dorkQuery = MutableStateFlow(
        DorkQuery(
            keyword = "الذكاء الاصطناعي",
            selectedPlatforms = setOf(PlatformType.FACEBOOK, PlatformType.X_TWITTER),
            timeRange = TimeRangeFilter.LAST_WEEK
        )
    )
    val dorkQuery: StateFlow<DorkQuery> = _dorkQuery.asStateFlow()

    private val _selectedPlatformFilter = MutableStateFlow<PlatformType?>(null)
    val selectedPlatformFilter: StateFlow<PlatformType?> = _selectedPlatformFilter.asStateFlow()

    private val _selectedSentimentFilter = MutableStateFlow<SentimentType?>(null)
    val selectedSentimentFilter: StateFlow<SentimentType?> = _selectedSentimentFilter.asStateFlow()

    private val _noiseFilterEnabled = MutableStateFlow(true)
    val noiseFilterEnabled: StateFlow<Boolean> = _noiseFilterEnabled.asStateFlow()

    private val _apiSettings = MutableStateFlow(
        ApiSettings(
            googleApiKey = prefs.getString("google_api_key", "") ?: "",
            googleSearchEngineId = prefs.getString("google_cx", "") ?: "",
            serpApiKey = prefs.getString("serp_api_key", "") ?: "",
            customGeminiKey = prefs.getString("custom_gemini_key", "") ?: ""
        )
    )
    val apiSettings: StateFlow<ApiSettings> = _apiSettings.asStateFlow()

    private val _bookmarkedPosts = MutableStateFlow<List<SocialPost>>(emptyList())
    val bookmarkedPosts: StateFlow<List<SocialPost>> = _bookmarkedPosts.asStateFlow()

    init {
        viewModelScope.launch {
            repository.bookmarkedPosts.collectLatest { list ->
                _bookmarkedPosts.value = list
            }
        }
        // Auto-run initial search so screen is vibrant immediately
        performSearch()
    }

    fun setKeyword(keyword: String) {
        _dorkQuery.value = _dorkQuery.value.copy(keyword = keyword)
    }

    fun togglePlatformInQuery(platform: PlatformType) {
        val current = _dorkQuery.value.selectedPlatforms.toMutableSet()
        if (current.contains(platform)) {
            if (current.size > 1) current.remove(platform)
        } else {
            current.add(platform)
        }
        _dorkQuery.value = _dorkQuery.value.copy(selectedPlatforms = current)
    }

    fun setTimeRange(timeRange: TimeRangeFilter) {
        _dorkQuery.value = _dorkQuery.value.copy(timeRange = timeRange)
    }

    fun setExactPhrase(phrase: String) {
        _dorkQuery.value = _dorkQuery.value.copy(exactPhrase = phrase)
    }

    fun setExcludeTerms(terms: String) {
        _dorkQuery.value = _dorkQuery.value.copy(excludeTerms = terms)
    }

    fun setMustInclude(terms: String) {
        _dorkQuery.value = _dorkQuery.value.copy(mustInclude = terms)
    }

    fun setPlatformFeedFilter(platform: PlatformType?) {
        _selectedPlatformFilter.value = platform
    }

    fun setSentimentFeedFilter(sentiment: SentimentType?) {
        _selectedSentimentFilter.value = sentiment
    }

    fun toggleNoiseFilter() {
        _noiseFilterEnabled.value = !_noiseFilterEnabled.value
    }

    fun saveApiSettings(googleKey: String, cx: String, serpKey: String, geminiKey: String) {
        prefs.edit()
            .putString("google_api_key", googleKey.trim())
            .putString("google_cx", cx.trim())
            .putString("serp_api_key", serpKey.trim())
            .putString("custom_gemini_key", geminiKey.trim())
            .apply()

        _apiSettings.value = ApiSettings(
            googleApiKey = googleKey.trim(),
            googleSearchEngineId = cx.trim(),
            serpApiKey = serpKey.trim(),
            customGeminiKey = geminiKey.trim()
        )
    }

    fun performSearch() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val settings = _apiSettings.value
                val (posts, stats) = repository.searchAndAggregate(
                    query = _dorkQuery.value,
                    googleApiKey = settings.googleApiKey.ifBlank { null },
                    googleSearchEngineId = settings.googleSearchEngineId.ifBlank { null },
                    serpApiKey = settings.serpApiKey.ifBlank { null },
                    enableAiSentiment = true,
                    geminiApiKey = settings.customGeminiKey.ifBlank { null }
                )
                _uiState.value = UiState.Success(posts = posts, stats = stats)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "حدث خطأ غير متوقع أثناء تجميع البيانات")
            }
        }
    }

    fun toggleBookmark(post: SocialPost) {
        viewModelScope.launch {
            repository.toggleBookmark(post)
            // Update current state if success
            val current = _uiState.value
            if (current is UiState.Success) {
                val updated = current.posts.map {
                    if (it.id == post.id) it.copy(isBookmarked = !it.isBookmarked) else it
                }
                _uiState.value = current.copy(posts = updated)
            }
        }
    }
}
