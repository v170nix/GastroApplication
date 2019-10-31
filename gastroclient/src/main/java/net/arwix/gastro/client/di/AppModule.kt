package net.arwix.gastro.client.di

import net.arwix.gastro.client.ui.order.OrderViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel { OrderViewModel() }
}
