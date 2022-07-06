package com.example.composeapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.composeapp.databinding.ActivityMainBinding
import com.example.composeapp.model.AccessPoint
import com.example.composeapp.ui.theme.ComposeAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var wifiManager: WifiManager

    val scannedApFlow = MutableLiveData(listOf<AccessPoint>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        requestPermission()
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess(wifiManager)
                }
            }
        }

        IntentFilter().run {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            registerReceiver(wifiScanReceiver, this)
        }
    }

    fun startScanning() {
        wifiManager.startScan()
    }

    private fun scanSuccess(wifiManager: WifiManager) {
        val results = wifiManager.scanResults
        results.map {
            AccessPoint(
                name = it.SSID,
                uid = it.BSSID,
                rssi = it.level,
            )
        }.let {
            scannedApFlow.value = it
        }

    }

    private fun requestPermission() {
        if (checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

}


suspend fun doSomething(): String {
    delay(3000)
    return listOf("1,2", "3,4,5,7").flatMap {
        it.split(",")
    }.toString()
}

fun TextView.setClickableSpan(
    content: String,
    start: Int,
    end: Int,
    underline: Boolean = false,
    color: Int = android.graphics.Color.BLUE,
    sizeScale: Float = 1f,
    style: Int? = null,
    clickHandler: (View) -> Unit = {},
) {
    val spannableString = SpannableString(content).apply {
        setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                clickHandler.invoke(p0)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.apply {
                    isUnderlineText = underline
                    this.color = color
                    textSize *= sizeScale
                    style?.let {
                        apply(this, it)
                    }
                }
            }

            @SuppressLint("WrongConstant")
            private fun apply(paint: Paint, style: Int) {
                val old: Typeface? = paint.typeface
                val oldStyle = old?.style ?: 0
                val want = oldStyle or style
                val tf = if (old == null) {
                    Typeface.defaultFromStyle(want)
                } else {
                    Typeface.create(old, want)
                }

                val fake = want and tf.style.inv()
                if (fake and Typeface.BOLD != 0) {
                    paint.isFakeBoldText = true
                }
                if (fake and Typeface.ITALIC != 0) {
                    paint.textSkewX = -0.25f
                }
                paint.typeface = tf
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    movementMethod = LinkMovementMethod.getInstance()
    text = spannableString
}

@Composable
fun Player() {
    var knobPercent by remember {
        mutableStateOf(0f)
    }
    val barCount by remember {
        mutableStateOf(20)
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        MusicKnob(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
            knobPercent = it
        }

        VolumeBar(
            modifier = Modifier
                .padding(5.dp)
                .height(50.dp),
            activeBar = (knobPercent * barCount).roundToInt(),
            barCount = barCount,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MusicKnob(
    modifier: Modifier = Modifier,
    limitingAngle: Float = 25f,
    onValueChange: (Float) -> Unit,
) {
    var rotation by remember {
        mutableStateOf(limitingAngle)
    }
    var touchX by remember {
        mutableStateOf(0f)
    }
    var touchY by remember {
        mutableStateOf(0f)
    }
    var centerX by remember {
        mutableStateOf(0f)
    }
    var centerY by remember {
        mutableStateOf(0f)
    }
    Image(
        painter = painterResource(id = R.drawable.knob),
        contentDescription = "Music knob",
        modifier = modifier
            .onGloballyPositioned {
                it
                    .boundsInWindow()
                    .let { window ->
                        centerX = window.size.width / 2
                        centerY = window.size.height / 2
                    }
            }
            .pointerInteropFilter
            { event ->
                touchX = event.x
                touchY = event.y
                val angle = -atan2(centerX - touchX, centerY - touchY) * (180f / PI).toFloat()
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        if (angle !in -limitingAngle..limitingAngle) {
                            val fixedAngle = if (angle in -180f..-limitingAngle) {
                                360f + angle
                            } else {
                                angle
                            }
                            rotation = fixedAngle
                            val percent = (fixedAngle - limitingAngle) / (360f - 2 * limitingAngle)
                            onValueChange(percent)
                            true
                        } else false
                    }
                    else -> false
                }
            }
            .rotate(rotation)
    )
}

@Composable
fun VolumeBar(
    modifier: Modifier = Modifier,
    activeBar: Int = 0,
    barCount: Int = 10,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        val barWidth = remember {
            constraints.maxWidth / (2f * barCount)
        }
        Canvas(modifier = modifier) {
            for (i in 0 until barCount) {
                drawRoundRect(
                    color = if (i in 0..activeBar) Color.Green else Color.DarkGray,
                    topLeft = Offset(i * barWidth * 2f + barWidth / 2, 0f),
                    size = Size(barWidth, constraints.maxHeight.toFloat()),
                    cornerRadius = CornerRadius(2f)
                )
            }
        }
    }
}

@Composable
fun SideEffectTry() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    Scaffold(scaffoldState = scaffoldState) {
        var counter = produceState(initialValue = 0) {
            delay(3000)
            value = 4
        }
        if (counter.value % 5 == 0) {
            LaunchedEffect(key1 = scaffoldState.snackbarHostState) {
                scaffoldState.snackbarHostState.showSnackbar("Hello")
            }
        }
        Button(onClick = { }) {
            Text(text = "Click me: ${counter.value}")
        }
    }
}

@Composable
fun InputBox() {
    var name by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(text = "Enter something")
                }
            )
            Button(onClick = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Hi $name")
                }
            }) {
                Text(text = "Say hello!")
            }
        }
    }

}

fun toast(context: Context, content: String) {
    Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
}

@Composable
fun Greeting(name: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .background(Color.Green),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(
            text = "Hello $name!",
            modifier = Modifier
                .draggable(
                    state = DraggableState { },
                    orientation = Orientation.Horizontal
                )
                .clickable {
                    Toast
                        .makeText(context, "I am clickable", Toast.LENGTH_SHORT)
                        .show()
                }
        )
        Text(text = "Hi there")

    }
}

@Composable
fun ColorBox() {
    val color = remember {
        mutableStateOf(Color.Black)
    }
    Box(
        modifier = Modifier
            .background(color = color.value)
            .clickable {
                color.value = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat(),
                )
            }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeAppTheme {
        Greeting("Android")
    }
}