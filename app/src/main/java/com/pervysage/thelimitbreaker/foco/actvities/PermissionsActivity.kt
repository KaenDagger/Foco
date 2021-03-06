package com.pervysage.thelimitbreaker.foco.actvities

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.pervysage.thelimitbreaker.foco.R
import kotlinx.android.synthetic.main.activity_permissions.*

class PermissionsActivity : AppCompatActivity(), View.OnClickListener {

    private var permResult = false
    private val PERM_REQUEST = 1

    private var dndAccess = false
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnGrantPerm -> {

                var permissions = arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.READ_CALL_LOG,
                        android.Manifest.permission.CALL_PHONE,
                        android.Manifest.permission.SEND_SMS,
                        android.Manifest.permission.WAKE_LOCK
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    permissions = arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.READ_CONTACTS,
                            android.Manifest.permission.READ_PHONE_STATE,
                            android.Manifest.permission.READ_CALL_LOG,
                            android.Manifest.permission.CALL_PHONE,
                            android.Manifest.permission.SEND_SMS,
                            android.Manifest.permission.WAKE_LOCK,
                            android.Manifest.permission.ANSWER_PHONE_CALLS
                    )
                }
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            permissions,
                            PERM_REQUEST
                    )
                }
            }

            R.id.btnNext -> {
                startActivity(Intent(this, MainActivity::class.java)
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        })
            }
            R.id.btnGrantDND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please grant LOCATION Permission", Toast.LENGTH_SHORT).show()
            }
            if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please grant READ CONTACTS Permission", Toast.LENGTH_SHORT).show()
            }
            if (grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please grant PHONE STATE Permission", Toast.LENGTH_SHORT).show()
            }
            if (grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please grant PHONE STATE Permission", Toast.LENGTH_SHORT).show()
            }
            if (grantResults[4] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please grant PHONE STATE Permission", Toast.LENGTH_SHORT).show()
            }
            if (grantResults[5] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please grant SMS Permission", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        val sharedPrefs = getSharedPreferences(getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().commit()
        btnNext.isEnabled = false
        btnGrantPerm.setOnClickListener(this)
        setGrantDND()
        setAutoStart()
    }

    private fun setGrantDND() {
        if (isDNDExist()) {
            btnGrantDND.setOnClickListener(this)
        } else {
            dndAccess = true
            btnGrantDND.visibility = View.GONE
            tvDNDAccess.visibility = View.GONE
        }
    }

    private fun isDNDExist(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val dndIntent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            val list = packageManager.queryIntentActivities(dndIntent, PackageManager.MATCH_DEFAULT_ONLY)
            list.size > 0
        } else {
            false
        }

    }

    private fun setAutoStart() {
        try {
            val autoStartIntent = Intent()
            val manufacturer = android.os.Build.MANUFACTURER
            when {
                "xiaomi".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                "oppo".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                "vivo".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                "Letv".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
                "Honor".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
            }

            val list = packageManager.queryIntentActivities(autoStartIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                btnAutoStart.setOnClickListener { startActivity(autoStartIntent) }
            } else {
                btnAutoStart.visibility = View.GONE
            }
        } catch (e: Exception) {
            btnAutoStart.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()


        dndAccess = if (isDNDExist()) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) notificationManager.isNotificationPolicyAccessGranted
            else true
        }else{
            true
        }

        permResult =
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

        if (permResult) {
            val drawable = ContextCompat.getDrawable(this, R.drawable.reg_background)?.mutate()
            drawable?.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.colorActive),
                    PorterDuff.Mode.SRC_ATOP
            )
            btnGrantPerm.background = drawable
            btnGrantPerm.text = "Done"
            btnGrantPerm.setOnClickListener(null)
        }

        if (isDNDExist() && dndAccess) {
            val drawable = ContextCompat.getDrawable(this, R.drawable.reg_background)!!.mutate()
            drawable.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.colorActive),
                    PorterDuff.Mode.SRC_ATOP
            )
            btnGrantDND.background = drawable
            btnGrantDND.text = "Done"
            btnGrantDND.setOnClickListener(null)
        }

        if (permResult && dndAccess) {
            btnNext.apply {
                isEnabled = true
                val drawable = ContextCompat.getDrawable(this@PermissionsActivity, R.drawable.reg_background)?.mutate()
                drawable?.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(this@PermissionsActivity, R.color.colorActive),
                        PorterDuff.Mode.SRC_ATOP
                )
                btnNext.background = drawable
                setOnClickListener(this@PermissionsActivity)
            }
        }
    }
}
