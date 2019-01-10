package io.github.droidkaigi.confsched2019.session.ui.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.soywiz.klock.DateTimeSpan
import com.xwray.groupie.GroupAdapter
import io.github.droidkaigi.confsched2019.session.R
import io.github.droidkaigi.confsched2019.session.ui.item.SessionItem
import io.github.droidkaigi.confsched2019.timber.debug
import timber.log.Timber

class SessionsItemDecoration(
    val context: Context,
    val groupAdapter: GroupAdapter<*>
) : RecyclerView.ItemDecoration() {
    private val resources = context.resources
    private val textSize = resources.getDimensionPixelSize(
        R.dimen.session_bottom_sheet_left_time_text_size
    )
    private val textLeftSpace = resources.getDimensionPixelSize(
        R.dimen.session_bottom_sheet_left_time_text_left
    )
    private val textPaddingTop = resources.getDimensionPixelSize(
        R.dimen.session_bottom_sheet_left_time_text_padding_top
    )
    private val textPaddingBottom = resources.getDimensionPixelSize(
        R.dimen.session_bottom_sheet_left_time_text_padding_bottom
    )

    val paint = Paint().apply {
        style = Paint.Style.FILL
        textSize = this@SessionsItemDecoration.textSize.toFloat()
        color = Color.BLACK
        isAntiAlias = true
        try {
            typeface = ResourcesCompat.getFont(context, R.font.lekton)
        } catch (e: Resources.NotFoundException) {
            Timber.debug(e)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var lastTime: String? = null
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(view)
            if (position == -1 || position >= groupAdapter.itemCount) return

            val time = getSessionTime(position) ?: continue

            if (lastTime == time) continue
            lastTime = time

            val nextTime = getSessionTime(position + 1)

            var textY = view.top.coerceAtLeast(0) + textPaddingTop + textSize
            if (time != nextTime) {
                textY = textY.coerceAtMost(view.bottom - textPaddingBottom)
            }

            c.drawText(
                time,
                textLeftSpace.toFloat(),
                textY.toFloat(),
                paint
            )
        }
    }

    private val displayTimezoneOffset = lazy {
        DateTimeSpan(hours = 9) // FIXME Get from device setting
    }

    private fun getSessionTime(position: Int): String? {
        if (position < 0 || position >= groupAdapter.itemCount) {
            return null
        }

        val item = groupAdapter.getItem(position) as? SessionItem ?: return null
        return item.session.startTime
            .plus(displayTimezoneOffset.value).toString("HH:mm")
    }
}
