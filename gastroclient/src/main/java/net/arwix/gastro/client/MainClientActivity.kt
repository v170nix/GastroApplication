package net.arwix.gastro.client

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_main_client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.client.common.AppMenuHelper
import net.arwix.gastro.client.feature.notification.ui.ActivityNotificationHelper
import net.arwix.gastro.client.feature.print.ui.PrintIntentService
import net.arwix.gastro.client.ui.profile.ProfileViewModel
import net.arwix.gastro.library.common.CustomToolbarActivity
import net.arwix.gastro.library.common.hideKeyboard
import org.koin.android.viewmodel.ext.android.viewModel

class MainClientActivity : AppCompatActivity(),
    CustomToolbarActivity,
    NavController.OnDestinationChangedListener, CoroutineScope by MainScope() {

    private val profileViewModel by viewModel<ProfileViewModel>()
    private var printService: PrintIntentService? = null
    private var isBoundPrintService = false
    private lateinit var notificationHelper: ActivityNotificationHelper

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private var isDestinationChanged = false

    private val printConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service == null) {
                Toast.makeText(
                    this@MainClientActivity,
                    "print service connected false", Toast.LENGTH_LONG
                ).show()
                Crashlytics.logException(Exception("print service connected false"))
                return
            }
            printService = (service as PrintIntentService.PrintBinder).getService()

            launch {
                printService?.getResultAsFlow()?.collect {
                    notificationHelper.showNotification(it)
                }
//                materialCardView.scaleY = 0f
//                materialCardView.translationY = materialCardView.height.toFloat()
//                printService?.getResultAsFlow()?.collect {
//                    val text = when (it) {
//                        is PrintIntentService.PrintResult.Success -> "Success"
//                        is PrintIntentService.PrintResult.Error -> "Error ${it.printList}"
//                    }
//                    materialCardView.animate().withStartAction {
//                        materialCardView.visible()
//                    }
//                        .translationY(0f)
////                        .scaleY(1f)
//                        .start()
//                    delay(10000)
//                    materialCardView.animate()
////                        .scaleY(0f)
//                        .translationY(materialCardView.height.toFloat())
//                        .withEndAction { materialCardView.gone() }
//                        .start()
////                    Snackbar.make(this@MainClientActivity.findViewById<View>(android.R.id.content),
////                        text, Snackbar.LENGTH_LONG).show()
//                }
            }

            isBoundPrintService = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBoundPrintService = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_client)
        notificationHelper = ActivityNotificationHelper(notification_container, this)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//        setCustomToolbar(app_main_toolbar)
        navController = findNavController(this, R.id.nav_host_fragment)
//        navController.addOnDestinationChangedListener(this)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph)
            .build()
//        NavigationUI
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        profileViewModel.liveState.observe(this, Observer(this::renderProfile))
        requestRuntimePermission()
        runCatching {
            bindService(
                Intent(this, PrintIntentService::class.java),
                printConnection,
                Context.BIND_AUTO_CREATE
            )
        }.onFailure {
            Crashlytics.logException(it)
        }
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
                    navigateTo(R.id.historyCheckDetailFragment, false)
                    return true
                }
            R.id.menu_report_day ->
                if (profileViewModel.isLogin()) {
                    navigateTo(R.id.reportDayFragment, true)
                    return true
                }
            R.id.menu_history_orders ->
                if (profileViewModel.isLogin()) {
                    navigateTo(R.id.historyOrderDetailFragment, false)
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
//        menu?.forEach { it.isVisible = false }
        MenuCompat.setGroupDividerEnabled(menu, true)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        navController.currentDestination?.id?.run {
            //            AppMenuHelper.updateVisibleMenu(this, menu)
            supportActionBar?.let {
                AppMenuHelper.updateActionBar(this@MainClientActivity, this, it)
            }
            return true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        navController.addOnDestinationChangedListener(this)
    }

    override fun onStop() {
        super.onStop()
        navController.removeOnDestinationChangedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBoundPrintService) {
            unbindService(printConnection)
            isBoundPrintService = false
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        isDestinationChanged = true
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

    override fun setCustomToolbar(toolbar: Toolbar?) {
//        if (toolbar == null && isDestinationChanged) {
//            setSupportActionBar(app_main_toolbar)
//            app_main_toolbar.visible()
//            isDestinationChanged = false
//            return
//        }
//        if (toolbar == null) return
//        if (toolbar != app_main_toolbar) {
//            app_main_toolbar.gone()
//        }
        setSupportActionBar(toolbar)
//        toolbar.visible()
//        isDestinationChanged = false
    }

}
