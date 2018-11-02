package catt.custom.view

import android.view.View

interface OnDropDragBubbleListener {
    fun onDismiss(view: View)
    fun onSpringBack(view: View)
}