package com.example.network

import com.example.model.GoogleCustomSearchResponse
import com.example.model.SerpApiResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GoogleCustomSearchApi {
    @GET("customsearch/v1")
    suspend fun search(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String,
        @Query("dateRestrict") dateRestrict: String? = null,
        @Query("num") num: Int = 10,
        @Query("lr") languageRestrict: String? = null
    ): GoogleCustomSearchResponse
}

interface SerpApiSearchApi {
    @GET("search.json")
    suspend fun searchGoogle(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("engine") engine: String = "google",
        @Query("num") num: Int = 20,
        @Query("tbs") tbs: String? = null,
        @Query("hl") language: String = "ar"
    ): SerpApiResponse
}

object NetworkClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val googleSearchApi: GoogleCustomSearchApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GoogleCustomSearchApi::class.java)
    }

    val serpApi: SerpApiSearchApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://serpapi.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SerpApiSearchApi::class.java)
    }
}
