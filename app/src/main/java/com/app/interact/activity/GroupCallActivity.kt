package com.app.interact.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.*
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.interact.RobotoFont
import com.app.interact.adapter.AudioDeviceListAdapter
import com.app.interact.adapter.LeaveOptionListAdapter
import com.app.interact.adapter.MessageAdapter
import com.app.interact.adapter.MoreOptionsListAdapter
import com.app.interact.adapter.ParticipantListAdapter
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import live.videosdk.rtc.android.VideoView
import com.app.interact.listener.ResponseListener
import com.app.interact.modal.ListItem
import com.app.interact.utils.NetworkUtils
import com.app.interact.adapter.ParticipantViewAdapter
import com.app.interact.utils.HelperClass
import com.app.interact.utils.ParticipantState
import com.app.interact.utils.PersistentBottomBarHelper
import com.app.interact.R
import com.app.interact.adapter.CameraDeviceListAdapter
import live.videosdk.rtc.android.CustomStreamTrack
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Stream
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.lib.AppRTCAudioManager
import live.videosdk.rtc.android.lib.AppRTCAudioManager.AudioDevice
import live.videosdk.rtc.android.lib.JsonUtils
import live.videosdk.rtc.android.listeners.*
import live.videosdk.rtc.android.model.PubSubPublishOptions
import live.videosdk.rtc.android.permission.Permission
import live.videosdk.rtc.android.permission.PermissionHandler
import org.json.JSONObject
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack
import java.util.*
import kotlin.math.roundToInt
import live.videosdk.rtc.android.lib.transcription.TranscriptionConfig
import live.videosdk.rtc.android.lib.transcription.SummaryConfig
import live.videosdk.rtc.android.lib.transcription.TranscriptionState
import live.videosdk.rtc.android.lib.transcription.TranscriptionText
import live.videosdk.rtc.android.lib.transcription.PostTranscriptionConfig
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView


class GroupCallActivity : AppCompatActivity() {
    private var meeting: Meeting? = null
    private var btnWebcam: FloatingActionButton? = null
    private var btnMic: ImageButton? = null
    private var btnAudioSelection: ImageButton? = null
    private var btnSwitchCameraMode: ImageButton? = null
    private var btnLeave: FloatingActionButton? = null
    private var btnChat: FloatingActionButton? = null
    private var btnMore: FloatingActionButton? = null

    private var micLayout: LinearLayout? = null
    private var participants: ArrayList<Participant>? = null
    private var shareView: VideoView? = null
    private var shareLayout: FrameLayout? = null

    private var micEnabled = true
    private var webcamEnabled = true
    private var recording = false
    private var localScreenShare = false
    private var token: String? = null
    private var recordingStatusSnackbar: Snackbar? = null


    private val CAPTURE_PERMISSION_REQUEST_CODE = 1

    private var screenshareEnabled = false
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var selectedAudioDeviceName: String? = null

    private var etmessage: EditText? = null
    private var messageAdapter: MessageAdapter? = null
    private var pubSubMessageListener: PubSubMessageListener? = null
    private var viewPager2: ViewPager2? = null
    private var viewAdapter: ParticipantViewAdapter? = null
    private var meetingSeconds = 0
    private var txtMeetingTime: TextView? = null
    private var btnStopScreenShare: Button? = null

    var clickCount = 0
    var startTime: Long = 0
    val MAX_DURATION = 500
    var fullScreen = false
    var onTouchListener: OnTouchListener? = null
    private var screenShareParticipantNameSnackbar: Snackbar? = null
    private var runnable: Runnable? = null
    val handler = Handler(Looper.getMainLooper())
    private var chatListener: PubSubMessageListener? = null
    private var raiseHandListener: PubSubMessageListener? = null

    private lateinit var bottomBarHelper: PersistentBottomBarHelper

    private var transcriptionEnabled = false
    private var btnTranscription: FloatingActionButton? = null
    private var transcriptionContainer: LinearLayout? = null
    private var transcriptionTextView: TextView? = null
    private var transcriptionScrollView: NestedScrollView? = null
    private var transcriptionStatusSnackbar: Snackbar? = null
    private var postTranscriptionEnabled = false  // Flag to track post-transcription status
    private var postTranscriptionData: JSONObject? = null  // Store fetched post-transcription data

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_call)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val toolbar = findViewById<Toolbar>(R.id.material_toolbar)
        toolbar.title = ""
        toolbar.setBackgroundColor(Color.TRANSPARENT) // Set toolbar background transparent
        setSupportActionBar(toolbar)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set ActionBar background transparent

        //
        btnLeave = findViewById(R.id.btnLeave)
        btnChat = findViewById(R.id.btnChat)
        btnMore = findViewById(R.id.btnMore)
        btnSwitchCameraMode = findViewById(R.id.btnSwitchCameraMode)
        micLayout = findViewById(R.id.micLayout)
        btnMic = findViewById(R.id.btnMic)
        btnWebcam = findViewById(R.id.btnWebcam)
        btnAudioSelection = findViewById(R.id.btnAudioSelection)
        txtMeetingTime = findViewById(R.id.txtMeetingTime)
        btnStopScreenShare = findViewById(R.id.btnStopScreenShare)
        viewPager2 = findViewById(R.id.view_pager_video_grid)
        shareLayout = findViewById(R.id.shareLayout)
        shareView = findViewById(R.id.shareView)
        token = intent.getStringExtra("token")
        val meetingId = intent.getStringExtra("meetingId")
        micEnabled = intent.getBooleanExtra("micEnabled", true)
        webcamEnabled = intent.getBooleanExtra("webcamEnabled", true)
        var localParticipantName = intent.getStringExtra("participantName")
        if (localParticipantName == null) {
            localParticipantName = "John Doe"
        }

        // Set scale type for buttons
        btnLeave?.scaleType = ImageView.ScaleType.FIT_CENTER
        btnChat?.scaleType = ImageView.ScaleType.FIT_CENTER
        btnMore?.scaleType = ImageView.ScaleType.FIT_CENTER
        btnWebcam?.scaleType = ImageView.ScaleType.FIT_CENTER

        // pass the token generated from api server
        VideoSDK.config(token)

        val customTracks: MutableMap<String, CustomStreamTrack> = HashMap()

        val videoCustomTrack = VideoSDK.createCameraVideoTrack(
            "h720p_w960p",
            "front",
            CustomStreamTrack.VideoMode.TEXT,
            true,
            this,null,VideoSDK.getSelectedVideoDevice()
        )
        customTracks["video"] = videoCustomTrack

        val audioCustomTrack = VideoSDK.createAudioTrack("high_quality", this)
        customTracks["mic"] = audioCustomTrack

        // create a new meeting instance
        meeting = VideoSDK.initMeeting(
            this@GroupCallActivity, meetingId, localParticipantName,
            micEnabled, webcamEnabled, null, null, true,customTracks,null
        )

        //
        val textMeetingId = findViewById<TextView>(R.id.txtMeetingId)
        textMeetingId.text = meetingId
        meeting!!.addEventListener(meetingEventListener)

        //show Progress
        HelperClass.showProgress(window.decorView.rootView)

        //
        checkPermissions()

        // Actions
        setActionListeners()
        setAudioDeviceListeners()
        (findViewById<View>(R.id.btnCopyContent) as ImageButton).setOnClickListener {
            copyTextToClipboard(
                meetingId
            )
        }
        btnAudioSelection!!.setOnClickListener { showAudioInputDialog() }
        btnStopScreenShare!!.setOnClickListener {
            if (localScreenShare) {
                meeting!!.disableScreenShare()
            }
        }
        recordingStatusSnackbar = Snackbar.make(
            findViewById(R.id.mainLayout), "Recording will be started in few moments",
            Snackbar.LENGTH_INDEFINITE
        )
        styleSnackbar(recordingStatusSnackbar!!)
        recordingStatusSnackbar!!.isGestureInsetBottomIgnored = true
        viewAdapter = ParticipantViewAdapter(this@GroupCallActivity, meeting!!)
        onTouchListener = object : OnTouchListener {
            @SuppressLint("ClickableViewAccessibility", "CutPasteId")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> {
                        clickCount++
                        if (clickCount == 1) {
                            startTime = System.currentTimeMillis()
                        } else if (clickCount == 2) {
                            val duration = System.currentTimeMillis() - startTime
                            if (duration <= MAX_DURATION) {
                                if (fullScreen) {
                                    toolbar.visibility = VISIBLE
                                    run {
                                        var i = 0
                                        while (i < toolbar.childCount) {
                                            toolbar.getChildAt(i).visibility = VISIBLE
                                            i++
                                        }
                                    }
                                    val params = Toolbar.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    params.setMargins(22, 10, 0, 0)
                                    findViewById<View>(R.id.meetingLayout).layoutParams =
                                        params
                                    shareLayout!!.layoutParams = LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        HelperClass().dpToPx(420, this@GroupCallActivity)
                                    )
                                    (findViewById<View>(R.id.localScreenShareView) as LinearLayout).layoutParams =
                                        LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            HelperClass().dpToPx(420, this@GroupCallActivity)
                                        )
                                    val toolbarAnimation = TranslateAnimation(
                                        0F,
                                        0F,
                                        0F,
                                        10F
                                    )
                                    toolbarAnimation.duration = 500
                                    toolbarAnimation.fillAfter = true
                                    toolbar.startAnimation(toolbarAnimation)
                                    val bottomBarContainer = findViewById<View>(R.id.bottomBarContainer)
                                    bottomBarContainer.visibility = VISIBLE
                                    val controlPanelLayout = findViewById<LinearLayout>(R.id.control_panel_layout)
                                    controlPanelLayout.visibility = VISIBLE
                                    val animate = TranslateAnimation(
                                        0F,
                                        0F,
                                        findViewById<View>(R.id.bottomBarContainer).height
                                            .toFloat(),
                                        0F
                                    )
                                    animate.duration = 300
                                    animate.fillAfter = true
                                    findViewById<View>(R.id.bottomBarContainer).startAnimation(animate)
                                } else {
                                    toolbar.visibility = GONE
                                    run {
                                        var i = 0
                                        while (i < toolbar.childCount) {
                                            toolbar.getChildAt(i).visibility = GONE
                                            i++
                                        }
                                    }
                                    shareLayout!!.layoutParams = LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        HelperClass().dpToPx(500, this@GroupCallActivity)
                                    )
                                    (findViewById<View>(R.id.localScreenShareView) as LinearLayout).layoutParams =
                                        LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            HelperClass().dpToPx(500, this@GroupCallActivity)
                                        )
                                    val toolbarAnimation = TranslateAnimation(
                                        0F,
                                        0F,
                                        0F,
                                        10F
                                    )
                                    toolbarAnimation.duration = 500
                                    toolbarAnimation.fillAfter = true
                                    toolbar.startAnimation(toolbarAnimation)
                                    val bottomBarContainer = findViewById<View>(R.id.bottomBarContainer)
                                    bottomBarContainer.visibility = GONE
                                    val controlPanelLayout = findViewById<LinearLayout>(R.id.control_panel_layout)
                                    controlPanelLayout.visibility = GONE
                                    val animate = TranslateAnimation(
                                        0F,
                                        0F,
                                        0F,
                                        findViewById<View>(R.id.bottomBarContainer).height
                                            .toFloat()
                                    )
                                    animate.duration = 400
                                    animate.fillAfter = true
                                    findViewById<View>(R.id.bottomBarContainer).startAnimation(animate)
                                }
                                fullScreen = !fullScreen
                                clickCount = 0
                            } else {
                                clickCount = 1
                                startTime = System.currentTimeMillis()
                            }

                        }
                    }
                }
                return true
            }
        }
        findViewById<View>(R.id.participants_Layout).setOnTouchListener(onTouchListener)

        findViewById<View>(R.id.ivParticipantScreenShareNetwork).setOnClickListener {
            val participantList = getAllParticipants()
            val participant = participantList[0]
            val popupwindow_obj: PopupWindow? = HelperClass().callStatsPopupDisplay(
                participant,
                findViewById(R.id.ivParticipantScreenShareNetwork),
                this@GroupCallActivity,
                true
            )
            popupwindow_obj!!.showAsDropDown(
                findViewById(R.id.ivParticipantScreenShareNetwork),
                -350,
                -85
            )
        }

        findViewById<View>(R.id.ivLocalScreenShareNetwork).setOnClickListener {
            val popupwindow_obj: PopupWindow? = HelperClass().callStatsPopupDisplay(
                meeting!!.getLocalParticipant(),
                findViewById(R.id.ivLocalScreenShareNetwork),
                this@GroupCallActivity,
                true
            )
            popupwindow_obj!!.showAsDropDown(
                findViewById(R.id.ivLocalScreenShareNetwork),
                -350,
                -85
            )
        }

        bottomBarHelper = PersistentBottomBarHelper(this)
        bottomBarHelper.setupPersistentBottomBar()

        // Initialize transcription views
        transcriptionContainer = findViewById(R.id.transcriptionContainer)
        transcriptionTextView = findViewById(R.id.transcriptionTextView)
        transcriptionScrollView = findViewById(R.id.transcriptionScrollView)

        // Setup transcription status snackbar
        transcriptionStatusSnackbar = Snackbar.make(
            findViewById(R.id.mainLayout), "Transcription will be started in few moments",
            Snackbar.LENGTH_INDEFINITE
        )
        styleSnackbar(transcriptionStatusSnackbar!!)
        transcriptionStatusSnackbar!!.isGestureInsetBottomIgnored = true
    }

    override fun onResume() {
        super.onResume()
        bottomBarHelper.refreshBottomBarVisibility()
    }

    private fun updateUI() {
        bottomBarHelper.refreshBottomBarVisibility()
    }

    fun getTouchListener(): OnTouchListener? {
        return onTouchListener
    }

    private fun toggleMicIcon() {
        if (micEnabled) {
            btnMic!!.setImageResource(R.drawable.ic_mic_on)
            btnMic!!.setColorFilter(ContextCompat.getColor(this, R.color.white)) // Set white tint
            btnAudioSelection!!.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
            btnAudioSelection!!.setColorFilter(ContextCompat.getColor(this, R.color.white)) // Set white tint
        } else {
            btnMic!!.setImageResource(R.drawable.ic_mic_off_24)
            btnMic!!.setColorFilter(ContextCompat.getColor(this, R.color.white)) // Keep white tint
            btnAudioSelection!!.setImageResource(R.drawable.ic_baseline_arrow_drop_down)
            btnAudioSelection!!.setColorFilter(ContextCompat.getColor(this, R.color.white)) // Keep white tint
        }
        // Center the icons after changing them
        btnMic!!.scaleType = ImageView.ScaleType.FIT_CENTER
        btnAudioSelection!!.scaleType = ImageView.ScaleType.FIT_CENTER
    }
    @SuppressLint("ResourceType")
    private fun toggleWebcamIcon() {
        if (webcamEnabled) {
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera)
            btnWebcam!!.setColorFilter(Color.WHITE)
            var buttonDrawable = btnWebcam!!.background
            buttonDrawable = DrawableCompat.wrap(buttonDrawable!!)
            DrawableCompat.setTint(buttonDrawable, Color.TRANSPARENT)
            btnWebcam!!.background = buttonDrawable
        } else {
            btnWebcam!!.setImageResource(R.drawable.ic_video_camera_off)
            btnWebcam!!.setColorFilter(Color.BLACK)
            var buttonDrawable = btnWebcam!!.background
            buttonDrawable = DrawableCompat.wrap(buttonDrawable!!)
            DrawableCompat.setTint(buttonDrawable, Color.WHITE)
            btnWebcam!!.background = buttonDrawable
        }
        // Ensure the icon is properly centered
        btnWebcam!!.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private fun styleSnackbar(snackbar: Snackbar) {
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.BLACK)
        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(Color.WHITE)
        snackbar.isGestureInsetBottomIgnored = true
    }

    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        override fun onMeetingJoined() {
            if (meeting != null) {
                //hide progress when meetingJoined
                HelperClass.hideProgress(window.decorView.rootView)
                toggleMicIcon()
                toggleWebcamIcon()
                setLocalListeners()
                NetworkUtils(this@GroupCallActivity).fetchMeetingTime(
                    meeting!!.meetingId,
                    token,
                    object : ResponseListener<Int> {
                        override fun onResponse(meetingTime: Int?) {
                            meetingSeconds = meetingTime!!
                            showMeetingTime()
                        }
                    })
                viewPager2!!.offscreenPageLimit = 1
                viewPager2!!.adapter = viewAdapter
                raiseHandListener =
                    PubSubMessageListener { pubSubMessage ->
                        val parentLayout = findViewById<View>(android.R.id.content)
                        val snackbar: Snackbar
                        if ((pubSubMessage.senderId == meeting!!.localParticipant.id)) {
                            snackbar = Snackbar.make(
                                parentLayout,
                                "You raised hand",
                                Snackbar.LENGTH_SHORT
                            )
                        } else {
                            snackbar = Snackbar.make(
                                parentLayout,
                                pubSubMessage.senderName + " raised hand  ",
                                Snackbar.LENGTH_LONG
                            )
                        }

                        val snackbarLayout = snackbar.view
                        val snackbarTextId = com.google.android.material.R.id.snackbar_text
                        val textView = snackbarLayout.findViewById<View>(snackbarTextId) as TextView

                        val drawable = resources.getDrawable(R.drawable.ic_raise_hand)
                        drawable.setBounds(0, 0, 50, 65)
                        textView.setCompoundDrawablesRelative(drawable, null, null, null)
                        textView.compoundDrawablePadding = 15
                        styleSnackbar(snackbar)
                        snackbar.view.setOnClickListener { snackbar.dismiss() }
                        snackbar.show()
                    }

                // notify user for raise hand
                meeting!!.pubSub.subscribe("RAISE_HAND", raiseHandListener)
                chatListener = PubSubMessageListener { pubSubMessage ->
                    if (pubSubMessage.senderId != meeting!!.localParticipant.id) {
                        val parentLayout = findViewById<View>(android.R.id.content)
                        val snackbar = Snackbar.make(
                            parentLayout, (pubSubMessage.senderName + " says: " +
                                    pubSubMessage.message), Snackbar.LENGTH_SHORT
                        )
                            .setDuration(2000)
                        styleSnackbar(snackbar)
                        snackbar.view.setOnClickListener { snackbar.dismiss() }
                        snackbar.show()
                    }
                }
                // notify user of any new messages
                meeting!!.pubSub.subscribe("CHAT", chatListener)

                //terminate meeting in 10 minutes
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isDestroyed) {
                        val alertDialog = MaterialAlertDialogBuilder(
                            this@GroupCallActivity,
                            R.style.AlertDialogCustom
                        ).create()
                        alertDialog.setCanceledOnTouchOutside(false)
                        val inflater = this@GroupCallActivity.layoutInflater
                        val dialogView = inflater.inflate(R.layout.alert_dialog_layout, null)
                        alertDialog.setView(dialogView)
                        val title = dialogView.findViewById<View>(R.id.title) as TextView
                        title.text = "Meeting Left"
                        val message = dialogView.findViewById<View>(R.id.message) as TextView
                        message.text = "Demo app limits meeting to 10 Minutes"
                        val positiveButton = dialogView.findViewById<Button>(R.id.positiveBtn)
                        positiveButton.text = "Ok"
                        positiveButton.setOnClickListener {
                            if (!isDestroyed) {
                                ParticipantState.destroy()
                                unSubscribeTopics()
                                meeting!!.leave()
                            }
                            alertDialog.dismiss()
                        }
                        val negativeButton = dialogView.findViewById<Button>(R.id.negativeBtn)
                        negativeButton.visibility = GONE
                        alertDialog.show()
                    }
                }, 600000)
            }
        }

        override fun onMeetingLeft() {
            handler.removeCallbacks(runnable!!)
            if (!isDestroyed) {
                val intents = Intent(this@GroupCallActivity, CreateOrJoinActivity::class.java)
                intents.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
                startActivity(intents)
                finish()
            }
        }

        override fun onPresenterChanged(participantId: String?) {
            updatePresenter(participantId)
        }

        override fun onRecordingStarted() {
            recording = true
            recordingStatusSnackbar!!.dismiss()
            (findViewById<View>(R.id.recordingLottie)).visibility = VISIBLE
            Toast.makeText(
                this@GroupCallActivity, "Recording started",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onRecordingStopped() {
            recording = false
            (findViewById<View>(R.id.recordingLottie)).visibility = GONE
            Toast.makeText(
                this@GroupCallActivity, "Recording stopped",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onExternalCallStarted() {
            Toast.makeText(this@GroupCallActivity, "onExternalCallStarted", Toast.LENGTH_SHORT)
                .show()
        }

        override fun onError(error: JSONObject) {
            try {
                val errorCodes = VideoSDK.getErrorCodes()
                val code = error.getInt("code")
                if (code == errorCodes.getInt("PREV_RECORDING_PROCESSING")) {
                    recordingStatusSnackbar!!.dismiss()
                }
                val snackbar = Snackbar.make(
                    findViewById(R.id.mainLayout), error.getString("message"),
                    Snackbar.LENGTH_LONG
                )
                styleSnackbar(snackbar)
                snackbar.view.setOnClickListener { snackbar.dismiss() }
                snackbar.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onSpeakerChanged(participantId: String?) {}
        override fun onMeetingStateChanged(state: String) {
            if (state === "FAILED") {
                val parentLayout = findViewById<View>(android.R.id.content)
                val builderTextLeft = SpannableStringBuilder()
                builderTextLeft.append("   Call disconnected. Reconnecting...")
                builderTextLeft.setSpan(
                    ImageSpan(
                        this@GroupCallActivity,
                        R.drawable.ic_call_disconnected
                    ), 0, 1, 0
                )
                val snackbar = Snackbar.make(parentLayout, builderTextLeft, Snackbar.LENGTH_LONG)
                styleSnackbar(snackbar)
                snackbar.view.setOnClickListener { snackbar.dismiss() }
                snackbar.show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (handler.hasCallbacks((runnable)!!)) handler.removeCallbacks((runnable)!!)
                }
            }
        }

        override fun onMicRequested(participantId: String, listener: MicRequestListener) {
            showMicRequestDialog(listener)
        }

        override fun onWebcamRequested(participantId: String, listener: WebcamRequestListener) {
            showWebcamRequestDialog(listener)
        }

        override fun onTranscriptionStateChanged(data: JSONObject) {
            try {
                val status = data.getString("status")
                val id = data.getString("id")

                runOnUiThread {
                    when (status) {
                        TranscriptionState.TRANSCRIPTION_STARTING.name -> {
                            Log.d("Transcription", "Realtime Transcription is starting, ID: $id")
                            transcriptionStatusSnackbar?.show()
                        }
                        TranscriptionState.TRANSCRIPTION_STARTED.name -> {
                            Log.d("Transcription", "Realtime Transcription is started, ID: $id")
                            transcriptionStatusSnackbar?.dismiss()
                            transcriptionEnabled = true
                            transcriptionContainer?.visibility = VISIBLE

                            Toast.makeText(
                                this@GroupCallActivity, "Transcription started",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TranscriptionState.TRANSCRIPTION_STOPPING.name -> {
                            Log.d("Transcription", "Realtime Transcription is stopping, ID: $id")
                        }
                        TranscriptionState.TRANSCRIPTION_STOPPED.name -> {
                            Log.d("Transcription", "Realtime Transcription is stopped, ID: $id")
                            transcriptionEnabled = false
                            transcriptionContainer?.visibility = GONE

                            Toast.makeText(
                                this@GroupCallActivity, "Transcription stopped",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> Log.d("Transcription", "Unknown transcription state: $status, ID: $id")
                    }
                }
            } catch (e: Exception) {
                Log.e("Transcription", "Error parsing transcription state", e)
            }
        }

        override fun onTranscriptionText(data: TranscriptionText) {
            val participantId: String = data.participantId
            val participantName: String = data.participantName
            val text: String = data.text
            val timestamp: Int = data.timestamp
            val type: String = data.type

            runOnUiThread {
                // Append text to the transcription view
                val formattedText = "$participantName: $text\n"
                transcriptionTextView?.append(formattedText)

                // Auto-scroll to the bottom
                transcriptionScrollView?.post {
                    transcriptionScrollView?.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

            Log.d("Transcription", "$participantName: $text $timestamp")
        }
    }


    private fun setLocalListeners() {
        meeting!!.localParticipant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    webcamEnabled = true
                    toggleWebcamIcon()
                } else if (stream.kind.equals("audio", ignoreCase = true)) {
                    micEnabled = true
                    toggleMicIcon()
                } else if (stream.kind.equals("share", ignoreCase = true)) {
                    findViewById<View>(R.id.localScreenShareView).visibility = VISIBLE
                    screenShareParticipantNameSnackbar = Snackbar.make(
                        findViewById(R.id.mainLayout), "You started presenting",
                        Snackbar.LENGTH_SHORT
                    )
                    styleSnackbar(screenShareParticipantNameSnackbar!!)
                    screenShareParticipantNameSnackbar!!.view.setOnClickListener { screenShareParticipantNameSnackbar!!.dismiss() }
                    screenShareParticipantNameSnackbar!!.show()
                    localScreenShare = true
                    screenshareEnabled = true
                }
            }

            override fun onStreamDisabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    webcamEnabled = false
                    toggleWebcamIcon()
                } else if (stream.kind.equals("audio", ignoreCase = true)) {
                    micEnabled = false
                    toggleMicIcon()
                } else if (stream.kind.equals("share", ignoreCase = true)) {
                    findViewById<View>(R.id.localScreenShareView).visibility = GONE
                    localScreenShare = false
                    screenshareEnabled = false
                }
            }
        })
    }

    private fun askPermissionForScreenShare() {
        val mediaProjectionManager = application.getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE
        )
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) return
        if (resultCode != RESULT_OK) {
            Toast.makeText(
                this@GroupCallActivity,
                "You didn't give permission to capture the screen.",
                Toast.LENGTH_SHORT
            ).show()
            localScreenShare = false
            return
        }
        meeting!!.enableScreenShare(data)
    }

    private val permissionHandler: com.nabinbhandari.android.permissions.PermissionHandler = object : com.nabinbhandari.android.permissions.PermissionHandler() {
        override fun onGranted() {
        }

        override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
            super.onDenied(context, deniedPermissions)
            Toast.makeText(
                this@GroupCallActivity,
                "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT
            ).show()
        }

        override fun onBlocked(context: Context, blockedList: ArrayList<String>): Boolean {
            Toast.makeText(
                this@GroupCallActivity,
                "Permission(s) not granted. Some feature may not work", Toast.LENGTH_SHORT
            ).show()
            return super.onBlocked(context, blockedList)
        }


    }


    private val permissionHandlerSDK: live.videosdk.rtc.android.permission.PermissionHandler = object :
        live.videosdk.rtc.android.permission.PermissionHandler() {
        override fun onGranted() {
            if (meeting != null) meeting!!.join()
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
        val optionsSDK = live.videosdk.rtc.android.permission.Permissions.Options().setRationaleDialogTitle("Info").setSettingsDialogTitle("Warning")
        VideoSDK.checkPermissions(this,
            permissionListSDK,
            optionsSDK,
            permissionHandlerSDK
        )
    }

    private fun setAudioDeviceListeners() {
        meeting!!.setAudioDeviceChangeListener { selectedAudioDevice, availableAudioDevices ->
            selectedAudioDeviceName = selectedAudioDevice.toString()
        }
    }

    private fun copyTextToClipboard(text: String?) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@GroupCallActivity, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    private fun toggleMic() {
        if (micEnabled) {
            meeting!!.muteMic()
        } else {
            val audioCustomTrack = VideoSDK.createAudioTrack("high_quality", this)
            meeting!!.unmuteMic(audioCustomTrack)
        }
    }

    private fun toggleWebCam() {
        if (webcamEnabled) {
            meeting!!.disableWebcam()
        } else {
            val videoCustomTrack = VideoSDK.createCameraVideoTrack(
                "h720p_w960p",
                "front",
                CustomStreamTrack.VideoMode.DETAIL,
                true,
                this,VideoSDK.getSelectedVideoDevice()
            )
            meeting!!.enableWebcam(videoCustomTrack)
        }
    }

    private fun setActionListeners() {
        // Toggle mic
        micLayout!!.setOnClickListener { toggleMic() }
        btnMic!!.setOnClickListener { toggleMic() }

        // Toggle webcam
        btnWebcam!!.setOnClickListener { toggleWebCam() }

        // Leave meeting
        btnLeave!!.setOnClickListener { showLeaveOrEndDialog() }
        btnMore!!.setOnClickListener { showMoreOptionsDialog() }
        btnSwitchCameraMode!!.setOnClickListener { showCameraSelectionBottomSheet() }

        // Chat
        btnChat!!.setOnClickListener {
            if (meeting != null) {
                openChat()
            }
        }
    }

    private fun showCameraSelectionBottomSheet() {
        try {
            // Get all camera devices directly from VideoSDK
            val cameraDeviceList = ArrayList<ListItem?>()
            
            // Create options for Front and Back camera
            cameraDeviceList.add(ListItem("Front Camera", null, false))
            cameraDeviceList.add(ListItem("Back Camera", null, false))
            
            // Inflate the bottom sheet layout
            val view = layoutInflater.inflate(R.layout.camera_device_bottomsheet, null)
            val listView = view.findViewById<ListView>(R.id.list_view_camera_devices)
            
            // Set the adapter with white text color
            val adapter = CameraDeviceListAdapter(
                this,
                R.layout.camera_device_list_layout,
                cameraDeviceList,
                Color.WHITE // Set white text color
            )
            listView.adapter = adapter
            
            // Create and show bottom sheet
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            bottomSheetDialog.setContentView(view)
            
            // Handle item clicks
            listView.setOnItemClickListener { _, _, position, _ ->
                try {
                    // Simple approach: position 0 is front, position 1 is back
                    val cameraMode = if (position == 0) "front" else "back"
                    
                    if (webcamEnabled) {
                        meeting!!.disableWebcam()
                        
                        // Short delay to ensure camera is properly disabled before re-enabling
                        Handler(Looper.getMainLooper()).postDelayed({
                            val videoTrack = VideoSDK.createCameraVideoTrack(
                                "h720p_w960p",
                                cameraMode,
                                CustomStreamTrack.VideoMode.DETAIL,
                                true,
                                this@GroupCallActivity,
                                null,
                                null
                            )
                            meeting!!.enableWebcam(videoTrack)
                        }, 500)
                    }
                    
                    Toast.makeText(this, "Switching to ${cameraDeviceList[position]?.itemName}",
                        Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("CameraSwitch", "Error: ${e.message}")
                    Toast.makeText(this, "Failed to switch camera", Toast.LENGTH_SHORT).show()
                }
                
                bottomSheetDialog.dismiss()
            }
            
            bottomSheetDialog.setOnShowListener {
                val bottomSheet = bottomSheetDialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.setBackgroundResource(android.R.color.transparent)
            }
            
            bottomSheetDialog.show()
        } catch (e: Exception) {
            Log.e("CameraSwitch", "Error: ${e.message}")
            Toast.makeText(this, "Failed to show camera options", Toast.LENGTH_SHORT).show()
        }
    }

    // Add the missing toggleScreenSharing method
    private fun toggleScreenSharing() {
        if (!screenshareEnabled) {
            if (!localScreenShare) {
                askPermissionForScreenShare()
            }
            localScreenShare = !localScreenShare
        } else {
            if (localScreenShare) {
                meeting!!.disableScreenShare()
            } else {
                Toast.makeText(this, "You can't share your screen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startTranscription() {
        if (!transcriptionEnabled) {
            transcriptionStatusSnackbar?.show()

            // Create transcription configuration
            val summaryConfig = SummaryConfig(
                true,
                "Write summary in sections like Title, Agenda, Speakers, Action Items, Outlines, Notes and Summary"
            )

            val transcriptionConfig = TranscriptionConfig(
                null, // No webhook URL for this example
                summaryConfig
            )

            meeting?.startTranscription(transcriptionConfig)
        }
    }

    private fun stopTranscription() {
        if (transcriptionEnabled) {
            meeting?.stopTranscription()
        }
    }

    private fun toggleTranscription() {
        if (transcriptionEnabled) {
            stopTranscription()
        } else {
            startTranscription()
        }
    }

    @SuppressLint("InflateParams")
    private fun showLeaveOrEndDialog() {
        val optionsArrayList: ArrayList<ListItem> = ArrayList()
        val leaveMeeting = AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_leave)?.let {
            ListItem("Leave", "Only you will leave the call", it)
        }
        val endMeeting = AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_end_meeting)?.let {
            ListItem("End", "End call for all the participants", it)
        }
        optionsArrayList.add(leaveMeeting!!)
        optionsArrayList.add(endMeeting!!)

        // Inflate the bottom sheet layout
        val view = layoutInflater.inflate(R.layout.leave_options_bottomsheet, null)
        val listView = view.findViewById<ListView>(R.id.list_view_leave_options)

        // Set up the adapter using your existing leave option list layout
        val adapter = LeaveOptionListAdapter(
            this@GroupCallActivity,
            R.layout.leave_options_list_layout,
            optionsArrayList
        )
        listView.adapter = adapter

        // Optionally configure the ListView dividers (if needed)
        listView.divider = ContextCompat.getColor(this, R.color.md_grey_200).toDrawable()
        listView.setFooterDividersEnabled(false)
        listView.addFooterView(View(this@GroupCallActivity))
        listView.dividerHeight = 2

        // Create and display the BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this@GroupCallActivity, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)

        // Handle item clicks
        listView.setOnItemClickListener { _, _, which, _ ->
            when (which) {
                0 -> {
                    unSubscribeTopics()
                    meeting!!.leave()
                }
                1 -> {
                    unSubscribeTopics()
                    meeting!!.end()
                }
            }
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
        bottomSheetDialog.show()
    }


    @SuppressLint("InflateParams")
    private fun showAudioInputDialog() {
        val mics = meeting!!.mics
        val audioDeviceList: ArrayList<ListItem?> = ArrayList()

        // Prepare the list of audio devices
        for (item in mics) {
            var mic = item.toString()
            mic = mic.substring(0, 1).uppercase(Locale.getDefault()) +
                    mic.substring(1).lowercase(Locale.getDefault())
            mic = mic.replace("_", " ")
            val isSelected = (mic == selectedAudioDeviceName)
            audioDeviceList.add(ListItem(mic, null, isSelected))
        }

        // Inflate the bottom sheet layout
        val view = layoutInflater.inflate(R.layout.audio_device_bottomsheet, null)
        val listView = view.findViewById<ListView>(R.id.list_view_audio_devices)

        // Set the custom adapter
        val adapter = AudioDeviceListAdapter(
            this@GroupCallActivity,
            R.layout.audio_device_list_layout,
            audioDeviceList
        )
        listView.adapter = adapter

        // Create and show the BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this@GroupCallActivity, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)

        // Handle item clicks
        listView.setOnItemClickListener { _, _, which, _ ->
            var audioDevice: AppRTCAudioManager.AudioDevice? = null
            when (audioDeviceList[which]!!.itemName) {
                "Bluetooth" -> audioDevice = AppRTCAudioManager.AudioDevice.BLUETOOTH
                "Wired headset" -> audioDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                "Speaker phone" -> audioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
                "Earpiece" -> audioDevice = AppRTCAudioManager.AudioDevice.EARPIECE
            }
            meeting!!.changeMic(audioDevice, VideoSDK.createAudioTrack("high_quality", this))
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
        bottomSheetDialog.show()
    }

    @SuppressLint("InflateParams")
    private fun showMoreOptionsDialog() {
        val participantSize = meeting!!.participants.size + 1
        val moreOptionsArrayList: ArrayList<ListItem> = ArrayList<ListItem>()

        val raisedHand = ListItem(
            "Raise Hand",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.raise_hand)!!
        )
        val startScreenShare = ListItem(
            "Share screen",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_screen_share)!!
        )
        val stopScreenShare = ListItem(
            "Stop screen share",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_screen_share)!!
        )
        val startRecording = ListItem(
            "Start recording",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_recording)!!
        )
        val stopRecording = ListItem(
            "Stop recording",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_recording)!!
        )
        val startTranscription = ListItem(
            "Start transcription",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_transcription)!!
        )
        val stopTranscription = ListItem(
            "Stop transcription",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_transcription)!!
        )
        val participantList = ListItem(
            "Participants ($participantSize)",
            AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_people)!!
        )

        moreOptionsArrayList.add(raisedHand)
        
        if (localScreenShare) {
            moreOptionsArrayList.add(stopScreenShare)
        } else {
            moreOptionsArrayList.add(startScreenShare)
        }
        
        if (recording) {
            moreOptionsArrayList.add(stopRecording)
            if (postTranscriptionEnabled) {
                // Add an option to check the post-transcription status
                val checkTranscriptionStatus = ListItem(
                    "Check post-transcription status",
                    AppCompatResources.getDrawable(this@GroupCallActivity, R.drawable.ic_transcription)!!
                )
                moreOptionsArrayList.add(checkTranscriptionStatus)
            }
        } else {
            moreOptionsArrayList.add(startRecording)
        }
        
        if (transcriptionEnabled) {
            moreOptionsArrayList.add(stopTranscription)
        } else {
            moreOptionsArrayList.add(startTranscription)
        }
        
        moreOptionsArrayList.add(participantList)

        // Inflate the BottomSheet layout
        val view = layoutInflater.inflate(R.layout.more_options_bottomsheet, null)
        val listView = view.findViewById<ListView>(R.id.list_view_more_options)

        // Set the custom adapter
        val arrayAdapter = MoreOptionsListAdapter(
            this@GroupCallActivity,
            R.layout.more_options_list_layout,
            moreOptionsArrayList
        )
        listView.adapter = arrayAdapter

        // Create and show the BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this@GroupCallActivity, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(view)

        // Handle item clicks
        listView.setOnItemClickListener { _, _, which, _ ->
            val selectedItem = moreOptionsArrayList[which].itemName
            when {
                selectedItem.contains("Raise Hand") -> { raisedHand() }
                selectedItem.contains("screen share") -> { toggleScreenSharing() }
                selectedItem.contains("recording") -> { toggleRecording() }
                selectedItem.contains("transcription") -> { toggleTranscription() }
                selectedItem.contains("Check post-transcription") -> { fetchPostTranscriptionData() }
                selectedItem.contains("Participants") -> { openParticipantList() }
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }

        bottomSheetDialog.show()
    }


    private fun raisedHand() {
        meeting!!.pubSub.publish("RAISE_HAND", "Raise Hand by Me", PubSubPublishOptions())
    }

    private fun toggleRecording() {
        if (!recording) {
            recordingStatusSnackbar!!.show()
            val config = JSONObject()
            val layout = JSONObject()
            JsonUtils.jsonPut(layout, "type", "SPOTLIGHT")
            JsonUtils.jsonPut(layout, "priority", "PIN")
            JsonUtils.jsonPut(layout, "gridSize", 12)
            JsonUtils.jsonPut(config, "layout", layout)
            JsonUtils.jsonPut(config, "orientation", "portrait")
            JsonUtils.jsonPut(config, "theme", "DARK")
            
            // Configure post-transcription and summary
//            val prompt = "Write summary in sections like Title, Agenda, Speakers, Action Items, Outlines, Notes and Summary"
            val prompt = "First analyse what different participants in meeting is saying print it exactly line by line with their name make sure in the same language as well and then summary in sections like Title, Agenda, Speakers, Action Items, Outlines, Notes and Summary"
            val summaryConfig = SummaryConfig(true, prompt)
            val modelId = "raman_v1"
            val postTranscriptionConfig = PostTranscriptionConfig(true, summaryConfig, modelId)
            
            // Start recording with post-transcription
            postTranscriptionEnabled = true
            meeting!!.startRecording(null, null, config, postTranscriptionConfig)
            
            Toast.makeText(
                this@GroupCallActivity,
                "Recording started with post-transcription enabled",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            meeting!!.stopRecording()
            
            if (postTranscriptionEnabled) {
                Toast.makeText(
                    this@GroupCallActivity,
                    "Post-transcription is being processed and will be available soon",
                    Toast.LENGTH_LONG
                ).show()
                
                // Schedule a fetch attempt after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isDestroyed) {
                        fetchPostTranscriptionData()
                    }
                }, 30000) // 30 seconds delay before first attempt
            }
        }
    }
    
    private fun fetchPostTranscriptionData() {
        // This method would typically call an API to fetch post-transcription data
        // For demo purposes, we'll show a toast message
        
        if (!isDestroyed && postTranscriptionEnabled) {
            Toast.makeText(
                this@GroupCallActivity,
                "Fetching post-transcription data...",
                Toast.LENGTH_SHORT
            ).show()
            
            // In a real implementation, you would make an API call here
            // For example, using a NetworkUtils method:
            // NetworkUtils(this).fetchPostTranscriptions(meeting!!.meetingId, token, object : ResponseListener<JSONObject> {
            //     override fun onResponse(data: JSONObject?) {
            //         if (data != null) {
            //             postTranscriptionData = data
            //             showPostTranscriptionDialog()
            //         }
            //     }
            // })
            
            // For now, let's simulate a fetching process with a delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isDestroyed) {
                    showPostTranscriptionDialog()
                }
            }, 3000)
        }
    }
    
    private fun showPostTranscriptionDialog() {
        if (!isDestroyed) {
            val alertDialog = MaterialAlertDialogBuilder(this@GroupCallActivity, R.style.AlertDialogCustom).create()
            alertDialog.setCanceledOnTouchOutside(false)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.alert_dialog_layout, null)
            alertDialog.setView(dialogView)
            
            val title = dialogView.findViewById<TextView>(R.id.title)
            title.text = "Post-Transcription Status"
            
            val message = dialogView.findViewById<TextView>(R.id.message)
            message.text = "Your meeting transcription is being processed. " +
                    "You can access the transcription and summary once processing is complete. " +
                    "This may take several minutes depending on the meeting length."
            
            val positiveButton = dialogView.findViewById<Button>(R.id.positiveBtn)
            positiveButton.text = "OK"
            positiveButton.setOnClickListener {
                alertDialog.dismiss()
            }
            
            val negativeButton = dialogView.findViewById<Button>(R.id.negativeBtn)
            negativeButton.text = "Check Again"
            negativeButton.setOnClickListener {
                fetchPostTranscriptionData()
                alertDialog.dismiss()
            }
            
            alertDialog.show()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        showLeaveOrEndDialog()
    }

    override fun onDestroy() {
        if (meeting != null) {
            meeting!!.removeAllListeners()
            meeting!!.localParticipant.removeAllListeners()
            meeting!!.leave()
            meeting = null
        }
        if (shareView != null) {
            shareView!!.visibility = GONE
            shareLayout!!.visibility = GONE
            shareView!!.releaseSurfaceViewRenderer()
        }
        postTranscriptionData = null
        super.onDestroy()
    }

    fun unSubscribeTopics() {
        if (meeting != null) {
            meeting!!.pubSub.unsubscribe("CHAT", chatListener)
            meeting!!.pubSub.unsubscribe("RAISE_HAND", raiseHandListener)
        }
    }

    private fun openParticipantList() {
        val participantsListView: RecyclerView
        val close: ImageView
        bottomSheetDialog = BottomSheetDialog(this)
        val v3 = LayoutInflater.from(applicationContext)
            .inflate(R.layout.layout_participants_list_view, findViewById(R.id.layout_participants))
        bottomSheetDialog!!.setContentView(v3)
        participantsListView = v3.findViewById(R.id.rvParticipantsLinearView)
        (v3.findViewById<View>(R.id.participant_heading) as TextView).typeface =
            RobotoFont().getTypeFace(
                this@GroupCallActivity
            )
        close = v3.findViewById(R.id.ic_close)
        participantsListView.minimumHeight = getWindowHeight()
        bottomSheetDialog!!.show()
        close.setOnClickListener { bottomSheetDialog!!.dismiss() }
        meeting!!.addEventListener(meetingEventListener)
        participants = getAllParticipants()
        participantsListView.layoutManager = LinearLayoutManager(applicationContext)
        participantsListView.adapter =
            ParticipantListAdapter(participants, meeting!!, this@GroupCallActivity)
        participantsListView.setHasFixedSize(true)
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (this@GroupCallActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun getWindowWidth(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (this@GroupCallActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getAllParticipants(): ArrayList<Participant> {
        val participantList: ArrayList<Participant> = ArrayList<Participant>()
        val participants: Iterator<Participant> = meeting!!.participants.values.iterator()
        for (i in 0 until meeting!!.participants.size) {
            val participant = participants.next()
            participantList.add(participant)
        }
        return participantList
    }


    @SuppressLint("ClickableViewAccessibility")
    fun openChat() {
        val messageRcv: RecyclerView
        val close: ImageView
        bottomSheetDialog = BottomSheetDialog(this)
        val v3 = LayoutInflater.from(applicationContext)
            .inflate(R.layout.activity_chat, findViewById(R.id.layout_chat))
        bottomSheetDialog!!.setContentView(v3)
        messageRcv = v3.findViewById(R.id.messageRcv)
        messageRcv.layoutManager = LinearLayoutManager(applicationContext)
        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            getWindowHeight() / 2
        )
        messageRcv.layoutParams = lp
        val mBottomSheetCallback: BottomSheetCallback = object : BottomSheetCallback() {
            override fun onStateChanged(
                bottomSheet: View,
                @BottomSheetBehavior.State newState: Int
            ) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        getWindowHeight() / 2
                    )
                    messageRcv.layoutParams = lp
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                    messageRcv.layoutParams = lp
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
        bottomSheetDialog!!.behavior.addBottomSheetCallback(mBottomSheetCallback)
        etmessage = v3.findViewById(R.id.etMessage)
        etmessage!!.setOnTouchListener { view, event ->
            if (view.id == R.id.etMessage) {
                view.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
        val btnSend = v3.findViewById<ImageButton>(R.id.btnSend)
        btnSend.isEnabled = false
        etmessage!!.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etmessage!!.hint = ""
            }
        }
        etmessage!!.isVerticalScrollBarEnabled = true
        etmessage!!.isScrollbarFadingEnabled = false
        etmessage!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (etmessage!!.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                    btnSend.isEnabled = true
                    btnSend.isSelected = true
                } else {
                    btnSend.isEnabled = false
                    btnSend.isSelected = false
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        //
        pubSubMessageListener = PubSubMessageListener { message ->
            messageAdapter!!.addItem(message)
            messageRcv.scrollToPosition(messageAdapter!!.itemCount - 1)
        }

        // Subscribe for 'CHAT' topic
        val pubSubMessageList = meeting!!.pubSub.subscribe("CHAT", pubSubMessageListener)

        //
        messageAdapter = MessageAdapter(this, pubSubMessageList, meeting!!)
        messageRcv.adapter = messageAdapter
        
        // Fix the OnLayoutChangeListener signature to match the required parameters
        messageRcv.addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, 
                                         oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            messageRcv.scrollToPosition(messageAdapter!!.itemCount - 1)
        }
        
        v3.findViewById<View>(R.id.btnSend).setOnClickListener {
            val message: String = etmessage!!.text.toString()
            if (message != "") {
                val publishOptions = PubSubPublishOptions()
                publishOptions.isPersist = true
                meeting!!.pubSub.publish("CHAT", message, publishOptions)
                etmessage!!.setText("")
            } else {
                Toast.makeText(
                    this@GroupCallActivity, "Please Enter Message",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        close = v3.findViewById(R.id.ic_close)
        bottomSheetDialog!!.show()
        close.setOnClickListener { bottomSheetDialog!!.dismiss() }
        bottomSheetDialog!!.setOnDismissListener {
            meeting!!.pubSub.unsubscribe(
                "CHAT",
                pubSubMessageListener
            )
        }
    }

    fun showMeetingTime() {
        runnable = object : Runnable {
            override fun run() {
                val hours = meetingSeconds / 3600
                val minutes = (meetingSeconds % 3600) / 60
                val secs = meetingSeconds % 60

                // Format the seconds into minutes,seconds.
                val time = String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d", hours,
                    minutes, secs
                )
                txtMeetingTime!!.text = time
                meetingSeconds++
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable!!)
    }

    private fun showMicRequestDialog(listener: MicRequestListener) {
        val alertDialog =
            MaterialAlertDialogBuilder(this@GroupCallActivity, R.style.AlertDialogCustom).create()
        alertDialog.setCanceledOnTouchOutside(false)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.alert_dialog_layout, null)
        alertDialog.setView(dialogView)
        val title = dialogView.findViewById<View>(R.id.title) as TextView
        title.visibility = GONE
        val message = dialogView.findViewById<View>(R.id.message) as TextView
        message.text = "Host is asking you to unmute your mic, do you want to allow ?"
        val positiveButton = dialogView.findViewById<Button>(R.id.positiveBtn)
        positiveButton.text = "Yes"
        positiveButton.setOnClickListener {
            listener.accept()
            alertDialog.dismiss()
        }
        val negativeButton = dialogView.findViewById<Button>(R.id.negativeBtn)
        negativeButton.text = "No"
        negativeButton.setOnClickListener {
            listener.reject()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    private fun showWebcamRequestDialog(listener: WebcamRequestListener) {
        val alertDialog =
            MaterialAlertDialogBuilder(this@GroupCallActivity, R.style.AlertDialogCustom).create()
        alertDialog.setCanceledOnTouchOutside(false)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.alert_dialog_layout, null)
        alertDialog.setView(dialogView)
        val title = dialogView.findViewById<View>(R.id.title) as TextView
        title.visibility = GONE
        val message = dialogView.findViewById<View>(R.id.message) as TextView
        message.text = "Host is asking you to enable your webcam, do you want to allow ?"
        val positiveButton = dialogView.findViewById<Button>(R.id.positiveBtn)
        positiveButton.text = "Yes"
        positiveButton.setOnClickListener {
            listener.accept()
            alertDialog.dismiss()
        }
        val negativeButton = dialogView.findViewById<Button>(R.id.negativeBtn)
        negativeButton.text = "No"
        negativeButton.setOnClickListener {
            listener.reject()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun updatePresenter(participantId: String?) {
        if (participantId == null) {
            shareView!!.visibility = GONE
            shareLayout!!.visibility = GONE
            screenshareEnabled = false
            return
        } else {
            screenshareEnabled = true
        }

        // find participant
        val participant = meeting!!.participants[participantId] ?: return

        // find share stream in participant
        var shareStream: Stream? = null
        for (stream: Stream in participant.streams.values) {
            if ((stream.kind == "share")) {
                shareStream = stream
                break
            }
        }
        if (shareStream == null) return
        (findViewById<View>(R.id.tvScreenShareParticipantName) as TextView).text =
            participant.displayName + " is presenting"
        findViewById<View>(R.id.tvScreenShareParticipantName).visibility = VISIBLE
        findViewById<View>(R.id.ivParticipantScreenShareNetwork).visibility = VISIBLE

        // display share video
        shareLayout!!.visibility = VISIBLE
        shareView!!.visibility = VISIBLE
        shareView!!.setZOrderMediaOverlay(true)
        shareView!!.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        val videoTrack = shareStream.track as VideoTrack
        shareView!!.addTrack(videoTrack)
        screenShareParticipantNameSnackbar = Snackbar.make(
            findViewById(R.id.mainLayout), participant.displayName + " started presenting",
            Snackbar.LENGTH_SHORT
        )
        styleSnackbar(screenShareParticipantNameSnackbar!!)
        screenShareParticipantNameSnackbar!!.view.setOnClickListener { screenShareParticipantNameSnackbar!!.dismiss() }
        screenShareParticipantNameSnackbar!!.show()

        // listen for share stop event
        participant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamDisabled(stream: Stream) {
                if ((stream.kind == "share")) {
                    val track: VideoTrack = stream.track as VideoTrack
                    shareView!!.removeTrack()
                    shareView!!.visibility = GONE
                    shareLayout!!.visibility = GONE
                    findViewById<View>(R.id.tvScreenShareParticipantName).visibility = GONE
                    findViewById<View>(R.id.ivParticipantScreenShareNetwork).visibility = GONE
                    localScreenShare = false
                }
            }
        })
    }
}