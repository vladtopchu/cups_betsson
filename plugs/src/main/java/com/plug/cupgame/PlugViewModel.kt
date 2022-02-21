package com.plug.cupgame

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plug.cupgame.data.local.daos.ProgressDao
import com.plug.cupgame.data.local.entities.ProgressEntity
import com.plug.cupgame.utils.GameState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlugViewModel @Inject constructor(
    private val progressDao: ProgressDao
): ViewModel() {
    // 0 - idle, 1 - in process, 2 - sorting finished
    private val _gameState = MutableLiveData(GameState.IDLE)
    val gameState = _gameState as LiveData<GameState>

    private val _progress = MutableLiveData<ProgressEntity>(null)
    val progress = _progress as LiveData<ProgressEntity>

    private val _cupsPoses = MutableLiveData<List<Float>>(null)
    val cupsPoses = _cupsPoses as LiveData<List<Float>>

    private val _cups = MutableLiveData<List<ImageView>>(null)
    val cups = _cups as LiveData<List<ImageView>>

    private val _lastCup = MutableLiveData(1)
    val lastCup = _lastCup as LiveData<Int>

    init {
        viewModelScope.launch {
            progressDao.getProgress().collect {
                _progress.postValue(it)
            }
        }
    }

    fun subtractScore() {
        val newScore = _progress.value!!.score - 1
        if(newScore < 0) {
            viewModelScope.launch {
                progressDao.updateProgress(0)
            }
        } else {
            viewModelScope.launch {
                progressDao.updateProgress(newScore)
            }
        }
    }

    fun addScore() {
        val newScore = _progress.value!!.score + 1
        viewModelScope.launch {
            progressDao.updateProgress(newScore)
        }
    }

    fun initButtons(vararg buttons: ImageView) {
        _cupsPoses.postValue(buttons.map { el -> el.x })
        _cups.postValue(buttons.toList())
    }

    fun emitGameState(state: GameState) {
        _gameState.postValue(state)
    }

    @SuppressLint("Recycle")
    fun animateCups() {
        val animator1 =
            ObjectAnimator.ofFloat(_cups.value!![0], "x", _cupsPoses.value!![2])
        val animator2 =
            ObjectAnimator.ofFloat(_cups.value!![2], "x", _cupsPoses.value!![0])
        val animator3 =
            ObjectAnimator.ofFloat(_cups.value!![1], "x", _cupsPoses.value!![0]).apply {
                startDelay = 600
            }
        val animator4 =
            ObjectAnimator.ofFloat(_cups.value!![2], "x", _cupsPoses.value!![1]).apply {
                startDelay = 600
            }
        val animator5 =
            ObjectAnimator.ofFloat(_cups.value!![1], "x", _cupsPoses.value!![2]).apply {
                startDelay = 1200
            }
        val animator6 =
            ObjectAnimator.ofFloat(_cups.value!![0], "x", _cupsPoses.value!![0]).apply {
                startDelay = 1200
            }

        AnimatorSet().apply {
            playTogether(animator1, animator2)
            playTogether(animator3, animator4)
            playTogether(animator5, animator6)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    _gameState.postValue(GameState.WAIT)
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }

            })
            start()
        }
    }
}