package com.plug.cupgame

import android.animation.Animator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import com.plug.cupgame.databinding.ActivityPlugBinding
import com.plug.cupgame.utils.GameState
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlugActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlugBinding

    private val viewModel: PlugViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prevInit()

        viewModel.progress.observe(this) {
            if(it != null){
                binding.score.text = it.score.toString()
            }
        }

        viewModel.gameState.observe(this) {
            if(it != null) {
                when(it) {
                    GameState.IDLE -> {
                        binding.button.setOnClickListener {
                            viewModel.emitGameState(GameState.STARTED)
                        }
                    }
                    GameState.STARTED -> {
                        binding.button.isEnabled = false
                        binding.ball.visibility = View.INVISIBLE
                        viewModel.cups.value!!.forEachIndexed { ind, el ->
                            if(el.translationY != 0f){
                                el.animate().translationY(0f).start()
                            }
                            handler.postDelayed({
                                viewModel.animateCups()
                            }, 1000)
                        }
                    }
                    GameState.WAIT -> {
                        viewModel.cups.value!!.forEachIndexed { ind, el ->
                            el.setOnClickListener {
                                val randomCupInd = (0..2).random()
                                val winnerCup = viewModel.cups.value!![randomCupInd]
                                binding.ball.x = winnerCup.x + (winnerCup.width / 4)
                                binding.ball.visibility = View.VISIBLE
                                if(ind == randomCupInd) {
                                    el.animate().translationY(-(binding.ball.height + 100).toFloat()).start()
                                    viewModel.emitGameState(GameState.WIN)
                                } else {
                                    el.animate().translationY(-(binding.ball.height + 100).toFloat()).start()
                                    winnerCup.animate().translationY(-(binding.ball.height + 100).toFloat()).setStartDelay(500).start()
                                    viewModel.emitGameState(GameState.LOSE)
                                }
                            }
                        }
                    }
                    GameState.WIN -> {
                        Toast.makeText(this@PlugActivity, "WIN!", Toast.LENGTH_SHORT).show()
                        viewModel.addScore()
                        binding.button.isEnabled = true
                        binding.button.setOnClickListener {
                            viewModel.emitGameState(GameState.STARTED)
                        }
                    }
                    GameState.LOSE -> {
                        Toast.makeText(this@PlugActivity, "LOSE!", Toast.LENGTH_SHORT).show()
                        viewModel.subtractScore()
                        binding.button.isEnabled = true
                        binding.button.setOnClickListener {
                            viewModel.emitGameState(GameState.STARTED)
                        }
                    }
                }
            }
        }
    }

    private fun prevInit() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.ball.viewTreeObserver.removeOnGlobalLayoutListener(this)
                viewModel.initButtons(binding.cup1, binding.cup2, binding.cup3)
                binding.cup2.translationY = -(binding.ball.height + 50).toFloat()
                binding.button.isEnabled = true
            }
        })
    }
}