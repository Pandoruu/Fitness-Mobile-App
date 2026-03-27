package com.example.fitness.data.remote.model

data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig(
        temperature = 0.4,
        maxOutputTokens = 1000
    )
)

data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

data class GeminiCandidate(
    val content: GeminiCandidateContent? = null
)

data class GeminiCandidateContent(
    val parts: List<GeminiPart> = emptyList()
)

data class GeminiPart(
    val text: String? = null
)

data class GeminiGenerationConfig(
    val temperature: Double,
    val maxOutputTokens: Int
)

