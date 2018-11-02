package catt.custom.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

internal class DropDragBubbleTouchAdapter(private val _view: View, private var listener: OnDropDragBubbleListener? = null) : View.OnTouchListener {
    private val _TAG:String by lazy { DropDragBubbleTouchAdapter::class.java.simpleName }
    private val statusBarHeight: Int by lazy {
        val id: Int = _view.context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (id > 0) {
            return@lazy _view.context.resources.getDimensionPixelSize(id)
        }
        return@lazy bubbleView.convertPixel(TypedValue.COMPLEX_UNIT_DIP, 25).toInt()
    }

    private val viewCenterX: Float by lazy {
        val l = IntArray(2)
        _view.getLocationInWindow(l)
        l[0].toFloat() + _view.measuredWidth / 2F
    }

    private val viewCenterY: Float by lazy {
        val l = IntArray(2)
        _view.getLocationInWindow(l)
        l[1] + _view.measuredHeight / 2F
    }

    private val windowManager: WindowManager by lazy {
        (_view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).apply { }
    }

    private val bubbleView: DropDragBubbleView by lazy {
        DropDragBubbleView(_view.context).apply {
            this@apply.listener = this@DropDragBubbleTouchAdapter.listener
            this@apply.bindView = _view
            this@apply.bindDrawable = cloneViewDrawable
            this@apply.bindWindowManager = windowManager
            this@apply.bindWindowParams = params
        }
    }

    private val cloneViewDrawable: Drawable by lazy {
        _view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val bp = Bitmap.createBitmap(_view.measuredWidth, _view.measuredHeight, Bitmap.Config.ARGB_8888)
        Canvas(bp).apply canvas@{
            _view.draw(this@canvas)
            save()
        }
        BitmapDrawable(_view.context.resources, bp)
    }

    private val params: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSPARENT
            flags = /*若无该参数，则需要在触摸Y轴时减去状态栏高度(statusBarHeight)*/WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        }
    }

    private var differenceX : Float = 0F
    private var differenceY : Float = 0F

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event ?: return false
        event.apply {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    differenceX = rawX - viewCenterX
                    differenceY = rawY - viewCenterY
                    bubbleView.initPoint(viewCenterX, viewCenterY)
                }
                MotionEvent.ACTION_MOVE -> {
                    bubbleView.updatePoint(rawX - differenceX, rawY - differenceY)
                }
                MotionEvent.ACTION_UP -> {
                    bubbleView.stopPoint()
                    differenceX = 0F
                    differenceY = 0F
                }
            }
        }
        return true
    }
}