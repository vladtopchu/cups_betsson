package com.plug.cupgame.utils

import android.animation.Animator
import android.view.ViewPropertyAnimator
import android.widget.ImageView

enum class GameState {
    IDLE,
    READY,
    STARTED,
    WAIT,
    WIN,
    LOSE,
}