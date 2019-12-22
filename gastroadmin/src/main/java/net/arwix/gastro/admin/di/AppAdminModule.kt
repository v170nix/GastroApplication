package net.arwix.gastro.admin.di

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import net.arwix.gastro.admin.feature.menu.ui.AdminMenuGroupViewModel
import net.arwix.gastro.admin.feature.profile.ui.AdminProfileViewModel
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.menu.data.MenuRepository
import net.arwix.gastro.library.menu.domain.MenuUseCase
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AppAdminModule = module {

    single {
        //        val firebaseFirestore =
        val firebaseFirestore = Firebase.firestore(Firebase.app).apply {
            clearPersistence()
        }
        FirestoreDbApp("test-", firebaseFirestore)
    }

    single { MenuUseCase(MenuRepository(get<FirestoreDbApp>().refs.menu)) }

    viewModel { AdminProfileViewModel() }
    viewModel { AdminMenuGroupViewModel(get()) }
}
