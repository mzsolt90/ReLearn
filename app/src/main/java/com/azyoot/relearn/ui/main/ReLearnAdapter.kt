package com.azyoot.relearn.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.di.ui.AdapterSubcomponent
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.main.relearn.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.min

sealed class ReLearnAdapterActions {
    data class LaunchReLearn(val reLearnTranslation: ReLearnTranslation): ReLearnAdapterActions()
}

class ReLearnAdapter(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val sourceCount: Int
) : RecyclerView.Adapter<ReLearnBaseViewHolder>() {

    @Inject
    lateinit var viewModelProvider: Provider<ReLearnCardViewModel>

    private val actionsInternal = MutableLiveData<ReLearnAdapterActions>()
    val actionsLiveData: LiveData<ReLearnAdapterActions>
    get() = actionsInternal

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job)
    private val component: AdapterSubcomponent = (context.applicationContext as ReLearnApplication)
        .appComponent
        .adapterSubcomponentFactory()
        .create(coroutineScope)

    private val viewModels = mutableListOf<ReLearnCardViewModel>()

    init {
        component.inject(this)

        Timber.v("Setting up viewmodels")
        repeat(itemCount) {
            viewModels.add(setupViewModel(it))
        }
    }

    private fun setupViewModel(position: Int) = viewModelProvider.get().apply {
        stateLiveData.observe(viewLifecycleOwner, Observer {
            notifyItemChanged(position)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            ITEM_TYPE_NEXT -> ReLearnNextCardViewHolder(
                ItemRelearnCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), component
            )
            ITEM_TYPE_HISTORY -> ReLearnHistoryCardViewHolder(
                ItemRelearnHistoryCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), component
            )
            else -> throw NotImplementedError("View type is not supported")
        }


    override fun getItemCount() = min(sourceCount, MAX_HISTORY) + 1

    override fun onBindViewHolder(holder: ReLearnBaseViewHolder, position: Int) {
        Timber.v("Binding view holder at position: $position")
        val viewModel = viewModels[position]
        if (isNextReLearn(position)) {
            if (viewModel.currentState is ReLearnCardViewState.Initial) {
                viewModel.loadNextReLearn()
            }
        } else {
            if (viewModel.currentState is ReLearnCardViewState.Initial) {
                viewModel.loadNthHistory(itemCount - position - 1)
            }
        }
        holder.bind(viewModel.currentState)
        holder.actionsLiveData.observe(viewLifecycleOwner, Observer {
            handleAction(it, position)
        })
    }

    private fun handleAction(action: ReLearnAction, position: Int){
        Timber.v("Adapter action $action at position $position")
        if(!isNextReLearn(position)) return
        val viewModel = viewModels[position]
        val state = viewModel.currentState
        if(state !is ReLearnCardViewState.Finished) return

        when(action){
            ReLearnAction.ViewReLearn -> launchReLearn(state.reLearnTranslation)
        }
    }

    private fun launchReLearn(relearn: ReLearnTranslation){
        actionsInternal.postValue(ReLearnAdapterActions.LaunchReLearn(relearn))
    }

    override fun onViewRecycled(holder: ReLearnBaseViewHolder) {
        super.onViewRecycled(holder)
        Timber.v("Recycling view holder")
        holder.actionsLiveData.removeObservers(viewLifecycleOwner)
    }

    override fun getItemViewType(position: Int) =
        if (isNextReLearn(position)) ITEM_TYPE_NEXT else ITEM_TYPE_HISTORY

    private fun isNextReLearn(position: Int) = position == itemCount - 1

    override fun onViewDetachedFromWindow(holder: ReLearnBaseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        job.cancelChildren()
    }

    companion object {
        const val MAX_HISTORY = 10

        private const val ITEM_TYPE_HISTORY = 1
        private const val ITEM_TYPE_NEXT = 2
    }
}
