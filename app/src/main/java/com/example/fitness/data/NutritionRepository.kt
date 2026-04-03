package com.example.fitness.data

import com.example.fitness.BuildConfig
import com.example.fitness.data.remote.api.GeminiApi
import com.example.fitness.data.remote.api.UsdaApi
import com.example.fitness.data.remote.model.GeminiContent
import com.example.fitness.data.remote.model.GeminiGenerateRequest
import com.example.fitness.data.remote.model.GeminiGenerateResponse
import com.example.fitness.data.remote.model.GeminiGenerationConfig
import com.example.fitness.data.remote.model.GeminiPart
import com.example.fitness.data.remote.model.UsdaFoodDetailsResponse
import com.example.fitness.data.remote.model.UsdaSearchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NutritionRepository(
    private val usdaApi: UsdaApi,
    private val geminiApi: GeminiApi,
    private val geminiModel: String = "gemini-2.5-flash",
    private val usdaKey: String = BuildConfig.USDA_API_KEY,
    private val geminiKey: String = BuildConfig.GEMINI_API_KEY,
) {

    suspend fun suggestFromUserInput(foodQuery: String, grams: Double): String = withContext(Dispatchers.IO) {
        require(usdaKey.isNotBlank()) { "USDA_API_KEY đang trống" }
        require(geminiKey.isNotBlank()) { "GEMINI_API_KEY đang trống" }

        // 1) USDA search -> fdcId + description
        val search = usdaApi.searchFoods(
            apiKey = usdaKey,
            body = UsdaSearchRequest(
                query = foodQuery,
                pageSize = 5
            )
        )

        val item = search.foods.firstOrNull()
            ?: error("Không tìm thấy món ăn: $foodQuery")

        // 2) USDA details -> kcal per 100g
        val details = usdaApi.getFoodDetails(item.fdcId, usdaKey)
        val kcalPer100g = extractKcalPer100g(details)
        val kcalTotal = kcalPer100g * grams / 100.0

        // 3) Gemini prompt
        val prompt = buildPrompt(
            foodDescription = item.description,
            grams = grams,
            kcalTotal = kcalTotal
        )

        // 4) Gemini generateContent
        val resp = geminiApi.generateContent(
            model = geminiModel,
            apiKey = geminiKey,
            body = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4,
                    maxOutputTokens = 1000
                )
            )
        )

        val text = extractGeminiText(resp)
        stripMarkdownFences(text)
    }

    private fun extractGeminiText(resp: GeminiGenerateResponse): String {
        val text = resp.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.mapNotNull { it.text }
            ?.joinToString(separator = "")
            ?.trim()
            .orEmpty()

        if (text.isBlank()) {
            error("Gemini không trả về text")
        }
        return text
    }

    private fun extractKcalPer100g(details: UsdaFoodDetailsResponse): Double {
        val energy = details.foodNutrients.firstOrNull { n ->
            n.nutrient?.name == "Energy" && n.nutrient.unitName?.equals("kcal", ignoreCase = true) == true
        } ?: error("Không tìm thấy Energy (kcal) trong USDA details")

        return energy.amount ?: error("Energy.amount null")
    }

    private fun stripMarkdownFences(s: String): String {
        var t = s.trim()
        t = t.removePrefix("```json").trim()
        t = t.removePrefix("```").trim()
        t = t.removeSuffix("```").trim()
        return t
    }

    private fun buildPrompt(foodDescription: String, grams: Double, kcalTotal: Double): String {
        return "Trả lời NGẮN và KHÔNG giải thích dài dòng, bỏ qua các câu nối từ và trả lời theo đúng mẫu." +
                "Input: food=$foodDescription; grams=$grams; kcal=$kcalTotal. " +
                "Yêu cầu trả lời: Món [Tên món ăn] chứa nhiều [dưỡng chất mà món đó cung cấp]; Nên ăn vào các bữa: [sáng, trưa, tối, ăn nhẹ]; Lượng thức ăn khuyên dùng mỗi bữa: [số nguyên làm tròn đến 5g gần nhất] = Lượng kcal của phần thức ăn đó."
    }
}