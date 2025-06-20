package com.app.interact.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.app.interact.R
import com.app.interact.modal.ListItem

class CameraDeviceListAdapter(
    context: Context,
    resource: Int,
    objects: ArrayList<ListItem?>,
    private val textColor: Int = Color.WHITE // Default to white text
) : ArrayAdapter<ListItem?>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val layoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(R.layout.camera_device_list_layout, parent, false)
        }

        val item = getItem(position)
        val tvDeviceName = view!!.findViewById<TextView>(R.id.tvCameraDeviceName)
        val ivSelected = view.findViewById<ImageView>(R.id.ivCameraDeviceSelected)

        if (item != null) {
            tvDeviceName.text = item.itemName
            tvDeviceName.setTextColor(textColor) // Set the text color here
            
            // If this item is the selected camera, show the check mark
            if (item.isSelected) {
                ivSelected.visibility = View.VISIBLE
            } else {
                ivSelected.visibility = View.INVISIBLE
            }
        }

        return view
    }
}
