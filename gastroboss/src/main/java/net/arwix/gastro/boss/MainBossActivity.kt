package net.arwix.gastro.boss

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI


class MainBossActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_boss)
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        requestRuntimePermission()
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun requestRuntimePermission() {
        val permissionStorage =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionLocation =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        val requestPermissions: MutableList<String> = ArrayList()

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (requestPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                requestPermissions.toTypedArray(),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val REQUEST_PERMISSION = 100
    }
}
