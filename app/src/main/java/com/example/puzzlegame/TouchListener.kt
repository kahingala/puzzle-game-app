package com.example.puzzlegame

import PuzzleActivity
import PuzzlePiece
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

class TouchListener(private val activity: PuzzleActivity) : View.OnTouchListener {

    private var xDelta = 0f
    private var yDelta = 0f

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        val x = motionEvent!!.rawX
        val y = motionEvent.rawY
        val tolerance = Math.sqrt(
            Math.pow(view!!.width.toDouble(), 2.0) +
                    Math.pow(view.height.toDouble(), 2.0)
        ) / 10

        val piece = view as PuzzlePiece
        if (!piece.canMove) {
            return true
        }
        val lParams = view.layoutParams as RelativeLayout.LayoutParams
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                xDelta = x - lParams.leftMargin
                yDelta = y - lParams.topMargin

                piece.bringToFront()
            }
            MotionEvent.ACTION_MOVE -> {
                lParams.leftMargin = (x - xDelta).toInt()
                lParams.topMargin = (y - yDelta).toInt()
                view.layoutParams = lParams
            }
            MotionEvent.ACTION_UP -> {
                val xDiff = StrictMath.abs(x - lParams.leftMargin)
                val yDiff = StrictMath.abs(y - lParams.topMargin)

                if (xDiff <= tolerance && yDiff <= tolerance) {
                    lParams.leftMargin = (x - xDelta).toInt()
                    lParams.topMargin = (y - yDelta).toInt()
                    piece.layoutParams = lParams
                    piece.canMove = false
                    sendViewToBack(piece)
                    activity.checkGameOver()
                }

            }
        }
        return true
    }

    private fun sendViewToBack(child: View) {
        val parent = child.parent as ViewGroup
        parent.removeView(child)
        parent.addView(child, 0)
    }
}
