package com.azyoot.relearn.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.di.ui.AdapterSubcomponent
import com.azyoot.relearn.ui.main.relearn.ReLearnCardViewHolder
import com.azyoot.relearn.ui.main.relearn.ReLearnCardViewModel
import com.azyoot.relearn.ui.main.relearn.ReLearnCardViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.min

class ReLearnAdapter(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val sourceCount: Int
) : RecyclerView.Adapter<ReLearnCardViewHolder>() {

    @Inject
    lateinit var viewModelProvider: Provider<ReLearnCardViewModel>

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job)
    private val component: AdapterSubcomponent = (context.applicationContext as ReLearnApplication)
        .appComponent
        .adapterSubcomponentFactory()
        .create(coroutineScope)

    private val viewModels = mutableListOf<ReLearnCardViewModel>()

    init {
        component.inject(this)

        repeat(itemCount) {
            viewModels.add(setupViewModel(it))
        }
    }

    private fun setupViewModel(position: Int) = viewModelProvider.get().apply {
        stateLiveData.observe(viewLifecycleOwner, Observer {
            notifyItemChanged(position)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReLearnCardViewHolder {
        val binding =
            ItemRelearnCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReLearnCardViewHolder(binding, component)
    }

    override fun getItemCount() = min(sourceCount, MAX_HISTORY) + 1

    override fun onBindViewHolder(holder: ReLearnCardViewHolder, position: Int) {
        val viewModel = viewModels[position]
        if (isNextReLearn(position)) {
            if(viewModel.currentState is ReLearnCardViewState.Initial) {
                viewModel.loadNextReLearn()
            }
        } else {
            if(viewModel.currentState is ReLearnCardViewState.Initial) {
                viewModel.loadNthHistory(itemCount - position - 1)
            }
        }
        holder.bind(viewModel.currentState)
    }

    private fun isNextReLearn(position: Int) = position == itemCount - 1

    override fun onViewDetachedFromWindow(holder: ReLearnCardViewHolder) {
        super.onViewDetachedFromWindow(holder)
        job.cancelChildren()
    }

    companion object {
        const val MAX_HISTORY = 10
    }
}
