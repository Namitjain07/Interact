package com.app.interact.utils

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.app.interact.R

/**
 * Helper class to ensure the bottom bar stays visible
 */
class PersistentBottomBarHelper(private val activity: Activity) {
    
    private var bottomBar: View? = null
    private var pillLayout: LinearLayout? = null
    
    fun setupPersistentBottomBar() {
        bottomBar = activity.findViewById(R.id.bottomBarContainer)
        pillLayout = activity.findViewById(R.id.control_panel_layout)
        
        // Ensure visibility is maintained
        bottomBar?.visibility = View.VISIBLE
        pillLayout?.visibility = View.VISIBLE
    }
    
    fun refreshBottomBarVisibility() {
        bottomBar?.visibility = View.VISIBLE
        pillLayout?.visibility = View.VISIBLE
        
        // Reset any potentially overriding properties
        bottomBar?.alpha = 1.0f
        pillLayout?.alpha = 1.0f
    }
}
