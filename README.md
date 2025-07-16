
# Project Title

A brief description of what this project does and who it's for


## Description
AppBlocker is an Android utility that detects and blocks YouTube Shorts using an accessibility service. When Shorts content is detected, the app displays a full-screen overlay with a back button, preventing the user from watching the content.
Features
Detects YouTube Shorts via AccessibilityNodeInfo.
Blocks Shorts Content with a fullscreen overlay.
Allows Quick Exit using a customizable back button.
Permissions Check for Accessibility and Overlay settings.

## Working

Uses Android’s Accessibility Service to inspect UI elements.

Searches for the YouTube Shorts RecyclerView with the ID

When detected, an overlay view is shown over the screen.

Users can press Back to exit Shorts and return to the previous screen.
