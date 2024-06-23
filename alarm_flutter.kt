package kt.tiesco.flutter_alarme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

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

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "showAlarms" -> showAlarms(result)
            "createAlarm" -> {
                val hour = call.argument<Int>("hour")
                val minutes = call.argument<Int>("minutes")
                val title = call.argument<String>("title")
                val skipUi = call.argument<Boolean>("skipUi") ?: true
                if (hour != null && minutes != null) {
                    createAlarm(hour, minutes, title, skipUi, result)
                } else {
                    result.error("INVALID_ARGUMENTS", "Hora e minutos devem ser fornecidos", null)
                }
            }
            "showTimers" -> showTimers(result)
            "createTimer" -> {
                val length = call.argument<Int>("length")
                val title = call.argument<String>("title")
                val skipUi = call.argument<Boolean>("skipUi") ?: true
                if (length != null) {
                    createTimer(length, title, skipUi, result)
                } else {
                    result.error("INVALID_ARGUMENTS", "O valor deve ser fornecido", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    private fun createAlarm(hour: Int, minutes: Int, title: String?, skipUi: Boolean, result: Result) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            }
            activity.startActivity(intent)
            result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar alarme: ${e.message}")
            result.error("CREATE_ALARM_ERROR", e.message, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun createTimer(length: Int, title: String?, skipUi: Boolean, result: Result) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, length)
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            }
            activity.startActivity(intent)
            result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar timer: ${e.message}")
            result.error("CREATE_TIMER_ERROR", e.message, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun showAlarms(result: Result) {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            activity.startActivity(intent)
            result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mostrar alarmes: ${e.message}")
            result.error("SHOW_ALARMS_ERROR", e.message, null)
        }
    }

    private fun showTimers(result: Result) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(AlarmClock.ACTION_SHOW_TIMERS)
                activity.startActivity(intent)
                result.success(null)
            } else {
                result.error("UNSUPPORTED_VERSION", "Versão do Android não suportada", null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mostrar timers: ${e.message}")
            result.error("SHOW_TIMERS_ERROR", e.message, null)
        }
    }
}
