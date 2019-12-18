package net.arwix.gastro.client

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.library.common.hideKeyboard
import org.koin.android.viewmodel.ext.android.viewModel

class MainClientActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val profileViewModel by viewModel<ProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_client)
        val navController = findNavController(this, R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        profileViewModel.liveState.observe(this, Observer(this::renderProfile))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        hideKeyboard()
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        if (state.isLogin) {
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.openTablesFragment, true).build()
            hideKeyboard()
            findNavController(this, R.id.nav_host_fragment)
                .navigate(R.id.openTablesFragment, null, navOptions)
        } else {
            if (findNavController(
                    this,
                    R.id.nav_host_fragment
                ).currentDestination?.id != R.id.signInFragment
            ) {
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.signInFragment, true).build()
                hideKeyboard()
                findNavController(this, R.id.nav_host_fragment)
                    .navigate(R.id.signInFragment, null, navOptions)
            }
        }
//        if (state.error != null) {
//            password_input_layout.error = null
//            password_input_layout.isErrorEnabled = true
//            password_input_layout.error = "wrong password"
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_menu -> {
                profileViewModel.nonCancelableIntent(ProfileViewModel.Action.Logout)
                return true
            }
            R.id.check_menu -> {
                if (profileViewModel.isLogin()) {
                    findNavController(
                        this,
                        R.id.nav_host_fragment
                    ).navigate(R.id.checkDetailFragment)
                }
            }
        }
        return false
    }

}
