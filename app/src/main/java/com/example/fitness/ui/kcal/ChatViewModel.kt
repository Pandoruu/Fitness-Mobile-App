package com.example.fitness.ui.kcal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitness.data.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ChatViewModel(
    private val repo: NutritionRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ChatItem>>(emptyList())
    val items: StateFlow<List<ChatItem>> = _items

    fun onSearchClicked(rawInput: String) {
        val parsed = runCatching { parseInput(rawInput) }
        if (parsed.isFailure) {
            val message = parsed.exceptionOrNull()?.message ?: "Input không hợp lệ"
            _items.value = _items.value + ChatItem(isUser = false, text = "Error: $message")
            return
        }
        val (foodQuery, grams) = parsed.getOrThrow()

        // add user message
        _items.value = _items.value + ChatItem(isUser = true, text = "$foodQuery ($grams g)")

        viewModelScope.launch {
            runCatching {
                repo.suggestFromUserInput(foodQuery = foodQuery, grams = grams)
            }.onSuccess { json ->
                _items.value = _items.value + ChatItem(isUser = false, text = json)
            }.onFailure { e ->
                _items.value = _items.value + ChatItem(isUser = false, text = formatErrorMessage(e))
            }
        }
    }

    private fun formatErrorMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> {
                val body = error.response()?.errorBody()?.string().orEmpty().trim()
                if (body.isNotEmpty()) {
                    "Error ${error.code()}: $body"
                } else {
                    "Error ${error.code()}: ${error.message()}"
                }
            }

            else -> "Error: ${error.message ?: "Unknown error"}"
        }
    }

    private fun parseInput(raw: String): Pair<String, Double> {
        val input = raw.trim()
        require(input.isNotBlank()) { "Vui lòng nhập món ăn và khối lượng." }

        val parts = input.split(Regex("\\s+"))
        require(parts.size >= 2) { "Nhập theo dạng: <food> <grams>. Ví dụ: rice 150" }

        val gramsRaw = parts.last().lowercase().removeSuffix("g").replace(",", ".")
        val grams = gramsRaw.toDoubleOrNull()
            ?: error("Grams không hợp lệ. Ví dụ đúng: rice 150 hoặc rice 150g")

        val query = parts.dropLast(1).joinToString(" ")
        require(query.isNotBlank()) { "Tên món ăn không hợp lệ" }
        return query to grams
    }

    class ChatViewModelFactory(
        private val repo: NutritionRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}