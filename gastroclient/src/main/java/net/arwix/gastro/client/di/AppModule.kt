package net.arwix.gastro.client.di

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import net.arwix.gastro.client.ui.order.OrderViewModel
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    single { Firebase.firestore(Firebase.app) }

    viewModel { OrderViewModel(get()) }
    viewModel { ProfileViewModel() }
}
