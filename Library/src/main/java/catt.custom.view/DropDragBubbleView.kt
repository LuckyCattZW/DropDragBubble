package catt.custom.view

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.FloatRange


class DropDragBubbleView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private val _TAG : String by lazy { DropDragBubbleView::class.java.simpleName }

    internal var listener: OnDropDragBubbleListener? = null

    internal var bindView: View? = null

    internal var bindDrawable: Drawable? = null

    internal var bindWindowManager: WindowManager? = null

    internal var bindWindowParams: WindowManager.LayoutParams? = null

    //固定圆位置
    private val fixationPoint: PointF by lazy { PointF(-1F, -1F) }

    //拖拽圆位置
    private val dragPoint: PointF by lazy { PointF(-1F, -1F) }

    private val bezierPath: Path by lazy { Path() }

    private var dragRadius: Float = convertPixel(TypedValue.COMPLEX_UNIT_DIP, 16)

    private var maxFixationRadius: Float = convertPixel(TypedValue.COMPLEX_UNIT_DIP, 8)

    private var minFixationRadius: Float = convertPixel(TypedValue.COMPLEX_UNIT_DIP, 4)

    private val whetherOutRange : Boolean
        get() = fixationRadius < minFixationRadius

    private var springBackAnimator : ValueAnimator? = null

    private var whetherAttachWindow: Boolean = false

    /**
     * 拖动阻力
     */
    @FloatRange(from = 0.0, to = 1.0)
    var dragSize: Float = 0.5F
        set(value) {
            if (value < 0.0 || value > 1.0) {
                throw IllegalArgumentException("Must be within a reasonable range.")
            }
            field = value
        }
        get() = field + 1F

    private var fixationRadius: Float = maxFixationRadius
        set(value) {
            field = maxFixationRadius - value / (when {
                measuredWidth > measuredHeight -> measuredWidth
                else -> measuredHeight
            } / (dragRadius * dragSize))
        }


    private val paint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            isDither = true
            color = Color.RED
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
        }
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        if (dragPoint.x < 0 || dragPoint.y < 0) return
        canvas.drawCircle(dragPoint.x, dragPoint.y, dragRadius, paint)
        if (!whetherOutRange) {
            canvas.drawCircle(fixationPoint.x, fixationPoint.y, fixationRadius, paint)
            createBezierPath(bezierPath)
            canvas.drawPath(bezierPath, paint)
        }
        bindDrawable?.apply {
            val bitmap = (bindDrawable as BitmapDrawable).bitmap
            canvas.drawBitmap(bitmap, dragPoint.x - bitmap.width / 2F, dragPoint.y - bitmap.height / 2F, null)
        }
    }

   private fun startSpringBackAnimator(){
       val startX = dragPoint.x
       val startY = dragPoint.y
       val endX = fixationPoint.x
       val endY = fixationPoint.y
       val diffX = endX - startX
       val diffY = endY - startY
       springBackAnimator = ValueAnimator.ofFloat(1F).apply {
           addUpdateListener {
               dragPoint.x = startX + diffX * it.animatedFraction
               dragPoint.y = startY + diffY * it.animatedFraction
               invalidate()
           }
           duration = 350L
           interpolator =  OvershootInterpolator(3F)
           addListener(object: AnimatorListenerAdapter(){
               override fun onAnimationEnd(animation: Animator?) {
                   bindView?.apply bindView@{
                       alpha = 1F
                       if (visibility != View.VISIBLE) visibility = View.VISIBLE
                       listener?.onSpringBack(this@bindView)
                   }
                   if(whetherAttachWindow) {
                       postDelayed({
                           bindWindowManager?.removeView(this@DropDragBubbleView)
                           clearPoint()
                       }, 64L)
                   }
               }
           })
       }
       springBackAnimator?.start()
    }

    private fun clearPoint(){
        dragPoint.x = -1F
        dragPoint.y = -1F
        fixationPoint.x = -1F
        fixationPoint.y = -1F
        whetherAttachWindow = false
    }

    fun stopPoint(){
        if (!whetherOutRange) {
            startSpringBackAnimator()
        }else {
            bindView?.apply {
                if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
                listener?.onDismiss(this@apply)
            }
            if(whetherAttachWindow){
                postDelayed({
                    bindWindowManager?.removeView(this@DropDragBubbleView)
                    clearPoint()
                }, 64L)
            }
        }
    }

    fun initPoint(downX: Float, downY: Float) {
        if (dragPoint.x != -1F || dragPoint.y != -1F) return
        springBackAnimator?.end()
        bindWindowManager?.addView(this@DropDragBubbleView, bindWindowParams)
        whetherAttachWindow = true
        loadingBingViewAnimator()
        dragPoint.x = downX
        dragPoint.y = downY
        fixationPoint.x = downX
        fixationPoint.y = downY
        invalidate()
    }

    fun updatePoint(moveX : Float, moveY : Float){
        if(fixationPoint.x == -1F || fixationPoint.y == -1F) return
        bindView?.alpha = 0F
        dragPoint.x = moveX
        dragPoint.y = moveY
        fixationRadius = getDistance(dragPoint, fixationPoint).toFloat()
        invalidate()
    }

    private fun loadingBingViewAnimator() {
       ValueAnimator.ofFloat(1F).apply {
            duration = 250L
            interpolator = LinearInterpolator()
            addUpdateListener {
                bindView?.alpha = 1 - it.animatedFraction
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (bindView?.visibility != View.INVISIBLE) bindView?.visibility = View.INVISIBLE
                }
            })
        }.start()
    }

    private fun createBezierPath(path: Path){
        val dy = dragPoint.y - fixationPoint.y
        val dx = dragPoint.x - fixationPoint.x
        val tanA = dy / dx
        val arcTanA = Math.atan(tanA.toDouble())

        val p0x:Float = fixationPoint.x + fixationRadius * Math.sin(arcTanA).toFloat()
        val p0y:Float = fixationPoint.y - fixationRadius * Math.cos(arcTanA).toFloat()

        val p1x:Float = dragPoint.x + dragRadius * Math.sin(arcTanA).toFloat()
        val p1y:Float = dragPoint.y - dragRadius * Math.cos(arcTanA).toFloat()

        val p2x:Float = dragPoint.x - dragRadius * Math.sin(arcTanA).toFloat()
        val p2y:Float = dragPoint.y + dragRadius * Math.cos(arcTanA).toFloat()

        val p3x:Float = fixationPoint.x - fixationRadius * Math.sin(arcTanA).toFloat()
        val p3y:Float = fixationPoint.y + fixationRadius * Math.cos(arcTanA).toFloat()


        val bezierControlX = (dragPoint.x + fixationPoint.x) / 2
        val bezierControlY = (dragPoint.y + fixationPoint.y) / 2

        path.apply {
            if(!isEmpty) reset()
            moveTo(p0x, p0y)
            quadTo(bezierControlX, bezierControlY, p1x, p1y)
            lineTo(p2x, p2y)
            quadTo(bezierControlX, bezierControlY, p3x, p3y)
            close()
        }
    }

    private fun getDistance(p1: PointF, p2: PointF) = Math.sqrt(Math.pow(Math.abs(p1.x - p2.x).toDouble(), 2.0) + Math.pow(Math.abs(p1.y - p2.y).toDouble(), 2.0))

    internal fun convertPixel(unit: Int, value: Int) = TypedValue.applyDimension(unit, value.toFloat(), resources.displayMetrics)

    internal fun convertPixel(unit: Int, value: Float) = TypedValue.applyDimension(unit, value, resources.displayMetrics)


    companion object {
        fun attach(view : View?, listener: OnDropDragBubbleListener? = null){
            view ?: throw NullPointerException("View cannot be null.")
            view.setOnTouchListener(DropDragBubbleTouchAdapter(view, listener))
        }
    }
}