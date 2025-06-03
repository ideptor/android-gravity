package org.ideptor.freedrop


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Activity
import android.hardware.*
import android.util.Log
//import android.os.Bundle
import android.widget.TextView
import org.ideptor.freedrop.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.log


class MainActivity : AppCompatActivity() , SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelZ = 0f
    private var velocity = 0f
    private var lastTime: Long = 0

    private var isFreeFalling = false
    private val FREE_FALL_THRESHOLD = 2.0f  // m/s^2 이하로 판단

    private var altitude = 0.0f

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAG = "MainAcitvity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // 센서 등록
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartLine1.setOnClickListener {
            Log.i(TAG, "START LINE 1")
            binding.tvLog.text = ""
        }
        binding.btnStartLine1.setOnClickListener {
            Log.i(TAG, "START LINE 8")
            binding.tvLog.text = ""
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_PRESSURE -> handlePressure(event)
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()

        val rawZ = event.values[2]
        accelZ = rawZ - SensorManager.GRAVITY_EARTH  // 중력 보정

        if (abs(rawZ) < FREE_FALL_THRESHOLD) {
            isFreeFalling = true
        } else {
            isFreeFalling = false
            velocity = 0f  // 낙하 중이 아니면 속도 초기화
        }

        if (lastTime != 0L && isFreeFalling) {
            val dt = (currentTime - lastTime) / 1000.0f
            velocity += accelZ * dt
        }

        lastTime = currentTime

        updateUI()
    }

    private fun handlePressure(event: SensorEvent) {
        val pressure = event.values[0] // hPa
        altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
    }

    private fun updateUI() {
        val status = if (isFreeFalling) "Falling" else "normal"
        val text = """
            status:$status, Speed(m/s):${"%.3f".format(velocity)}, Height(m):${"%.3f".format(altitude)}
        """.trimIndent()
//        textView.text = text
        if(isFreeFalling) {
            Log.i(TAG, text)
            binding.tvLog.append("$text\n")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}