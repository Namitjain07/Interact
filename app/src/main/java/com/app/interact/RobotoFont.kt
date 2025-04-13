package com.app.interact

import android.content.Context
import android.graphics.Typeface

class RobotoFont {
    private var fromAsset: Typeface? = null

    fun getTypeFace(context: Context): Typeface? {
        if (fromAsset == null) {
            fromAsset = Typeface.createFromAsset(context.assets, "fonts/Roboto-Bold.ttf")
        }
        return fromAsset
    }

}