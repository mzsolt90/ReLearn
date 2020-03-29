package com.azyoot.relearn.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.databinding.ItemHistoryWebpageBinding
import com.azyoot.relearn.domain.entity.WebpageVisit
import java.time.format.DateTimeFormatter

class WebpageVisitViewHolder(private val binding: ItemHistoryWebpageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(webpageVisit: WebpageVisit) {
        binding.time.text = webpageVisit.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        binding.url.text = webpageVisit.url
    }
}

class WebpageVisitAdapter(private val context: Context) :
    ListAdapter<WebpageVisit, WebpageVisitViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WebpageVisitViewHolder(
            ItemHistoryWebpageBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: WebpageVisitViewHolder, position: Int) {
        getItem(position)?.also { holder.bind(it) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WebpageVisit>() {
            override fun areItemsTheSame(oldItem: WebpageVisit, newItem: WebpageVisit) =
                oldItem.databaseId == newItem.databaseId

            override fun areContentsTheSame(oldItem: WebpageVisit, newItem: WebpageVisit) =
                oldItem == newItem
        }
    }
}