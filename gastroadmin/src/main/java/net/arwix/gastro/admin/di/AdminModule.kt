package net.arwix.gastro.admin.di

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import net.arwix.gastro.admin.feature.profile.ui.AdminProfileViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AppAdminModule = module {

    single {
        //        val firebaseFirestore =
        Firebase.firestore(Firebase.app).apply {
            clearPersistence()
        }
//        FirestoreDbApp("test-", firebaseFirestore)
    }

    viewModel { AdminProfileViewModel() }
}
