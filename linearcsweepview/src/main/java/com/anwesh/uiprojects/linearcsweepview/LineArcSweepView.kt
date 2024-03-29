package com.anwesh.uiprojects.linearcsweepview

/**
 * Created by anweshmishra on 25/10/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.graphics.Canvas

val nodes : Int = 5
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val delay : Long = 20
val foreColor : Int = Color.parseColor("#01579B")
val backColor : Int = Color.parseColor("#BDBDBD")
val sweepDeg : Float = 60f
val parts : Int = 2
val startDeg : Float = 270f
val arcFactor : Float = 0.67f

fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawLineArc(size : Float, scale : Float, paint : Paint) {
    val sc : Float = scale.sinify()
    val deg : Float = sweepDeg * sc
    save()
    rotate(deg)
    drawLine(0f, 0f, 0f, -size, paint)
    restore()
    drawArc(RectF(-size * arcFactor, -size * arcFactor, size * arcFactor, size * arcFactor), startDeg, deg, true, paint)
}

fun Canvas.drawLineArcSweep(size : Float, scale : Float, paint : Paint) {
    for (j in 0..(parts - 1)) {
        save()
        scale(1f - 2 * j, 1f)
        drawLineArc(size, scale, paint)
        restore()
    }
}

fun Canvas.drawLASNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(gap * (i + 1), h / 2)
    drawLineArcSweep(size, scale, paint)
    restore()
}

class LineArcSweepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }


    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LASNode(var i : Int, val state : State = State()) {

        private var next : LASNode? = null
        private var prev : LASNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LASNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLASNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LASNode {
            var curr : LASNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineArcSweep(var i : Int) {

        private val root : LASNode = LASNode(0)
        private var curr : LASNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineArcSweepView) {

        private val animator : Animator = Animator(view)
        private val las : LineArcSweep = LineArcSweep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            las.draw(canvas, paint)
            animator.animate {
                las.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            las.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : LineArcSweepView {
            val view : LineArcSweepView = LineArcSweepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}