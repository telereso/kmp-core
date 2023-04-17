package io.telereso.kmp.core.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.telereso.kmp.core.*
import io.telereso.kmp.core.Log.logDebug
import io.telereso.kmp.core.Utils.launchPeriodicAsync
import io.telereso.kmp.core.app.databinding.ActivityMainBinding
import io.telereso.kmp.core.models.JwtPayload
import kotlinx.coroutines.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SampleViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SampleViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logDebug(getPlatform().userAgent)

        val handler = CoroutineExceptionHandler { _, e ->
            e.printStackTrace()
            println(e.message)
        }

        TasksExamples.testVerify(CoreClient(application))
//        lifecycleScope.launch(handler) {
//
//            runTest()
//
//            throw Throwable("test 2")
//        }

//        val settings = Settings.get(4.toDuration(DurationUnit.SECONDS))
//        settings.putExpirableString(
//            "test", "eeee", (System.currentTimeMillis() / 1000) + 10
//        )
//
//        lifecycleScope.launchPeriodicAsync(3.toDuration(DurationUnit.SECONDS)){
//            logDebug(settings.getExpirableString("test") ?: "NA")
//        }

        testCancel()
    }

    fun testCancel() {
        val task = TasksExamples.hiDelayed().onSuccess {
            logDebug(it)
        }.onFailure {
            logDebug(it.message ?: "")
        }.onCancel {
            logDebug(it.message ?: "")
        }

        Handler().postDelayed({
            task.cancel("test cancel")
        },2000)


    }

    suspend fun runTest() {
        withContext(Dispatchers.IO) {
            testError().await()
        }
    }

    suspend fun runTestAwaitOrNull(exceptionHandler: CoroutineExceptionHandler) {
        withContext(Dispatchers.IO) {
            testError().onFailure { e ->
                exceptionHandler.handleException(this.coroutineContext, e)
            }.awaitOrNull()
        }
    }

    fun testLog(): Task<JwtPayload> {
        return Task.execute {
            JwtPayload("sfsdf")
        }
    }

    fun testError(): Task<String> {
        return Task.execute {
            if (true)
                throw Throwable("test")
            else "hi"
        }
    }
}
