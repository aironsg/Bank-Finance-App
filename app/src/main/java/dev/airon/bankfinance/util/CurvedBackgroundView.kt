package dev.airon.bankfinance.util



import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CurvedBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val gradientColors = intArrayOf(
        Color.parseColor("#27A7E7"),
        Color.parseColor("#2196F3"),
        Color.parseColor("#2272EB")
    )

    init {
        setWillNotDraw(false) // 🔧 ESSENCIAL para garantir que o onDraw será chamado
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        if (w == 0f || h == 0f) return

        // Gradiente vertical
        val gradient = LinearGradient(
            0f, 0f, 0f, h,
            gradientColors, null, Shader.TileMode.CLAMP
        )
        paint.shader = gradient

        // Desenhar caminho com curva côncava
        path.reset()
        path.moveTo(0f, 0f)
        path.lineTo(0f, h - 100f)
        path.quadTo(w / 2, h + 100f, w, h - 100f)
        path.lineTo(w, 0f)
        path.close()

        canvas.drawPath(path, paint)
    }
}
