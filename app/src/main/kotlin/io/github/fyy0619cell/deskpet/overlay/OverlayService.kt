package io.github.fyy0619cell.deskpet.overlay

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.github.fyy0619cell.deskpet.MainActivity
import io.github.fyy0619cell.deskpet.R
import io.github.fyy0619cell.deskpet.pet.Adoption
import io.github.fyy0619cell.deskpet.pet.BrainEvent
import io.github.fyy0619cell.deskpet.pet.PetBrain
import io.github.fyy0619cell.deskpet.pet.PetStore
import io.github.fyy0619cell.deskpet.pet.StatsMath
import io.github.fyy0619cell.deskpet.pet.StatsStore
import io.github.fyy0619cell.deskpet.render.Action
import io.github.fyy0619cell.deskpet.render.Locomotion
import io.github.fyy0619cell.deskpet.render.Rng
import io.github.fyy0619cell.deskpet.sound.SoundPlayer
import io.github.fyy0619cell.deskpet.species.Voice
import io.github.fyy0619cell.deskpet.pet.Settings as PetSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var brain: PetBrain
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var locomotion: Locomotion
    private lateinit var voice: Voice
    private lateinit var rng: Rng
    private var view: PetOverlayView? = null
    private var trip: Trip? = null
    private val pos = FloatArray(2)
    private var t0 = 0L
    private var lastTick = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("overlay", MODE_PRIVATE) }
    private val sound by lazy { SoundPlayer(this) }
    private val stats by lazy { StatsStore(this) }
    private val settings by lazy { PetSettings(this) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val adoption = PetStore(this).adoption()
        // The overlay must be visible before entering the foreground (Android 15 background-start rule).
        val shown = adoption != null && Settings.canDrawOverlays(this) && runCatching { addOverlay(adoption) }.isSuccess
        val entered = runCatching { enterForeground(adoption?.name ?: "") }.isSuccess
        if (!shown || !entered) {
            stopSelf()
            return
        }
        registerReceiver()
        if ((getSystemService(POWER_SERVICE) as PowerManager).isInteractive) view?.start()
        _running.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val now = SystemClock.uptimeMillis()
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_FEED -> if (view != null) { trip = null; brain.feed(now) }
            ACTION_CLEAN -> if (view != null) { trip = null; brain.cheer(now); sound.voice(voice) }
            ACTION_SETTINGS -> if (view != null) brain.wanderEnabled = settings.wanderOn
        }
        return START_STICKY
    }

    private fun onBrainEvent(e: BrainEvent) {
        when (e) {
            BrainEvent.TAPPED, BrainEvent.WOKE -> {
                sound.voice(voice)
                stats.update { s, now -> StatsMath.pet(s, now) }
            }
            BrainEvent.FED -> sound.nom()
            else -> Unit
        }
    }

    override fun onDestroy() {
        _running.value = false
        handler.removeCallbacksAndMessages(null)
        runCatching { unregisterReceiver(screenReceiver) }
        view?.let { v ->
            v.stop()
            runCatching { wm.removeView(v) }
        }
        view = null
        sound.release()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (view == null) return
        trip = null
        if (brain.action == Action.WALK) brain.arrived(SystemClock.uptimeMillis())
        val b = bounds()
        moveTo(b.clampX(params.x.toFloat()), b.clampY(params.y.toFloat()))
    }

    // ---- window ----

    private fun addOverlay(adoption: Adoption) {
        val look = adoption.look
        locomotion = look.locomotion
        voice = adoption.species.voice
        rng = Rng(adoption.seed xor SystemClock.uptimeMillis())
        val size = (SIZE_DP * resources.displayMetrics.density).toInt()
        val b = bounds(size)
        params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = b.clampX(prefs.getFloat(KEY_X, b.maxX - 12 * b.density)).toInt()
            y = b.clampY(prefs.getFloat(KEY_Y, b.height * 0.62f)).toInt()
        }
        val v = PetOverlayView(this, look)
        v.tick = ::onTick
        v.setOnTouchListener(touch)
        wm.addView(v, params)
        view = v
        val now = SystemClock.uptimeMillis()
        t0 = now
        lastTick = now
        brain = PetBrain(adoption.seed, wanderEnabled = settings.wanderOn)
        brain.onEvent = ::onBrainEvent
        brain.start(now)
    }

    private fun bounds(size: Int = params.width): Bounds {
        val density = resources.displayMetrics.density
        val (w, h) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.let { it.width() to it.height() }
        } else {
            @Suppress("DEPRECATION")
            android.util.DisplayMetrics().also { wm.defaultDisplay.getRealMetrics(it) }.let { it.widthPixels to it.heightPixels }
        }
        return Bounds(w, h, (TOP_DP * density).toInt(), size, density)
    }

    private fun moveTo(x: Float, y: Float) {
        val v = view ?: return
        params.x = x.toInt()
        params.y = y.toInt()
        if (v.isAttachedToWindow) runCatching { wm.updateViewLayout(v, params) }
    }

    private fun savePosition() {
        prefs.edit().putFloat(KEY_X, params.x.toFloat()).putFloat(KEY_Y, params.y.toFloat()).apply()
    }

    // ---- frame loop ----

    private fun onTick(now: Long) {
        val v = view ?: return
        val dt = (now - lastTick).coerceIn(0L, 250L)
        lastTick = now
        brain.update(now)
        if (brain.action == Action.WALK && trip == null) {
            trip = Wander.plan(locomotion, params.x.toFloat(), params.y.toFloat(), bounds(), rng, now)
            v.facingRight = trip!!.facingRight
        }
        trip?.let { t ->
            val done = t.position(now, pos)
            moveTo(pos[0], pos[1])
            if (done) {
                trip = null
                brain.arrived(now)
                savePosition()
            }
        }
        v.pose.apply {
            action = brain.action
            time = (now - t0) / 1000f
            if (action == Action.WALK) walkPhase = (walkPhase + dt / 1000f * stepRate()) % 1f
            progress = brain.progress(now)
            blink = brain.blink(now)
        }
    }

    private fun stepRate() = when (locomotion) {
        Locomotion.WALK -> 1.6f
        Locomotion.HOP -> 1.1f
        Locomotion.SLITHER -> 0.8f
        Locomotion.SWIM -> 0.9f
        Locomotion.FLY -> 1f
    }

    // ---- touch ----

    private val touch = object : View.OnTouchListener {
        private var downX = 0f
        private var downY = 0f
        private var startX = 0
        private var startY = 0
        private var dragging = false
        private var longPressed = false
        // Lazy: the service has no Context yet while its fields are being initialised.
        private val slop by lazy { ViewConfiguration.get(this@OverlayService).scaledTouchSlop }
        private val longPress = Runnable {
            longPressed = true
            view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            startActivity(Intent(this@OverlayService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, e: MotionEvent): Boolean {
            val now = SystemClock.uptimeMillis()
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY
                    startX = params.x; startY = params.y
                    dragging = false; longPressed = false
                    handler.postDelayed(longPress, ViewConfiguration.getLongPressTimeout().toLong())
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (!dragging && (abs(dx) > slop || abs(dy) > slop)) {
                        dragging = true
                        handler.removeCallbacks(longPress)
                        trip = null
                        brain.grab(now)
                    }
                    if (dragging) moveTo(startX + dx, startY + dy)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPress)
                    if (dragging) {
                        settle()
                        brain.release(now)
                    } else if (!longPressed && e.actionMasked == MotionEvent.ACTION_UP) {
                        brain.tap(now)
                        view?.start()
                    }
                }
            }
            return true
        }
    }

    /** After a drag: keep it on screen and snap to a side edge if dropped close to it. */
    private fun settle() {
        val b = bounds()
        var x = b.clampX(params.x.toFloat())
        val snap = 28 * b.density
        if (x < snap) x = 0f else if (x > b.maxX - snap) x = b.maxX
        moveTo(x, b.clampY(params.y.toFloat()))
        savePosition()
    }

    // ---- screen state ----

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val v = view ?: return
            val now = SystemClock.uptimeMillis()
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> v.stop()
                Intent.ACTION_USER_PRESENT -> { v.start(); brain.greet(now) }
                Intent.ACTION_SCREEN_ON -> {
                    val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                    if (!km.isKeyguardLocked) { v.start(); brain.greet(now) }
                }
            }
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(this, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    // ---- foreground ----

    private fun enterForeground(name: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "桌面陪伴", NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) })
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, OverlayService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_paw)
            .setContentTitle(if (name.isNotBlank()) "${name}在桌面陪着你" else "桌宠在桌面陪着你")
            .setContentText("点它互动 · 按住拖动 · 长按回到 App")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(open)
            .addAction(0, "收起", stop)
            .build()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, n, type)
    }

    companion object {
        const val ACTION_STOP = "io.github.fyy0619cell.deskpet.STOP_OVERLAY"
        private const val CHANNEL = "overlay"
        private const val NOTIFICATION_ID = 7
        private const val SIZE_DP = 120
        private const val TOP_DP = 28
        private const val KEY_X = "x"
        private const val KEY_Y = "y"

        private val _running = MutableStateFlow(false)
        val running: StateFlow<Boolean> = _running

        fun start(context: Context) =
            ContextCompat.startForegroundService(context, Intent(context, OverlayService::class.java))

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }

        /** Forward an in-app action to the desktop pet; no-op when it is not on screen. */
        fun send(context: Context, action: String) {
            if (_running.value) context.startService(Intent(context, OverlayService::class.java).setAction(action))
        }

        const val ACTION_FEED = "io.github.fyy0619cell.deskpet.FEED"
        const val ACTION_CLEAN = "io.github.fyy0619cell.deskpet.CLEAN"
        const val ACTION_SETTINGS = "io.github.fyy0619cell.deskpet.SETTINGS"
    }
}
