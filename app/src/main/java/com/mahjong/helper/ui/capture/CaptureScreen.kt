package com.mahjong.helper.ui.capture

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahjong.helper.capture.CaptureService

@Composable
fun CaptureScreen() {
    val context = LocalContext.current

    var isCapturing by remember { mutableStateOf(false) }
    var service by remember { mutableStateOf<CaptureService?>(null) }
    var analysisResult by remember { mutableStateOf<CaptureService.AnalysisResult?>(null) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val b = binder as CaptureService.LocalBinder
                service = b.getService()
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }
        }
    }

    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val intent = Intent(context, CaptureService::class.java)
            context.startForegroundService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            // Small delay to let service bind before calling startCapture
            android.os.Handler(context.mainLooper).postDelayed({
                service?.startCapture(result.resultCode, result.data!!)
            }, 300)
            isCapturing = true
        }
    }

    LaunchedEffect(service) {
        service?.analysisResult?.collect { result ->
            analysisResult = result
        }
    }

    fun startCapture() {
        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    fun stopCapture() {
        service?.stopCapture()
        context.stopService(Intent(context, CaptureService::class.java))
        context.unbindService(connection)
        isCapturing = false
        service = null
        analysisResult = null
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isCapturing) {
                try { context.unbindService(connection) } catch (_: Exception) {}
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isCapturing) "🔴" else "📱",
            fontSize = 48.sp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            if (isCapturing) "监控中..." else "屏幕实时分析",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))
        Text(
            if (isCapturing) "正在实时分析牌面，建议将显示在悬浮窗"
            else "授权屏幕录制后，自动识别牌面并给出出牌建议",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        if (isCapturing) {
            analysisResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("当前建议", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "建议打出: ${result.suggestedTile}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "安全: ${result.safetyScore} | " +
                            "进张: ${result.acceptanceCount} | " +
                            "综合: ${result.combinedScore}分",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (analysisResult == null) {
                Text(
                    "等待识别牌面...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = { stopCapture() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("停止监控")
            }
        } else {
            Button(
                onClick = { startCapture() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("开始屏幕监控")
            }

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("使用说明", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "1. 打开微信\"决胜麻将\"小程序\n" +
                        "2. 回到本应用，点击\"开始屏幕监控\"\n" +
                        "3. 授权屏幕录制权限\n" +
                        "4. 回到小程序继续打牌\n" +
                        "5. 悬浮窗会实时显示出牌建议",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
