package io.github.fyy0619cell.deskpet.share

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import io.github.fyy0619cell.deskpet.pet.Adoption
import io.github.fyy0619cell.deskpet.quiz.Matcher
import io.github.fyy0619cell.deskpet.render.Pose
import io.github.fyy0619cell.deskpet.render.Rarity
import io.github.fyy0619cell.deskpet.render.Renderers

object ShareCard {
    const val WIDTH = 1080
    const val HEIGHT = 1440
    private const val PAPER = 0xFFF6F4F1.toInt()
    private const val INK = 0xFF231E29.toInt()
    private const val MUTED = 0xFF8C8592.toInt()
    private const val HAIRLINE = 0x1F231E29
    private const val GOLD = 0xFFB98A2E.toInt()

    fun commonGround(a: Adoption): List<String> {
        val answers = a.answers.split(',').mapNotNull { it.trim().toIntOrNull() }.toIntArray()
        return runCatching { Matcher.commonGround(answers, a.species) }.getOrDefault(emptyList())
    }

    fun render(a: Adoption, accent: Int, appName: String): Bitmap {
        val bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val sp = a.species
        val look = a.look
        c.drawColor(PAPER)
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(WIDTH / 2f, 520f, 520f, (accent and 0x00FFFFFF) or 0x1A000000, 0x00000000, Shader.TileMode.CLAMP)
        }
        c.drawCircle(WIDTH / 2f, 520f, 520f, glow)

        val m = 96f
        val text = Paint(Paint.ANTI_ALIAS_FLAG)
        text.color = MUTED; text.textSize = 30f; text.letterSpacing = 0.5f
        c.drawText("我的本命动物", m, 150f, text)

        c.save()
        c.translate((WIDTH - 760f) / 2f, 150f)
        c.scale(3.8f, 3.8f)
        Renderers.create(look).draw(c, Pose().apply { time = 0.6f })
        c.restore()

        text.letterSpacing = 0.04f
        text.color = INK; text.textSize = 104f; text.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        c.drawText(a.name, m, 1000f, text)
        text.color = MUTED; text.textSize = 38f; text.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        c.drawText("${sp.nameZh} · ${sp.latin}", m, 1062f, text)

        var x = m
        x = chip(c, sp.era, x, 1100f, MUTED) + 16f
        if (look.rare != Rarity.NORMAL) chip(c, rarityText(look.rare), x, 1100f, GOLD)

        val line = Paint().apply { color = HAIRLINE; strokeWidth = 2f }
        c.drawLine(m, 1200f, WIDTH - m, 1200f, line)
        text.typeface = Typeface.DEFAULT; text.textSize = 34f; text.color = INK
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
        commonGround(a).take(2).forEachIndexed { i, s ->
            val y = 1262f + i * 56f
            c.drawCircle(m + 6f, y - 11f, 6f, dot)
            c.drawText(s, m + 30f, y, text)
        }
        text.textSize = 26f; text.color = MUTED; text.letterSpacing = 0.2f
        val footer = "$appName · 测测你的本命动物"
        c.drawText(footer, WIDTH - m - text.measureText(footer), HEIGHT - 70f, text)
        return bmp
    }

    private fun chip(c: Canvas, label: String, x: Float, top: Float, color: Int): Float {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 28f; this.color = color; letterSpacing = 0.05f }
        val w = p.measureText(label) + 48f
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 2f; this.color = (color and 0x00FFFFFF) or 0x66000000
        }
        c.drawRoundRect(RectF(x, top, x + w, top + 56f), 28f, 28f, border)
        c.drawText(label, x + 24f, top + 38f, p)
        return x + w
    }

    private fun rarityText(r: Rarity) = when (r) {
        Rarity.ALBINO -> "稀有 · 白化"
        Rarity.GOLDEN -> "稀有 · 金色"
        Rarity.MELANISTIC -> "稀有 · 黑化"
        Rarity.NORMAL -> ""
    }

    /** Saves into Pictures/<folder> through MediaStore; no storage permission needed on API 29+. */
    fun save(context: Context, bmp: Bitmap, folder: String, fileName: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$folder")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values) ?: return null
        val ok = runCatching {
            resolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) } == true
        }.getOrDefault(false)
        if (!ok) {
            resolver.delete(uri, null, null)
            return null
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }
}
