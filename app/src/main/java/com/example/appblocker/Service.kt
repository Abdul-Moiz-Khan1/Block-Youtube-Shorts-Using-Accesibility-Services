package com.example.appblocker

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AppLoggerService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isDialerBlocked = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event?.source == null) return

        val rootNode = rootInActiveWindow ?: return
        Log.d("AppLogger", "Event: ${rootNode.packageName}")
        val reelRecycler = rootNode.findAccessibilityNodeInfosByViewId(
            "com.google.android.youtube:id/reel_recycler"
        )

        if (rootNode.packageName.contains("com.sh.smart.caller")) {
            showDialerBlocker()
            Log.d("AppLogger", "Dialer Detected")
        }
        if (reelRecycler.isNotEmpty()) {
            Log.d("YTShortsDetector", "✅ YouTube Shorts is OPEN")
            showBlockOverlay()
        } else {
            Log.d("YTShortsDetector", "⛔ Shorts not detected")
        }
    }

    private fun showDialerBlocker() {
        if (overlayView != null || isDialerBlocked) return
        isDialerBlocked = true
        Handler(Looper.getMainLooper()).postDelayed({ isDialerBlocked = false }, 10000)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            PixelFormat.TRANSLUCENT
        )

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.block_dialer_overlay, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = overlayView?.windowInsetsController
            controller?.show(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars())
        }

        val pin = overlayView?.findViewById<EditText>(R.id.pin)
        val submit = overlayView?.findViewById<Button>(R.id.submit_btn)
        val back= overlayView?.findViewById<ImageView>(R.id.backspace)
        val buttonIds = listOf(
            R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six,
            R.id.seven, R.id.eight, R.id.nine
        )
        back?.setOnClickListener {
            val text = pin?.text
            if (!text.isNullOrEmpty()) {
                text.delete(text.length - 1, text.length)
            }
        }

        for (id in buttonIds) {
            overlayView?.findViewById<AppCompatButton>(id)?.setOnClickListener { button ->
                val digit = (button as AppCompatButton).text.toString()
                pin?.append(digit)
            }
        }
        submit?.setOnClickListener {
            if (pin?.text.toString() == "1234") {
                performGlobalAction(GLOBAL_ACTION_BACK)
                windowManager?.removeView(overlayView)
                overlayView = null
            } else {
                pin?.text?.clear()
            }
        }


        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager?.addView(overlayView, layoutParams)
    }


    private fun showBlockOverlay() {
        if (overlayView != null) return // Already shown

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.block_overlay, null)
        val backButton = overlayView?.findViewById<Button>(R.id.backButton)
        backButton?.setOnClickListener {
            performGlobalAction(GLOBAL_ACTION_BACK)

            windowManager?.removeView(overlayView)
            overlayView = null
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager?.addView(overlayView, layoutParams)
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        Log.d("AppLogger", "Accessibility Service Connected")
    }

    private fun containsShortsKeyword(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.text?.toString()?.contains("Shorts", ignoreCase = true) == true) return true

        for (i in 0 until node.childCount) {
            if (containsShortsKeyword(node.getChild(i))) return true
        }

        return false
    }
}
