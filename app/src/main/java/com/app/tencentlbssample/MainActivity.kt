package com.app.tencentlbssample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.tencentlbssample.databinding.ActivityMainBinding
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest

class MainActivity : AppCompatActivity(), TencentLocationListener {
    private val mLocationManager by lazy { TencentLocationManager.getInstance(this) }
    private lateinit var mLocationRequest: TencentLocationRequest
    private lateinit var mViewBinding: ActivityMainBinding
    private lateinit var mActivityResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        mLocationRequest = TencentLocationRequest.create().apply {
            interval = 2000
            isAllowGPS = true
            requestLevel = TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA
            isAllowDirection = true
            isIndoorLocationMode = true
        }

        mViewBinding.btnStartLocation.setOnClickListener {
            if (checkAndRequestLocationPermission()) {
                startLocation()
            }
        }

        mViewBinding.btnStopLocation.setOnClickListener {
            stopLocation()
        }

        mActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // ???????????????????????????????????????????????? resultCode ????????? RESULT_CANCELED
                if (PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(this, PERMISSION_LOCATION)
                ) {
                    startLocation()
                }
            }
    }

    private fun startLocation() {
        mLocationManager.requestLocationUpdates(mLocationRequest, this)
    }

    private fun stopLocation() {
        mLocationManager.removeUpdates(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation()
            } else {
                if (shouldShowRequestPermissionRationale(PERMISSION_LOCATION)) {
                    showPermissionRationale()
                } else {
                    showSettingDialog()
                }
            }
        }
    }

    override fun onLocationChanged(location: TencentLocation, error: Int, reason: String) {
        Log.i(TAG, "onLocationChanged???location: ${location.address} error: $error reason: $reason")
    }

    override fun onStatusUpdate(name: String, status: Int, desc: String) {
        Log.i(TAG, "onStatusUpdate???name: $name status: $status desc: $desc")
    }

    private fun checkAndRequestLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (PackageManager.PERMISSION_GRANTED ==
            ContextCompat.checkSelfPermission(this, PERMISSION_LOCATION)
        ) {
            return true
        }
        if (shouldShowRequestPermissionRationale(PERMISSION_LOCATION)) {
            showPermissionRationale()
        } else {
            requestPermissions(arrayOf(PERMISSION_LOCATION), RC_PERMISSION_LOCATION)
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setMessage("?????????????????????")
            .setNegativeButton("??????", null)
            .setPositiveButton("??????") { _, _ ->
                requestPermissions(arrayOf(PERMISSION_LOCATION), RC_PERMISSION_LOCATION)
            }
            .show()
    }

    private fun showSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage("?????????????????????????????????????????????")
            .setPositiveButton("??????") { _, _ -> openSetting() }
            .show()
    }

    private fun openSetting() {
        mActivityResult.launch(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
    }

    companion object {
        private const val TAG = "TencentLBS"
        private const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val RC_PERMISSION_LOCATION = 10
    }
}