package com.app.interact.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.interact.activity.CreateOrJoinActivity
import com.app.interact.R

class CreateOrJoinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_create_or_join, container, false)
        view.findViewById<View>(R.id.btnCreateMeeting)
            .setOnClickListener { (activity as CreateOrJoinActivity).createMeetingFragment() }
        view.findViewById<View>(R.id.btnJoinMeeting)
            .setOnClickListener { (activity as CreateOrJoinActivity).joinMeetingFragment() }
        // Inflate the layout for this fragment
        return view
    }
}