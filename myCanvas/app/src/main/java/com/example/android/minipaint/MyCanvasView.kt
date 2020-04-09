package com.example.android.minipaint

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

private const val STROKE_WIDTH =12f

class MyCanvasView(context: Context): View(context) {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitMap: Bitmap

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground,null)
    private val drawColor = ResourcesCompat.getColor(resources,R.color.colorPaint, null)

    //set up the paint with which to draw
    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    private var path = Path()

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private var currentX = 0f
    private var currentY = 0f

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private lateinit var frame: Rect

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if(::extraBitMap.isInitialized) extraBitMap.recycle()
        extraBitMap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitMap)
        extraCanvas.drawColor(backgroundColor)

        val inset = 40
        frame = Rect(inset, inset,w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitMap,0f,0f,null)
        canvas.drawRect(frame,paint)
    }

    private fun touchStart(){
        path.reset()
        path.moveTo(motionTouchEventX,motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove(){
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)

        if (dx >= touchTolerance || dy >= touchTolerance ){
            path.quadTo(currentX,currentY,(motionTouchEventX + currentX)/2, (motionTouchEventY + currentY)/2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            //draw the path in the extraBitMap to cache it.
            extraCanvas.drawPath(path,paint)
        }
        invalidate()
    }

    private fun touchUp(){
        //Reset the path so it doesn't get drawn again.
        path.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

}