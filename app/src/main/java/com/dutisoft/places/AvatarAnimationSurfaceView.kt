package com.dutisoft.places

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class AvatarAnimationSurfaceView(context: Context, attrs: AttributeSet?) :
    SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var thread: Thread? = null
    private var isRunning = false
    private var frameIndex = 0
    private val frameDuration = 150L // ms por frame

    private var frames: List<Bitmap> = emptyList()

    init {
        holder.addCallback(this)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    fun setFrames(bitmaps: List<Bitmap>) {
        frames = bitmaps
        if (holder.surface.isValid && !isRunning) {
            startAnimation()
        }
    }

    private fun startAnimation() {
        isRunning = true
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (frames.isNotEmpty() && !isRunning) {
            startAnimation()
        }
    }

    override fun run() {
        while (isRunning) {
            if (!holder.surface.isValid || frames.isEmpty()) continue

            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(Color.TRANSPARENT)

            val frame = frames[frameIndex]
            val canvasWidth = width
            val canvasHeight = height

            val scale = minOf(
                canvasWidth.toFloat() / frame.width,
                canvasHeight.toFloat() / frame.height
            )

            val scaledWidth = (frame.width * scale).toInt()
            val scaledHeight = (frame.height * scale).toInt()
            val left = (canvasWidth - scaledWidth) / 2
            val top = (canvasHeight - scaledHeight) / 2
            val destRect = Rect(left, top, left + scaledWidth, top + scaledHeight)

            val output = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(output)
            val radius = minOf(canvasWidth, canvasHeight) / 2f

            val borderPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 10f
                isAntiAlias = true
            }
            tempCanvas.drawCircle(canvasWidth / 2f, canvasHeight / 2f, radius - 5f, borderPaint)

            val path = Path().apply {
                addCircle(canvasWidth / 2f, canvasHeight / 2f, radius, Path.Direction.CCW)
            }
            tempCanvas.clipPath(path)
            tempCanvas.drawBitmap(frame, null, destRect, null)

            canvas.drawBitmap(output, 0f, 0f, null)
            holder.unlockCanvasAndPost(canvas)

            frameIndex = (frameIndex + 1) % frames.size
            Thread.sleep(frameDuration)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        thread?.join()
        thread = null
    }
}
