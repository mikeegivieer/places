package com.dutisoft.places

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class AvatarAnimationSurfaceView(context: Context, attrs: AttributeSet?) :
    SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private lateinit var thread: Thread
    private var isRunning = false
    private var frameIndex = 0
    private val frameDuration = 150L // ms por frame

    private lateinit var frames: List<Bitmap>

    init {
        holder.addCallback(this)
        setZOrderOnTop(true) // Muy importante para transparencia
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }




    override fun surfaceCreated(holder: SurfaceHolder) {
        // Carga los frames (puedes agregar más si tienes animación)
        frames = listOf(
            BitmapFactory.decodeResource(resources, R.drawable.avatar)
        )

        isRunning = true
        thread = Thread(this)
        thread.start()
    }

    override fun run() {
        while (isRunning) {
            if (!holder.surface.isValid) continue

            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(Color.TRANSPARENT) // Limpia el canvas

            val frame = frames[frameIndex]

            // Tamaño del canvas
            val canvasWidth = width
            val canvasHeight = height

            // Escalado proporcional
            val scale = minOf(
                canvasWidth.toFloat() / frame.width,
                canvasHeight.toFloat() / frame.height
            )

            val scaledWidth = (frame.width * scale).toInt()
            val scaledHeight = (frame.height * scale).toInt()

            val left = (canvasWidth - scaledWidth) / 2
            val top = (canvasHeight - scaledHeight) / 2

            val destRect = Rect(left, top, left + scaledWidth, top + scaledHeight)

            // Crea un bitmap temporal circular
            val output = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(output)

            // Radio del círculo
            val radius = minOf(canvasWidth, canvasHeight) / 2f

            // 👉 Dibuja borde blanco alrededor del círculo
            val borderPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 10f
                isAntiAlias = true
            }
            tempCanvas.drawCircle(
                canvasWidth / 2f,
                canvasHeight / 2f,
                radius - 5f, // Centrado del borde
                borderPaint
            )

            // 👉 Recorta el canvas con forma circular
            val path = Path()
            path.addCircle(
                canvasWidth / 2f,
                canvasHeight / 2f,
                radius,
                Path.Direction.CCW
            )
            tempCanvas.clipPath(path)

            // 👉 Dibuja el bitmap dentro del recorte circular
            tempCanvas.drawBitmap(frame, null, destRect, null)

            // 👉 Dibuja el resultado final en el canvas real
            canvas.drawBitmap(output, 0f, 0f, null)

            holder.unlockCanvasAndPost(canvas)

            frameIndex = (frameIndex + 1) % frames.size
            Thread.sleep(frameDuration)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        thread.join()
    }
}
