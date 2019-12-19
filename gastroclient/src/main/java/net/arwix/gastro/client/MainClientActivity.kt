package net.arwix.gastro.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        requestRuntimePermission()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        hideKeyboard()
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }

    private fun requestRuntimePermission() {
        val permissionStorage =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionLocation =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val requestPermissions: MutableList<String> = ArrayList()

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (requestPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                requestPermissions.toTypedArray(),
                100
            )
        }
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
            R.id.menu_day_report -> {
                if (profileViewModel.isLogin()) {
                    findNavController(
                        this,
                        R.id.nav_host_fragment
                    ).navigate(R.id.reportDayFragment)
                }
            }
            R.id.order_menu -> {
                if (profileViewModel.isLogin()) {
                    findNavController(
                        this,
                        R.id.nav_host_fragment
                    ).navigate(R.id.historyOrderDetailFragment)
                }
            }
        }
        return false
    }

}
