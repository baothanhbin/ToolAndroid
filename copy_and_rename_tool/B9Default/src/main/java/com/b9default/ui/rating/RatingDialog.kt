package com.b9default.ui.rating

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import com.b9default.a1_common_utils.view.tap
import com.b9ui.R
import com.b9ui.databinding.RatingDialogBinding


class RatingDialog(context: Context) :
    Dialog(context, R.style.BaseDialog) {
    private var onPress: OnPress? = null
    private var binding: RatingDialogBinding = RatingDialogBinding.inflate(layoutInflater)
    private var s = 5

    init {
        setContentView(binding.root)
        val attributes = window?.attributes
        attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.let {
            it.attributes = attributes
            it.setSoftInputMode(16)
        }
        binding.ratingBar.rating = 5.0f
        onclick()
        changeRating()
    }

    interface OnPress {
        fun send(s: Int)

        fun rating(s: Int)

        fun cancel()

        fun later()

        fun gotIt()
    }

    fun init(onPress: OnPress?) {
        this.onPress = onPress
    }

    private fun changeRating() {
        binding.ratingBar.onRatingBarChangeListener =
            OnRatingBarChangeListener { _: RatingBar?, _: Float, _: Boolean ->
                val getRating = binding.ratingBar.rating.toString()
                s = when (getRating) {
                    "1.0" -> 1
                    "2.0" -> 2
                    "3.0" -> 3
                    "4.0" -> 4
                    "5.0" -> 5
                    else -> 0
                }
            }
    }

    private fun onclick() {
        binding.btnSubmit.tap {
            Log.d("TAG23", "onclick: ${binding.ratingBar.rating}")
            if (binding.ratingBar.rating <= 4.0) {
                onPress.let {
                    it?.send(s)
                }
            } else {
                onPress.let {
                    it?.rating(s)
                }
            }
            binding.tvTitle.text = context.getString(R.string.thank_you)
            binding.tvContent.text =
                context.getString(R.string.thank_you_for_taking_the_time_to_rate_us_i_m_really_appreciate_that)
            binding.btnGotit.visibility = View.VISIBLE
            binding.ratingBar.visibility = View.GONE
        }

        binding.btnCancel.tap {
            onPress.let {
                it?.later()
            }
        }

        binding.imgClose.tap {
            onPress.let {
                it?.cancel()
            }
        }
        binding.btnGotit.tap {
            onPress.let {
                it?.gotIt()
            }
        }
    }
}