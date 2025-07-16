package com.example.appblocker

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.Toast

class AppLoggerService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
//    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            val packageName = event.packageName?.toString()
//            Log.d("AppLogger", "Opened: $packageName")
//            if (packageName == "com.android.chrome") {
//                Log.d("AppLogger", "conition true")
//                showBlockOverlay()
//            }else{
//                removeOverlay()
//            }
//        }
//    }

//    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//    if (event == null || event.packageName != "com.google.android.youtube") return
//
//    val rootNode = if(event.source != null){
//
//        Log.d("YTShortsDetector", "${event.source.toString()}")
//        event.source
//    }  else {
//
//
//        Log.d("YTShortsDetector", "${event.source.toString()}")
//        return
//    }
//    Log.d("YTShortsDetector", "${rootNode.toString()}")
//
//    if (isYouTubeShorts(rootNode)) {
//        Log.d("YTShortsDetector", "YouTube Shorts opened")
////        showBlockOverlay()
//    }
//    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event?.source == null) return

    val rootNode = rootInActiveWindow ?: return
    val reelRecycler = rootNode.findAccessibilityNodeInfosByViewId(
        "com.google.android.youtube:id/reel_recycler"
    )

    if (reelRecycler.isNotEmpty()) {
        Log.d("YTShortsDetector", "✅ YouTube Shorts is OPEN")
        showBlockOverlay()
    } else {
        Log.d("YTShortsDetector", "⛔ Shorts not detected")
    }
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

    private fun removeOverlay() {
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
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
