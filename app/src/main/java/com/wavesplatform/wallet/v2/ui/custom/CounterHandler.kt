package com.wavesplatform.wallet.v2.ui.custom

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.wavesplatform.wallet.v2.util.clearBalance
import java.math.BigDecimal

class CounterHandler private constructor(builder: Builder) {
    internal val handler = Handler()
    internal val handlerRange = Handler()
    private val incrementalView: View?
    private val decrementalView: View?
    private val valueView: EditText?
    private var counterDelay = 200
    private var rangeDelay = 2000
    private var rangeOffset = 0

    private var autoIncrement = false
    private var autoDecrement = false
    private var autoRangeIncrement = false

    private val listener: CounterListener?

    private val counterRunnable = object : Runnable {
        override fun run() {
            if (autoIncrement) {
                doOperation(true)
                handler.postDelayed(this, counterDelay.toLong())
            } else if (autoDecrement) {
                doOperation(false)
                handler.postDelayed(this, counterDelay.toLong())
            }
        }
    }

    private val rangeRunnable = object : Runnable {
        override fun run() {
            rangeOffset++
            Log.d("autoUpdateDEX:", "increment range $rangeOffset")
            handlerRange.postDelayed(this, rangeDelay.toLong())
        }
    }

    init {
        incrementalView = builder.incrementalView
        decrementalView = builder.decrementalView
        valueView = builder.valueView
        counterDelay = builder.counterDelay
        rangeDelay = builder.rangeDelay
        listener = builder.listener

        initDecrementalView()
        initIncrementalView()
    }

    private fun initIncrementalView() {
        incrementalView?.setOnClickListener { doOperation(true) }

        incrementalView?.setOnLongClickListener {
            autoIncrement = true
            handler.postDelayed(counterRunnable, counterDelay.toLong())
            if (!autoRangeIncrement) {
                autoRangeIncrement = true
                handlerRange.postDelayed(rangeRunnable, rangeDelay.toLong())
            }
            false
        }
        incrementalView?.setOnTouchListener { v, event ->
            if ((event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) && autoIncrement) {
                clearAutoUpdating()
            }
            false
        }
    }

    private fun initDecrementalView() {
        decrementalView?.setOnClickListener { doOperation(false) }

        decrementalView?.setOnLongClickListener {
            autoDecrement = true
            handler.postDelayed(counterRunnable, counterDelay.toLong())
            if (!autoRangeIncrement) {
                autoRangeIncrement = true
                handlerRange.postDelayed(rangeRunnable, rangeDelay.toLong())
            }
            false
        }
        decrementalView?.setOnTouchListener { v, event ->
            if ((event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) && autoDecrement) {
                clearAutoUpdating()
            }
            false
        }
    }

    private fun clearAutoUpdating() {
        Log.d("autoUpdateDEX:", "clear")
        autoRangeIncrement = false
        autoDecrement = false
        autoIncrement = false
        handlerRange.removeCallbacksAndMessages(null)
        rangeOffset = 0
    }

    private fun doOperation(increment: Boolean) {
        var currentValue = valueView?.text.toString()

        if (currentValue.trim().isEmpty() || currentValue.trim() == ".") currentValue = "0"

        var rang: Int =
                if (currentValue.contains(".")) currentValue.length - 1
                else currentValue.length

        val doteIndex = currentValue.indexOf(".")

        rang -= rangeOffset

        val incrementValueWithRang = StringBuilder()
        for (i in 0 until rang) {
            if (i == doteIndex)
                incrementValueWithRang.append(".")
            else
                incrementValueWithRang.append("0")
        }

        incrementValueWithRang.append("1")

        val t = BigDecimal(currentValue.clearBalance())

        if (increment) {
            var number = t.add(BigDecimal(incrementValueWithRang.toString()))
            if (number < BigDecimal.ZERO) {
                number = BigDecimal.ZERO
            }
            listener?.onIncrement(valueView, number)
        } else {
            var number = t.subtract(BigDecimal(incrementValueWithRang.toString()))
            if (number < BigDecimal.ZERO) {
                number = BigDecimal.ZERO
            }
            listener?.onDecrement(valueView, number)
        }
    }

    interface CounterListener {
        fun onIncrement(view: EditText?, number: BigDecimal)
        fun onDecrement(view: EditText?, number: BigDecimal)
    }

    class Builder {
        var incrementalView: View? = null
        var decrementalView: View? = null
        var valueView: EditText? = null
        var counterDelay = 200
        var rangeDelay = 2000
        var listener: CounterListener? = null

        fun incrementalView(view: View): Builder {
            incrementalView = view
            return this
        }

        fun decrementalView(view: View): Builder {
            decrementalView = view
            return this
        }

        fun counterDelay(value: Int): Builder {
            counterDelay = value
            return this
        }

        fun listener(listener: CounterListener): Builder {
            this.listener = listener
            return this
        }

        fun valueView(view: EditText): Builder {
            valueView = view
            return this
        }

        fun rangeDelay(value: Int): Builder {
            rangeDelay = value
            return this
        }

        fun build(): CounterHandler {
            return CounterHandler(this)
        }
    }
}