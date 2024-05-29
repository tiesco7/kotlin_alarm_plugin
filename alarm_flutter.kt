package kt.tiesco.flutter_alarme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.provider.AlarmClock.ACTION_SHOW_ALARMS
import android.provider.AlarmClock.ACTION_SHOW_TIMERS
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

class FlutterAlarmClockPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private val TAG = "FlutterAlarmClockPlugin"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_alarm_clock")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "showAlarms" -> showAlarms()
            "createAlarm" -> {
                val hour = call.argument<Int>("hour")
                val minutes = call.argument<Int>("minutes")
                val title = call.argument<String>("title")
                val skipUi = call.argument<Boolean>("skipUi") ?: true
                if (hour != null && minutes != null) {
                    createAlarm(hour, minutes, title, skipUi)
                } else {
                    Log.e(TAG, "Hora e minutos devem ser fornecidos")
                }
            }
            "showTimers" -> showTimers()
            "createTimer" -> {
                val length = call.argument<Int>("length")
                val title = call.argument<String>("title")
                val skipUi = call.argument<Boolean>("skipUi") ?: true
                if (length != null) {
                    createTimer(length, title, skipUi)
                } else {
                    Log.e(TAG, "O valor deve ser fornecido")
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {}
    override fun onDetachedFromActivity() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
    
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {}

    private fun createAlarm(hour: Int, minutes: Int, title: String? = "", skipUi: Boolean = true) {
        try {
            val i = Intent(AlarmClock.ACTION_SET_ALARM)
            i.putExtra(AlarmClock.EXTRA_HOUR, hour)
            i.putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            i.putExtra(AlarmClock.EXTRA_MESSAGE, title)
            // 
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            activity.startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar alarme: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun createTimer(length: Int, title: String? = "", skipUi: Boolean = true) {
        try {
            val i = Intent(AlarmClock.ACTION_SET_TIMER)
            i.putExtra(AlarmClock.EXTRA_LENGTH, length)
            i.putExtra(AlarmClock.EXTRA_MESSAGE, title)
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            // Cria alarme do sistema, sem mostrar a UI do sistema
            activity.startActivity(i)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar timer: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun showAlarms() {
        val i = Intent(ACTION_SHOW_ALARMS)
        // Abre a tela de alarmes do sistema Android
        activity.startActivity(i)
    }

    private fun showTimers() {
        val i = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Abre a tela de Timer do sistema Android
            Intent(ACTION_SHOW_TIMERS)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        activity.startActivity(i)
    }
}
