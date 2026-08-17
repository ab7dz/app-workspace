package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleCustomSearchResponse(
    @Json(name = "items") val items: List<GoogleSearchItem>? = null,
    @Json(name = "searchInformation") val searchInformation: SearchInformation? = null,
    @Json(name = "error") val error: GoogleApiError? = null
)

@JsonClass(generateAdapter = true)
data class GoogleSearchItem(
    @Json(name = "title") val title: String? = null,
    @Json(name = "link") val link: String? = null,
    @Json(name = "displayLink") val displayLink: String? = null,
    @Json(name = "snippet") val snippet: String? = null,
    @Json(name = "formattedUrl") val formattedUrl: String? = null,
    @Json(name = "pagemap") val pagemap: PageMap? = null
)

@JsonClass(generateAdapter = true)
data class PageMap(
    @Json(name = "cse_image") val cseImage: List<CseImage>? = null,
    @Json(name = "cse_thumbnail") val cseThumbnail: List<CseThumbnail>? = null,
    @Json(name = "metatags") val metatags: List<Map<String, Any?>>? = null
)

@JsonClass(generateAdapter = true)
data class CseImage(
    @Json(name = "src") val src: String? = null
)

@JsonClass(generateAdapter = true)
data class CseThumbnail(
    @Json(name = "src") val src: String? = null,
    @Json(name = "width") val width: String? = null,
    @Json(name = "height") val height: String? = null
)

@JsonClass(generateAdapter = true)
data class SearchInformation(
    @Json(name = "totalResults") val totalResults: String? = null,
    @Json(name = "formattedSearchTime") val formattedSearchTime: String? = null
)

@JsonClass(generateAdapter = true)
data class GoogleApiError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class SerpApiResponse(
    @Json(name = "organic_results") val organicResults: List<SerpOrganicResult>? = null,
    @Json(name = "search_information") val searchInformation: Map<String, Any?>? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SerpOrganicResult(
    @Json(name = "title") val title: String? = null,
    @Json(name = "link") val link: String? = null,
    @Json(name = "snippet") val snippet: String? = null,
    @Json(name = "displayed_link") val displayedLink: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "date") val date: String? = null
)
