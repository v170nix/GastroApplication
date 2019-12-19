package net.arwix.gastro.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import net.arwix.gastro.client.common.AppMenuHelper
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.library.common.hideKeyboard
import org.koin.android.viewmodel.ext.android.viewModel

class MainClientActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private val profileViewModel by viewModel<ProfileViewModel>()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private var destination: NavDestination? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_client)
        navController = findNavController(this, R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        profileViewModel.liveState.observe(this, Observer(this::renderProfile))
        requestRuntimePermission()
    }

    override fun onSupportNavigateUp(): Boolean {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                profileViewModel.nonCancelableIntent(ProfileViewModel.Action.Logout)
                return true
            }
            R.id.menu_history_checks ->
                if (profileViewModel.isLogin()) {
                    navigateTo(R.id.historyCheckDetailFragment, true)
                    return true
                }
            R.id.menu_report_day ->
                if (profileViewModel.isLogin()) {
                    navigateTo(R.id.reportDayFragment, true)
                    return true
                }
            R.id.menu_history_orders ->
                if (profileViewModel.isLogin()) {
                    navigateTo(R.id.historyOrderDetailFragment, true)
                    return true
                }
            R.id.menu_admin_menu_edit ->
                if (profileViewModel.isLogin()) {
                    navigateTo(R.id.adminMenuEditFragment, true)
                    return true
                }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateTo(@IdRes resId: Int, inclusive: Boolean = false) {
        hideKeyboard()
        val navOptions =
            if (inclusive) NavOptions.Builder().setPopUpTo(resId, true).build() else null
        findNavController(this, R.id.nav_host_fragment).navigate(resId, null, navOptions)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)
        menu?.forEach { it.isVisible = false }
        MenuCompat.setGroupDividerEnabled(menu, true)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        navController.currentDestination?.id?.run {
            AppMenuHelper.updateVisibleMenu(this, menu)
            supportActionBar?.let {
                AppMenuHelper.updateActionBar(this@MainClientActivity, this, it)
            }
            return true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(this)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        this.destination = destination
        invalidateOptionsMenu()
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        if (state.isLogin) {
            navigateTo(R.id.openTablesFragment, true)
            return
        }
        if (navController.currentDestination?.id != R.id.signInFragment
        ) navigateTo(R.id.signInFragment, true)
    }

}
