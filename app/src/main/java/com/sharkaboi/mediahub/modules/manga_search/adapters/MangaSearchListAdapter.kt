package com.sharkaboi.mediahub.modules.manga_search.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sharkaboi.mediahub.common.constants.UIConstants
import com.sharkaboi.mediahub.common.extensions.getRatingStringWithRating
import com.sharkaboi.mediahub.data.api.models.manga.MangaSearchResponse
import com.sharkaboi.mediahub.databinding.MangaListItemBinding

class MangaSearchListAdapter(
    private val onItemClick: (id: Int) -> Unit
) : PagingDataAdapter<MangaSearchResponse.Data, MangaSearchListAdapter.MangaSearchListViewHolder>(
    MangaSearchListItemComparator
) {

    inner class MangaSearchListViewHolder(private val mangaListItemBinding: MangaListItemBinding) :
        RecyclerView.ViewHolder(mangaListItemBinding.root) {
        fun bind(item: MangaSearchResponse.Data?) {
            item?.let {
                mangaListItemBinding.apply {
                    ivMangaBanner.load(
                        uri = it.node.mainPicture?.large ?: it.node.mainPicture?.medium,
                        builder = UIConstants.MangaImageBuilder
                    )
                    tvMangaName.text = it.node.title
                    tvChapsRead.isVisible = false
                    tvVolumesRead.isVisible = false
                    tvScore.text = tvScore.context.getRatingStringWithRating(it.node.meanScore)
                    root.setOnClickListener {
                        onItemClick(item.node.id)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MangaSearchListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaSearchListViewHolder {
        return MangaSearchListViewHolder(
            MangaListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    object MangaSearchListItemComparator : DiffUtil.ItemCallback<MangaSearchResponse.Data>() {
        override fun areItemsTheSame(
            oldItem: MangaSearchResponse.Data,
            newItem: MangaSearchResponse.Data
        ): Boolean {
            return oldItem.node.id == newItem.node.id
        }

        override fun areContentsTheSame(
            oldItem: MangaSearchResponse.Data,
            newItem: MangaSearchResponse.Data
        ): Boolean {
            return oldItem == newItem
        }
    }
}
