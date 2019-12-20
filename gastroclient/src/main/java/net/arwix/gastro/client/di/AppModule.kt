package net.arwix.gastro.client.di

import android.content.Context.MODE_PRIVATE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import net.arwix.gastro.client.ui.history.check.HistoryCheckDetailViewModel
import net.arwix.gastro.client.ui.history.order.HistoryOrderDetailViewModel
import net.arwix.gastro.client.ui.order.OrderViewModel
import net.arwix.gastro.client.ui.pay.PayViewModel
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.client.ui.report.day.ReportDayUseCase
import net.arwix.gastro.client.ui.table.OpenTablesViewModel
import net.arwix.gastro.library.data.FirestoreDbApp
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    single { androidApplication().getSharedPreferences("AppPref", MODE_PRIVATE) }

    single {
        val firebaseFirestore = Firebase.firestore(Firebase.app).apply {
            clearPersistence()
        }
        FirestoreDbApp("test-", firebaseFirestore)
    }

    single {
        ReportDayUseCase(get(), androidContext())
    }

    viewModel { OpenTablesViewModel(get()) }
    viewModel { OrderViewModel(get(), get(), androidContext()) }
    viewModel { ProfileViewModel() }
    viewModel { PayViewModel(get()) }
    viewModel { HistoryCheckDetailViewModel(get()) }
    viewModel { HistoryOrderDetailViewModel(get(), get()) }
}
