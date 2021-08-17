package io.ackeecz.sample.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import io.ackeecz.sample.App
import io.ackeecz.sample.R
import io.ackeecz.sample.detail.DetailActivity
import io.ackeecz.sample.model.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val viewModel: LoginViewModel by lazy {
        App.diContainer.create(LoginViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch {
            viewModel.viewState.consumeEach { state ->
                when (state) {
                    is State.Loaded -> openDetail()
                    is State.Error -> showError(state.error)
                }

            }
        }

        val btn = findViewById<Button>(R.id.btn_login)
        val editName = findViewById<EditText>(R.id.edit_email)
        val editPass = findViewById<EditText>(R.id.edit_pass)

        btn.setOnClickListener {
            viewModel.login(editName.text.toString(), editPass.text.toString())
        }
    }

    private fun openDetail() {
        startActivity(Intent(this, DetailActivity::class.java))
    }

    private fun showError(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.complete()
    }
}
