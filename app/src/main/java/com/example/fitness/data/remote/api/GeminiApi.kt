package com.example.fitness.data.remote.api

import com.example.fitness.data.remote.model.GeminiGenerateRequest
import com.example.fitness.data.remote.model.GeminiGenerateResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body body: GeminiGenerateRequest
    ): GeminiGenerateResponse
}