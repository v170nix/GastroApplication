package net.arwix.gastro.admin

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.data.FirestoreDbApp
import org.koin.android.ext.android.inject

class AdminActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val dbApp: FirestoreDbApp by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        appBarConfiguration =
            AppBarConfiguration.Builder(navController.graph).setDrawerLayout(drawer_layout).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.colorAppBar
                )
            )
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard()
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }
}
