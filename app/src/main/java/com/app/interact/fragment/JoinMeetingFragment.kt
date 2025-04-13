package com.app.interact.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.app.interact.activity.CreateOrJoinActivity
import com.app.interact.listener.ResponseListener
import com.app.interact.utils.NetworkUtils
import com.app.interact.activity.GroupCallActivity
import com.app.interact.utils.HelperClass
import com.app.interact.R


class JoinMeetingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_join_meeting, container, false)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etMeetingId = view.findViewById<EditText>(R.id.etMeetingId)
        val btnJoin = view.findViewById<Button>(R.id.btnJoin)


        btnJoin.setOnClickListener { v: View? ->
            val meetingId = etMeetingId!!.text.toString().trim { it <= ' ' }
            val pattern = Regex("\\w{4}-\\w{4}-\\w{4}")
            if ("" == meetingId) {
                Toast.makeText(
                    context, "Please enter meeting ID",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!pattern.matches(meetingId)) {
                Toast.makeText(
                    context, "Please enter valid meeting ID",
                    Toast.LENGTH_SHORT
                ).show()
            } else if ("" == etName.text.toString()) {
                Toast.makeText(context, "Please Enter Name", Toast.LENGTH_SHORT).show()
            } else {
                val networkUtils = NetworkUtils(context)
                if (networkUtils.isNetworkAvailable()) {
                    networkUtils.getToken(object : ResponseListener<String> {
                        override fun onResponse(token: String?) {
                            networkUtils.joinMeeting(
                                token,
                                etMeetingId.text.toString().trim { it <= ' ' },
                                object : ResponseListener<String> {
                                    override fun onResponse(meetingId: String?) {
                                        var intent: Intent? = null

                                        intent =Intent(
                                            activity as CreateOrJoinActivity?,
                                            GroupCallActivity::class.java
                                        )
                                        intent.putExtra("token", token)
                                        intent.putExtra("meetingId", meetingId)
                                        intent.putExtra(
                                            "webcamEnabled",
                                            (activity as CreateOrJoinActivity?)!!.isWebcamEnabled
                                        )
                                        intent.putExtra(
                                            "micEnabled",
                                            (activity as CreateOrJoinActivity?)!!.isMicEnabled
                                        )
                                        intent.putExtra(
                                            "participantName",
                                            etName.text.toString().trim { it <= ' ' })
                                        startActivity(intent)
                                        (activity as CreateOrJoinActivity?)!!.finish()

                                    }

                                })
                        }

                    })
                } else {
                    val snackbar = Snackbar.make(
                        view.findViewById(R.id.joinMeetingLayout),
                        "No Internet Connection",
                        Snackbar.LENGTH_LONG
                    )
                    HelperClass.setSnackBarStyle(snackbar.view, 0)
                    snackbar.show()
                }
            }
        }

        return view
    }
}