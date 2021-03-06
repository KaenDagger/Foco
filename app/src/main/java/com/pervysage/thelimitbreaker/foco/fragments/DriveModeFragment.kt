package com.pervysage.thelimitbreaker.foco.fragments


import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.actvities.MainActivity
import com.pervysage.thelimitbreaker.foco.actvities.MyContactsActivity
import com.pervysage.thelimitbreaker.foco.utils.DriveActivityRecogUtil
import com.pervysage.thelimitbreaker.foco.utils.sendDriveModeNotification
import kotlinx.android.synthetic.main.fragment_drive_mode.*


class DriveModeFragment : Fragment() {

    private var isFragEnabled = -1
    private lateinit var driveActivityRecogUtil: DriveActivityRecogUtil

    private var dmActiveGroup = "All Contacts"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        context?.run {
            driveActivityRecogUtil = DriveActivityRecogUtil(this)
        }
        return inflater.inflate(R.layout.fragment_drive_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val sharedPrefs = context?.getSharedPreferences(context?.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)

        val contactGroup = sharedPrefs?.getString(context?.resources?.getString(R.string.DM_ACTIVE_GROUP), "All Contacts")

        val groupAdapter = ArrayAdapter(context!!,
                R.layout.layout_spinner_item,
                arrayOf("All Contacts", "Priority Contacts", "None")
        )


        groupAdapter.setDropDownViewResource(R.layout.layout_spinner_dropdown_item)
        groupChooser.apply {
            adapter = groupAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    var group = "All Contacts"
                    when (position) {
                        0 -> {
                            tvInfoBox.text = "Receive calls from all contacts in your contact list"
                            btnSeePriority.visibility = View.GONE
                            group = "All Contacts"
                        }
                        1 -> {
                            tvInfoBox.text = "Receive calls from Priority People only"
                            btnSeePriority.visibility = View.VISIBLE
                            group = "Priority Contacts"
                        }
                        2 -> {
                            tvInfoBox.text = "Total Silence !"
                            btnSeePriority.visibility = View.GONE
                            group = "None"
                        }
                    }
                    dmActiveGroup = group
                    sharedPrefs?.edit()?.putString(context.getString(R.string.DM_ACTIVE_GROUP), group)?.commit()
                }
            }

            when (contactGroup) {
                "All Contacts" -> setSelection(0)
                "Priority Contacts" -> setSelection(1)
                "None" -> setSelection(2)
                else -> setSelection(0)
            }
        }

        btnSeePriority.setOnClickListener {
            startActivity(Intent(context, MyContactsActivity::class.java))
        }

        isFragEnabled = sharedPrefs?.getInt(context?.resources?.getString(R.string.DRIVE_MODE_ENABLED), -1) ?: 0
        applyState(isFragEnabled == 1, true)

        activity?.run {
            if (this is MainActivity) {
                setOnDMStatusChangeListener { applyState(it, false) }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }


    private fun applyState(state: Boolean, justInit: Boolean) {

        if (state) {
            isFragEnabled = 1

            context?.run {

                val sharedPrefs = this.getSharedPreferences(this.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)

                sharedPrefs.edit().putString(this.getString(R.string.DM_ACTIVE_GROUP), dmActiveGroup).commit()

                btnSeePriority.isEnabled = true

                val icSeePriority = ContextCompat.getDrawable(this, R.drawable.ic_person)

                icSeePriority?.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(this, R.color.colorGenDark),
                        PorterDuff.Mode.MULTIPLY
                )
                btnSeePriority.setCompoundDrawablesWithIntrinsicBounds(icSeePriority, null, null, null)

                ivDriveMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_scooter))

                tvContactGroupLabel.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))

                tvInfoBox.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
            }

            if (!justInit)
                driveActivityRecogUtil.startDriveModeRecog()

            groupChooser.isEnabled = true

        } else {
            isFragEnabled = 0

            if (!justInit)
                driveActivityRecogUtil.stopDriveModeRecog()

            context?.run {

                val sharedPrefs = this.getSharedPreferences(this.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)

                sharedPrefs.edit().putString(this.getString(R.string.DM_ACTIVE_GROUP), "").commit()

                btnSeePriority.isEnabled = false


                val icSeePriority = ContextCompat.getDrawable(this, R.drawable.ic_person)
                icSeePriority?.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(this, R.color.colorTextDisable),
                        PorterDuff.Mode.MULTIPLY
                )
                btnSeePriority.setCompoundDrawablesWithIntrinsicBounds(icSeePriority, null, null, null)

                val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                val dmStatus = sharedPrefs.getBoolean(this.getString(R.string.DM_STATUS), false)
                val geoStatus = sharedPrefs.getBoolean(this.getString(R.string.GEO_STATUS), false)

                if (dmStatus) {
                    sendDriveModeNotification("Drive Mode Stopped", "Service Stopped", false, this)
                    sharedPrefs.edit().putBoolean(getString(R.string.DM_STATUS), false).commit()
                }
                if (dmStatus && !geoStatus) {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    val maxVolume = (am.getStreamMaxVolume(AudioManager.STREAM_RING) * 0.90).toInt()
                    am.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_PLAY_SOUND)
                }


                ivDriveMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_scooter_disabled))

                tvContactGroupLabel.setTextColor(ContextCompat.getColor(this, R.color.colorTextDisable))

                tvInfoBox.setTextColor(ContextCompat.getColor(this, R.color.colorTextDisable))
            }

            groupChooser.isEnabled = false

        }

    }

}


