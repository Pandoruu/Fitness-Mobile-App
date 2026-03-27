package com.example.fitness.data.remote.model

data class UsdaSearchRequest(
    val query: String,
    val pageSize: Int = 1
)

data class UsdaSearchResponse(
    val foods: List<UsdaFoodItem> = emptyList()
)

data class UsdaFoodItem(
    val fdcId: Long,
    val description: String
)

data class UsdaFoodDetailsResponse(
    val foodNutrients: List<UsdaFoodNutrient> = emptyList()
)

data class UsdaFoodNutrient(
    val nutrient: UsdaNutrient? = null,
    val amount: Double? = null
)

data class UsdaNutrient(
    val name: String? = null,
    val unitName: String? = null
)
