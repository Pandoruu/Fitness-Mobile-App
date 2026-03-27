package com.example.fitness.data.remote.api

import com.example.fitness.data.remote.model.UsdaFoodDetailsResponse
import com.example.fitness.data.remote.model.UsdaSearchRequest
import com.example.fitness.data.remote.model.UsdaSearchResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
interface UsdaApi {
    @POST("fdc/v1/foods/search")
    suspend fun searchFoods(
        @Query("api_key") apiKey: String,
        @Body body: UsdaSearchRequest
    ): UsdaSearchResponse

    @GET("fdc/v1/food/{fdcId}")
    suspend fun getFoodDetails(
        @Path("fdcId") fdcId: Long,
        @Query("api_key") apiKey: String
    ): UsdaFoodDetailsResponse
}