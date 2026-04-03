package com.example.fitness.ui.kcal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitness.data.NutritionRepository
import com.example.fitness.data.app_model.ChatItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException

class ChatViewModel(
    private val repo: NutritionRepository
) : ViewModel() {

    companion object {
        private const val DEFAULT_GRAMS = 100.0
        private val FOOD_NAME_REGEX = Regex("^[A-Za-z][A-Za-z\\s'\\-]{1,79}$")
    }

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
        _items.value = _items.value + ChatItem(isUser = true, text = foodQuery)

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
            is IllegalArgumentException -> "Error: ${error.message ?: "Input không hợp lệ"}"
            is IOException -> "Error: Không thể kết nối mạng. Hãy thử lại."
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
        require(input.isNotBlank()) { "Vui lòng nhập tên món ăn bằng tiếng Anh. Ví dụ: chicken breast" }
        require(input.none { it.isDigit() }) {
            "Không cần nhập khối lượng. Chỉ nhập tên món ăn, ví dụ: brown rice"
        }

        val query = input.replace(Regex("\\s+"), " ")
        require(FOOD_NAME_REGEX.matches(query)) {
            "Tên món ăn chỉ gồm chữ cái tiếng Anh và khoảng trắng. Ví dụ: grilled salmon"
        }
        return query to DEFAULT_GRAMS
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