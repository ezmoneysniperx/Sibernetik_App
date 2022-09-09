package com.example.sibernetik

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class ImzaView(context: Context): View(context) {

    private var strokeWidth = 10f
    private var strokeColor = Color.BLACK
    private var background = Color.WHITE

    private val paint = Paint()
    private val path = Path()

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        paint.isAntiAlias = true
        paint.color = strokeColor
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = strokeWidth
    }

    fun setStrokeWidth(strokeWidth : Float){
        paint.strokeWidth = strokeWidth
    }

    fun setStrokeColor(strokeColor : Int){
        paint.color = strokeColor
    }

    fun setBackground(background : Int){
        this.background = background
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        when(event.action){

            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x,event.y)
                lastTouchX = event.x
                lastTouchY = event.y

                return true
            }

            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {

                val historySize = event.historySize
                for(i in 0 until historySize){
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)

                    path.lineTo(historicalX,historicalY)
                }
                path.lineTo(event.x, event.y)
            }
            else -> {
                return false
            }
        }

        invalidate()

        lastTouchX = event.x
        lastTouchY = event.y

        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(background)
        canvas.drawPath(path,paint)
    }

    fun clear(){
        path.reset()
        invalidate()
    }


}