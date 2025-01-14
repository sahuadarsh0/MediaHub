package com.sharkaboi.mediahub.modules.manga_details.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sharkaboi.mediahub.common.constants.UIConstants
import com.sharkaboi.mediahub.data.api.models.manga.MangaByIDResponse
import com.sharkaboi.mediahub.databinding.AnimeListItemBinding

class RelatedAnimeAdapter(private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<RelatedAnimeAdapter.RelatedAnimeViewHolder>() {

    private val diffUtilItemCallback =
        object : DiffUtil.ItemCallback<MangaByIDResponse.RelatedAnime>() {
            override fun areItemsTheSame(
                oldItem: MangaByIDResponse.RelatedAnime,
                newItem: MangaByIDResponse.RelatedAnime
            ): Boolean {
                return oldItem.node.id == newItem.node.id
            }

            override fun areContentsTheSame(
                oldItem: MangaByIDResponse.RelatedAnime,
                newItem: MangaByIDResponse.RelatedAnime
            ): Boolean {
                return oldItem == newItem
            }
        }

    private val listDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    private lateinit var binding: AnimeListItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedAnimeViewHolder {
        binding = AnimeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RelatedAnimeViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: RelatedAnimeViewHolder, position: Int) {
        holder.bind(listDiffer.currentList[position])
    }

    override fun getItemCount() = listDiffer.currentList.size

    fun submitList(list: List<MangaByIDResponse.RelatedAnime>) {
        listDiffer.submitList(list)
    }

    class RelatedAnimeViewHolder(
        private val binding: AnimeListItemBinding,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MangaByIDResponse.RelatedAnime) {
            binding.root.setOnClickListener {
                onClick(item.node.id)
            }
            binding.tvAnimeName.text = item.node.title
            binding.tvEpisodesWatched.text = item.relationTypeFormatted
            binding.cardRating.isGone = true
            binding.ivAnimeBanner.load(
                uri = item.node.mainPicture?.large ?: item.node.mainPicture?.medium,
                builder = UIConstants.AnimeImageBuilder
            )
        }
    }
}
