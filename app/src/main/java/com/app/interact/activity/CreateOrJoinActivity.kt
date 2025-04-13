package com.app.interact.activity

import android.Manifest
import com.app.interact.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.crashlytics.internal.Logger.TAG
import com.app.interact.fragment.CreateMeetingFragment
import com.app.interact.fragment.CreateOrJoinFragment
import com.app.interact.fragment.JoinMeetingFragment
import live.videosdk.rtc.android.CustomStreamTrack
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.lib.PeerConnectionUtils
import live.videosdk.rtc.android.mediaDevice.VideoDeviceInfo
import live.videosdk.rtc.android.permission.Permission
import live.videosdk.rtc.android.permission.PermissionHandler
import live.videosdk.rtc.android.permission.Permissions
import org.webrtc.Camera2Enumerator
import org.webrtc.PeerConnectionFactory
import org.webrtc.PeerConnectionFactory.InitializationOptions
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import androidx.core.content.ContextCompat
import org.webrtc.MediaStreamTrack

class CreateOrJoinActivity : AppCompatActivity() {
    var isMicEnabled = false
        private set
    var isWebcamEnabled = false
        private set
    private var btnMic: FloatingActionButton? = null
    private var btnWebcam: FloatingActionButton? = null
    private var joinView: VideoView? = null
    private var cameraOffText: TextView? = null
    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var videoTrack: CustomStreamTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var initializationOptions: InitializationOptions? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null



    private var videoSource: VideoSource? = null
    var permissionsGranted = false
    lateinit var optionsMenu: Menu
    private val permissionHandler: com.nabinbhandari.android.permissions.PermissionHandler = object : com.nabinbhandari.android.permissions.PermissionHandler() {
        override fun onGranted() {
        }

        override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
            super.onDenied(context, deniedPermissions)
            Toast.makeText(
                this@CreateOrJoinActivity,
                "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT
            ).show()
        }

        override fun onBlocked(context: Context, blockedList: ArrayList<String>): Boolean {
            Toast.makeText(
                this@CreateOrJoinActivity,
                "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT
            ).show()
            return super.onBlocked(context, blockedList)
        }


    }


    private val permissionHandlerSDK: PermissionHandler = object : PermissionHandler() {
        override fun onGranted() {
            permissionsGranted = true
            isMicEnabled = true
            btnMic!!.setImageResource(R.drawable.ic_mic_on)
            changeFloatingActionButtonLayout(btnMic, isMicEnabled)
            isWebcamEnabled = true
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera)
            changeFloatingActionButtonLayout(btnWebcam, isWebcamEnabled)
            updateCameraView(null)
        }

        override fun onBlocked(
            context: Context,
            blockedList: java.util.ArrayList<Permission>
        ): Boolean {
            for (blockedPermission in blockedList) {
                Log.d("VideoSDK Permission", "onBlocked: $blockedPermission")
            }
            return super.onBlocked(context, blockedList)
        }

        override fun onDenied(
            context: Context,
            deniedPermissions: java.util.ArrayList<Permission>
        ) {
            for (deniedPermission in deniedPermissions) {
                Log.d("VideoSDK Permission", "onDenied: $deniedPermission")
            }
            super.onDenied(context, deniedPermissions)
        }

        override fun onJustBlocked(
            context: Context,
            justBlockedList: java.util.ArrayList<Permission>,
            deniedPermissions: java.util.ArrayList<Permission>
        ) {
            for (justBlockedPermission in justBlockedList) {
                Log.d("VideoSDK Permission", "onJustBlocked: $justBlockedPermission")
            }
            super.onJustBlocked(context, justBlockedList, deniedPermissions)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VideoSDK.initialize(applicationContext)
        setContentView(R.layout.activity_create_or_join)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toolbar = findViewById(R.id.toolbar)
        toolbar!!.title = ""
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        btnMic = findViewById(R.id.btnMic)
        btnWebcam = findViewById(R.id.btnWebcam)
        joinView = findViewById(R.id.joiningView)
        cameraOffText = findViewById(R.id.cameraoff)
        checkPermissions()
        val fragContainer = findViewById<View>(R.id.fragContainer) as LinearLayout
        val ll = LinearLayout(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, CreateOrJoinFragment(), "CreateOrJoinFragment").commit()
        fragContainer.addView(ll)
        btnMic!!.setOnClickListener { toggleMic() }
        btnWebcam!!.setOnClickListener { toggleWebcam() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar,menu)
        optionsMenu = menu
        setAudioDeviceChangeListener()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    actionBar!!.setDisplayHomeAsUpEnabled(true)
                } else {
                    actionBar!!.setDisplayHomeAsUpEnabled(false)
                }
                toolbar!!.invalidate()
            }
            supportFragmentManager.popBackStack()
        }
        when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private var previousAvailableDevices: MutableList<String> = mutableListOf()
    private fun setAudioDeviceChangeListener(){
        VideoSDK.setAudioDeviceChangeListener { selectedAudioDevice, availableAudioDevices ->
            Log.d(TAG, "setAudioDeviceChangeListener: " + selectedAudioDevice.label)

            val currentAvailableDevices = availableAudioDevices.map { it.label }.toMutableList()
            Log.d(TAG, "Current available : $currentAvailableDevices")

            val addedDevices = currentAvailableDevices.filter { it !in previousAvailableDevices }
            Log.d(TAG, "Added audio devices: $addedDevices")

            val removedDevices = previousAvailableDevices.filter { it !in currentAvailableDevices }
            Log.d(TAG, "Removed audio devices: $removedDevices")

            previousAvailableDevices = currentAvailableDevices

            if(addedDevices.isNotEmpty() && addedDevices != previousAvailableDevices) {
                Toast.makeText(this, "$addedDevices Connected", Toast.LENGTH_SHORT).show()
            }
            if(removedDevices.isNotEmpty()){
                Toast.makeText(this,"$removedDevices Removed", Toast.LENGTH_SHORT).show()
            }
        }

    }


    fun createMeetingFragment() {
        setActionBar()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragContainer, CreateMeetingFragment(), "CreateMeetingFragment")
        ft.addToBackStack("CreateOrJoinFragment")
        ft.commit()
    }

    fun joinMeetingFragment() {
        setActionBar()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragContainer, JoinMeetingFragment(), "JoinMeetingFragment")
        ft.addToBackStack("CreateOrJoinFragment")
        ft.commit()
    }

    private fun setActionBar() {
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
        } else {
            throw NullPointerException("Something went wrong")
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun checkPermissions() {
        val permissionList: MutableList<String> = ArrayList()
        permissionList.add(Manifest.permission.INTERNET)
        permissionList.add(Manifest.permission.READ_PHONE_STATE)
        val options =
            com.nabinbhandari.android.permissions.Permissions.Options().sendDontAskAgainToSettings(false)
        com.nabinbhandari.android.permissions.Permissions.check(this, permissionList.toTypedArray(), null, options, permissionHandler)
        val permissionListSDK: MutableList<Permission> = ArrayList()
        permissionListSDK.add(Permission.audio)
        permissionListSDK.add(Permission.video)
        permissionListSDK.add(Permission.bluetooth)
        val optionsSDK = Permissions.Options().setRationaleDialogTitle("Info").setSettingsDialogTitle("Warning")
        VideoSDK.checkPermissions(this,
            permissionListSDK,
            optionsSDK,
            permissionHandlerSDK
        )
    }

    private fun changeFloatingActionButtonLayout(btn: FloatingActionButton?, enabled: Boolean) {
        if (enabled) {
            btn!!.setColorFilter(Color.BLACK)
            btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_grey_300))
        } else {
            btn!!.setColorFilter(Color.WHITE)
            btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_red_500))
        }
    }

    private fun toggleMic() {
        if (!permissionsGranted) {
            checkPermissions()
            return
        }
        isMicEnabled = !isMicEnabled
        if (isMicEnabled) {
            btnMic!!.setImageResource(R.drawable.ic_mic_on)
        } else {
            btnMic!!.setImageResource(R.drawable.ic_mic_off)
        }
        changeFloatingActionButtonLayout(btnMic, isMicEnabled)
    }

    private fun toggleWebcam() {
        if (!permissionsGranted) {
            checkPermissions()
            return
        }
        isWebcamEnabled = !isWebcamEnabled
        if (isWebcamEnabled) {
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera)
        } else {
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera_off)
        }
        updateCameraView(null)
        changeFloatingActionButtonLayout(btnWebcam, isWebcamEnabled)
    }


    private fun updateCameraView(videoDevice: VideoDeviceInfo?) {
        if (isWebcamEnabled) {
            cameraOffText?.visibility = View.GONE
            joinView!!.visibility = View.VISIBLE

            // Initialize PeerConnectionFactory and SurfaceTextureHelper once
            if (peerConnectionFactory == null) {
                initializationOptions = InitializationOptions.builder(this).createInitializationOptions()
                PeerConnectionFactory.initialize(initializationOptions)
                peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
            }

            SurfaceTextureHelper.create("CaptureThread", PeerConnectionUtils.getEglContext())

            // Create video capturer for the front camera using Camera2Enumerator
            val videoCapturer = createCameraCapturer()

            if (videoCapturer != null) {
                videoTrack = VideoSDK.createCameraVideoTrack(
                    "h720p_w960p",
                    "front",
                    CustomStreamTrack.VideoMode.TEXT,
                    true,
                    this,
                    videoDevice
                )
                // Display in localView
                joinView!!.addTrack(videoTrack!!.track as VideoTrack?)
            }
        } else {
            if (videoTrack?.track?.state() == MediaStreamTrack.State.LIVE) {
                videoTrack?.track?.setEnabled(false)  // Disable the track instead of disposing
            }
            videoTrack = null
            joinView!!.removeTrack()
            joinView!!.releaseSurfaceViewRenderer()
            joinView!!.visibility = View.INVISIBLE
            cameraOffText?.visibility = View.VISIBLE
        }
    }

    private fun createCameraCapturer(): VideoCapturer? {
        // Use Camera2Enumerator for better compatibility
        val enumerator = Camera2Enumerator(this)
        val deviceNames = enumerator.deviceNames

        // First, try to find front facing camera
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try to create capturer for back camera
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy crash")
        if(videoTrack?.track?.state() == MediaStreamTrack.State.LIVE)
        {
            Log.d(TAG, "onDestroyIf")
            videoTrack?.track?.dispose()
            videoTrack = null
        }
        joinView!!.removeTrack()
        joinView!!.releaseSurfaceViewRenderer()
        closeCapturer()
        super.onDestroy()
    }

    override fun onPause() {
        videoTrack?.track?.dispose()
        videoTrack = null
        joinView!!.removeTrack()
        joinView!!.releaseSurfaceViewRenderer()
        closeCapturer()
        super.onPause()
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart: ")
        updateCameraView(null)
        super.onRestart()
    }

    private fun closeCapturer() {
        if (videoCapturer != null) {
            try {
                videoCapturer!!.stopCapture()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            videoCapturer!!.dispose()
            videoCapturer = null
        }
        if (videoSource != null) {
            videoSource!!.dispose()
            videoSource = null
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory!!.stopAecDump()
            peerConnectionFactory!!.dispose()
            peerConnectionFactory = null
        }
        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()
    }
}