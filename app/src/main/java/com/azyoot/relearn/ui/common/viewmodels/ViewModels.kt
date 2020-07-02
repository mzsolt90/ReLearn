package com.azyoot.relearn.ui.common.viewmodels

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.reflect.KClass

@MainThread
inline fun <reified VM : ViewModel> ViewModelStoreOwner.lifecycleScoped(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: () -> ViewModelProvider.Factory
) = ViewModelLazy(
    VM::class,
    { ownerProducer().viewModelStore },
    factoryProducer
)

class ViewModelLazy<VM : ViewModel>(
    private val viewModelClass: KClass<VM>,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                ViewModelProvider(store, factory).get(viewModelClass.java).also {
                    cached = it
                }
            } else {
                viewModel
            }
        }

    override fun isInitialized() = cached != null
}