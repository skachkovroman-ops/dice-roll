package com.example.diceroller

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DiceRollerScreen()
            }
        }
    }
}

@Composable
fun DiceRollerScreen() {
    var currentDiceValue by remember { mutableIntStateOf(1) }
    var isRolling by remember { mutableBooleanStateOf(false) }
    var rotationX by remember { mutableFloatStateOf(0f) }
    var rotationY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    val animatedScale by animateFloatAsState(
        targetValue = if (isRolling) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "diceScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2C)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Бросок Кубика",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(animatedScale)
                .graphicsLayer {
                    this.rotationX = rotationX
                    this.rotationY = rotationY
                    cameraDistance = 12 * density
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = getDiceDrawable(currentDiceValue)),
                contentDescription = "Dice $currentDiceValue",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {
                if (!isRolling) {
                    isRolling = true
                    scope.launch {
                        val durationMs = 5000L // 5 секунд анимация
                        val startTime = System.currentTimeMillis()
                        
                        // Запуск звукового сопровождения
                        launch { playDiceRollSound(durationMs) }

                        while (System.currentTimeMillis() - startTime < durationMs) {
                            currentDiceValue = Random.nextInt(1, 7)
                            rotationX += Random.nextInt(15, 45)
                            rotationY += Random.nextInt(15, 45)
                            delay(80) // Скорость перебора граней
                        }

                        // Финальный результат
                        currentDiceValue = Random.nextInt(1, 7)
                        rotationX = 0f
                        rotationY = 0f
                        isRolling = false
                    }
                }
            },
            enabled = !isRolling,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF5722),
                disabledContainerColor = Color(0x88FF5722)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .height(56.dp)
                .width(220.dp)
        ) {
            Text(
                text = if (isRolling) "Бросаем..." else "Бросить кубик",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getDiceDrawable(value: Int): Int {
    return when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
}

suspend fun playDiceRollSound(durationMs: Long) {
    val sampleRate = 44100
    val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(minBufferSize)
        .build()

    audioTrack.play()

    val startTime = System.currentTimeMillis()
    var interval = 80L
    
    while (System.currentTimeMillis() - startTime < durationMs) {
        val clickDuration = 0.02
        val numSamples = (sampleRate * clickDuration).toInt()
        val sample = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i * 600.0 / sampleRate
            val valShort = (sin(angle) * 32767 * (1.0 - i.toDouble() / numSamples)).toInt().toShort()
            sample[i * 2] = (valShort.toInt() and 0x00ff).toByte()
            sample[i * 2 + 1] = (valShort.toInt() shr 8).toByte()
        }

        audioTrack.write(sample, 0, sample.size)
        delay(interval)
        interval += 3
    }

    audioTrack.stop()
    audioTrack.release()
}
