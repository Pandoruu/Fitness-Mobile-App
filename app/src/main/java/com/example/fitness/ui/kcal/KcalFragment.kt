package com.example.fitness.ui.kcal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness.databinding.FragmentKcalBinding
import com.example.fitness.data.NutritionRepository
import com.example.fitness.data.remote.api.*
import kotlinx.coroutines.launch

class KcalFragment : Fragment() {
    private var _binding : FragmentKcalBinding ? = null
    private val binding get() = _binding!!

    private val chatAdapter = ChatAdapter()

    // tạo repo
    private val repo: NutritionRepository by lazy {
        val usdaApi = ApiClients.usdaRetrofit.create(UsdaApi::class.java)
        val geminiApi = ApiClients.geminiRetrofit.create(GeminiApi::class.java)
        NutritionRepository(usdaApi = usdaApi, geminiApi = geminiApi)
    }

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModel.ChatViewModelFactory(repo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKcalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        // Click button
        binding.btnSearch.setOnClickListener {
            val rawText: String = binding.editText.text.toString()
            viewModel.onSearchClicked(rawText)
        }

        // Call API
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.collect {
                items -> chatAdapter.submitList(items)
                if (items.isNotEmpty()) {
                    binding.recyclerView.scrollToPosition(items.lastIndex)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}