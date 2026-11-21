package com.aydanailroom.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.DashPathEffect
import com.aydanailroom.app.R
import com.aydanailroom.app.data.ServiceEntity
import java.io.File
import java.io.FileOutputStream

object PriceMenuRenderer {
    fun render(context: Context, services: List<ServiceEntity>): File {
        val width = 1080
        val rowHeight = 100
        val headerHeight = 250
        val footerHeight = 180
        val height = headerHeight + services.size.coerceAtLeast(1) * rowHeight + footerHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val board = android.graphics.Color.rgb(43, 58, 51)
        val wood = android.graphics.Color.rgb(139, 90, 43)
        val woodLight = android.graphics.Color.rgb(169, 116, 63)
        val chalk = android.graphics.Color.rgb(242, 240, 230)
        val pink = android.graphics.Color.rgb(244, 166, 192)
        val yellow = android.graphics.Color.rgb(246, 226, 122)
        val dim = android.graphics.Color.rgb(191, 194, 181)

        canvas.drawColor(wood)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = woodLight; style = Paint.Style.STROKE; strokeWidth = 8f }
        canvas.drawRoundRect(RectF(18f, 18f, width - 18f, height - 18f), 28f, 28f, framePaint)
        val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = board }
        canvas.drawRoundRect(RectF(42f, 42f, width - 42f, height - 42f), 18f, 18f, boardPaint)

        val heading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = chalk
            textSize = 70f
            textAlign = Paint.Align.CENTER
            typeface = runCatching { context.resources.getFont(R.font.lalezar_regular) }.getOrElse { Typeface.DEFAULT_BOLD }
        }
        canvas.drawText(context.getString(R.string.services_title), width / 2f, 145f, heading)
        val handle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pink
            textSize = 38f
            textAlign = Paint.Align.CENTER
            typeface = runCatching { context.resources.getFont(R.font.vazirmatn_medium) }.getOrElse { Typeface.DEFAULT }
        }
        canvas.drawText(context.getString(R.string.brand_handle), width / 2f, 205f, handle)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = chalk
            textSize = 38f
            textAlign = Paint.Align.RIGHT
            typeface = runCatching { context.resources.getFont(R.font.vazirmatn_medium) }.getOrElse { Typeface.DEFAULT }
        }
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = yellow
            textSize = 34f
            textAlign = Paint.Align.LEFT
            typeface = runCatching { context.resources.getFont(R.font.vazirmatn_bold) }.getOrElse { Typeface.DEFAULT_BOLD }
        }
        val dotted = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(150, 191, 194, 181)
            strokeWidth = 3f
            pathEffect = DashPathEffect(floatArrayOf(5f, 8f), 0f)
        }
        services.filter { it.isActive }.forEachIndexed { index, service ->
            val baseline = headerHeight + index * rowHeight + 58f
            canvas.drawText(service.name.take(28), width - 100f, baseline, namePaint)
            canvas.drawText(formatToman(context, service.defaultPrice), 100f, baseline, pricePaint)
            val left = 390f
            val right = 660f
            canvas.drawLine(left, baseline - 10f, right, baseline - 10f, dotted)
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dim
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = runCatching { context.resources.getFont(R.font.vazirmatn_regular) }.getOrElse { Typeface.DEFAULT }
        }
        canvas.drawText(context.getString(R.string.tagline), width / 2f, height - 100f, footerPaint)
        val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pink; textSize = 44f; textAlign = Paint.Align.CENTER }
        canvas.drawText("✧   ✿   ✧", width / 2f, height - 50f, sparkPaint)

        val directory = File(context.cacheDir, "share").apply { mkdirs() }
        val output = File(directory, "ayda-price-menu.png")
        FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return output
    }
}
