package net.arwix.gastro.client.di

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import net.arwix.gastro.client.data.CheckRepository
import net.arwix.gastro.client.data.OpenTableRepository
import net.arwix.gastro.client.data.OrderRepository
import net.arwix.gastro.client.domain.InnerFragmentStateViewModel
import net.arwix.gastro.client.feature.order.domain.OrderUseCase
import net.arwix.gastro.client.feature.order.ui.OrderViewModel
import net.arwix.gastro.client.feature.table.domain.OpenTableUseCase
import net.arwix.gastro.client.feature.table.ui.OpenTableViewModel
import net.arwix.gastro.client.ui.history.check.HistoryCheckDetailViewModel
import net.arwix.gastro.client.ui.history.order.HistoryOrderDetailViewModel
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.client.ui.report.day.ReportDayUseCase
import net.arwix.gastro.client.ui.table.OpenTablesViewModel
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.menu.data.MenuRepository
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    single {
        val firebaseFirestore = Firebase.firestore(Firebase.app).apply {
            clearPersistence()
        }
        FirestoreDbApp("test-", firebaseFirestore)
    }

    single { MenuRepository(get<FirestoreDbApp>().refs.menu) }
    single { ReportDayUseCase(get(), androidContext()) }
    single { OpenTableRepository(get<FirestoreDbApp>().refs.openTables) }
    single { OrderRepository(get<FirestoreDbApp>().refs.orders) }
    single { OrderUseCase(get(), get(), get()) }
    single { CheckRepository(get<FirestoreDbApp>().refs.checks) }
    single { OpenTableUseCase(get(), get(), get(), get(), get()) }

    viewModel { InnerFragmentStateViewModel() }
    viewModel { ProfileViewModel() }
    viewModel { OpenTablesViewModel(get()) }
    viewModel { OrderViewModel(androidContext(), get(), get()) }
    viewModel { OpenTableViewModel(get(), get(), get()) }
    viewModel { HistoryCheckDetailViewModel(get()) }
    viewModel { HistoryOrderDetailViewModel(get()) }
}
