package com.wellington.lenstranslate.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.wellington.lenstranslate.ocr.OcrEngine
import com.wellington.lenstranslate.translate.TranslatorEngine
import kotlinx.coroutines.*

class FloatingLensService : Service() {
    companion object {
        var captureResultCode: Int = Activity.RESULT_CANCELED
        var captureData: Intent? = null
    }

    private lateinit var wm: WindowManager
    private lateinit var panel: LinearLayout
    private lateinit var output: TextView
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val ocr = OcrEngine()
    private val translator = TranslatorEngine()

    private var projection: MediaProjection? = null
    private var imageReader: ImageReader? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(7, notification())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        addOverlay()
    }

    override fun onDestroy() {
        scope.cancel()
        runCatching { wm.removeView(panel) }
        imageReader?.close()
        projection?.stop()
        super.onDestroy()
    }

    private fun addOverlay() {
        panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(0xCC111111.toInt())
        }
        val button = Button(this).apply {
            text = "TR"
            setOnClickListener { captureTranslateAndShow() }
        }
        output = TextView(this).apply {
            text = "Toque em TR para traduzir a tela"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            maxWidth = 850
        }
        panel.addView(button)
        panel.addView(output)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 24
            y = 160
        }
        wm.addView(panel, params)
    }

    private fun captureTranslateAndShow() {
        scope.launch {
            output.text = "Lendo tela..."
            val bitmap = withContext(Dispatchers.Default) { captureScreenBitmap() }
            if (bitmap == null) {
                output.text = "Permita captura de tela e toque novamente."
                return@launch
            }
            runCatching {
                val text = ocr.read(bitmap)
                val translated = translator.translate(text)
                output.text = if (text.isBlank()) "Nenhum texto encontrado." else "Original:\n$text\n\nTradução:\n$translated"
            }.onFailure { output.text = "Erro: ${it.message}" }
        }
    }

    private fun captureScreenBitmap(): Bitmap? {
        val data = captureData ?: return null
        if (projection == null) {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projection = manager.getMediaProjection(captureResultCode, data)
        }

        val metrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        if (imageReader == null) {
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            projection?.createVirtualDisplay(
                "LensTranslateCapture",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface,
                null,
                null
            )
            Thread.sleep(350)
        }

        val image = imageReader?.acquireLatestImage() ?: return null
        image.use {
            val plane = it.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width
            val paddedBitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
            paddedBitmap.copyPixelsFromBuffer(buffer)
            return Bitmap.createBitmap(paddedBitmap, 0, 0, width, height)
        }
    }

    private fun notification(): Notification {
        val channelId = "lens_translate"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Lens Translate", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("Lens Translate ativo")
            .setContentText("Toque no botão flutuante para traduzir a tela")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .build()
    }
}
