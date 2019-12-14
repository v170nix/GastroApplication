package net.arwix.gastro.client.di

import net.arwix.gastro.client.ui.order.OrderViewModel
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel { OrderViewModel() }
    viewModel { ProfileViewModel() }
}
