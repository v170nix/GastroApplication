package net.arwix.gastro.client.common

import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.merge_multiline_button.view.*
import net.arwix.extension.gone
import net.arwix.extension.visible

class MultilineButtonHelper(private val view: View, isShowInInit: Boolean = false) {
    private val textStart1: TextView = view.multiline_button_start_line_1_text
    private val textStart2: TextView = view.multiline_button_start_line_2_text
    private val textEnd1: TextView = view.multiline_button_end_line_1_text
    private val textEnd2: TextView = view.multiline_button_end_line_2_text

    var isEnabled: Boolean = isShowInInit

    init {
        if (isShowInInit) visible() else gone()
    }

    fun setTexts(line11: String, line12: String, line21: String, line22: String) {
        textStart1.text = line11
        textStart2.text = line21
        textEnd1.text = line12
        textEnd2.text = line22
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        view.setOnClickListener {
            if (isEnabled) listener.onClick(it)
        }
    }

    fun visible() {
        view.scaleY = 1f
        view.scaleX = 1f
        view.visible()
    }

    fun gone() {
        view.scaleY = 0f
        view.scaleX = 0f
        view.gone()
    }

    fun show(duration: Long = 250L) {
        view.animate()
            .withStartAction {
                view.visible()
            }
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .start()
    }

    fun hide(duration: Long = 200L) {
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(duration)
            .withEndAction {
                view.gone()
            }
            .start()
    }


}