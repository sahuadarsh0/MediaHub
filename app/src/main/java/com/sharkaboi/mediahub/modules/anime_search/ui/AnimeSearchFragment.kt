package com.sharkaboi.mediahub.modules.anime_search.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.sharkaboi.mediahub.BottomNavGraphDirections
import com.sharkaboi.mediahub.R
import com.sharkaboi.mediahub.common.extensions.debounce
import com.sharkaboi.mediahub.common.extensions.showToast
import com.sharkaboi.mediahub.databinding.FragmentAnimeSearchBinding
import com.sharkaboi.mediahub.modules.anime_search.adapters.AnimeSearchListAdapter
import com.sharkaboi.mediahub.modules.anime_search.adapters.AnimeSearchLoadStateAdapter
import com.sharkaboi.mediahub.modules.anime_search.vm.AnimeSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnimeSearchFragment : Fragment() {
    private var _binding: FragmentAnimeSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var animeSearchListAdapter: AnimeSearchListAdapter
    private val animeSearchViewModel by viewModels<AnimeSearchViewModel>()
    private val navController by lazy { findNavController() }
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        searchJob = null
        binding.rvSearchResults.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setObservers()
    }

    private fun setUpRecyclerView() {
        binding.rvSearchResults.apply {
            animeSearchListAdapter = AnimeSearchListAdapter { animeId ->
                val action = BottomNavGraphDirections.openAnimeById(animeId)
                navController.navigate(action)
            }
            layoutManager = GridLayoutManager(context, 3)
            itemAnimator = DefaultItemAnimator()
            adapter = animeSearchListAdapter.withLoadStateFooter(
                footer = AnimeSearchLoadStateAdapter()
            )
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            animeSearchListAdapter.addLoadStateListener { loadStates ->
                if (loadStates.source.refresh is LoadState.Error) {
                    showToast((loadStates.source.refresh as LoadState.Error).error.message)
                }
                binding.progress.isShowing = loadStates.refresh is LoadState.Loading
                binding.searchEmptyView.root.isVisible =
                    loadStates.refresh is LoadState.NotLoading && animeSearchListAdapter.itemCount == 0
                binding.searchEmptyView.tvHint.text =
                    getString(R.string.anime_search_no_result_hint)
            }
        }
        val debounce = debounce<CharSequence>(scope = lifecycleScope) {
            searchAnime(it)
        }
        binding.svSearch.doOnTextChanged { query, _, _, _ ->
            debounce(query)
        }
    }

    private fun searchAnime(query: CharSequence?) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            query?.toString()?.let {
                if (it.length < 3) {
                    binding.searchEmptyView.root.isVisible = true
                    binding.searchEmptyView.tvHint.text = getString(R.string.anime_search_hint)
                    animeSearchListAdapter.submitData(PagingData.empty())
                    return@launch
                }
                hideKeyboard()
                animeSearchViewModel.getAnime(it.trim())
                    .collectLatest { pagingData ->
                        animeSearchListAdapter.submitData(pagingData)
                        binding.rvSearchResults.scrollToPosition(0)
                    }
            }
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager? =
            context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.svSearch.windowToken, 0)
    }
}
