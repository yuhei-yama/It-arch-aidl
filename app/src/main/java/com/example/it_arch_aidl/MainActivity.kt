package com.example.it_arch_aidl

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.it_arch_aidl.databinding.ActivityMainBinding
import java.lang.ref.WeakReference


private const val BUMP_MSG = 1

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var mService: Secondary? = null

    /** Another interface we use on the service.  */
    internal var secondaryService: Secondary? = null

    private lateinit var killButton: Button
    private lateinit var callbackText: TextView
    private lateinit var handler: InternalHandler

    private var isBound: Boolean = false

    companion object{
        private const val NIGHT_THEME = "night"

    }

    fun getNightTheme(context: Context): Boolean {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(NIGHT_THEME, false)
    }

    fun putNightTheme(context: Context, value: Boolean) {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(NIGHT_THEME, value)
            .apply()
    }

    fun setDefaultNightMode(isChecked: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isChecked) {

                AppCompatDelegate.MODE_NIGHT_YES
            } else {

                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //color変更ボタン
        val fab : Button = findViewById(R.id.fab)
        fab.setOnClickListener(changeListener)

        // Watch for button clicks.
        var button: Button = findViewById(R.id.bind)
        button.setOnClickListener(mBindListener)
        button = findViewById(R.id.unbind)
        button.setOnClickListener(unbindListener)
        killButton = findViewById(R.id.kill)
        killButton.setOnClickListener(killListener)
        killButton.isEnabled = false

        callbackText = findViewById(R.id.callback)
        callbackText.text = "Not attached."
        handler = InternalHandler(callbackText)
    }

    var isChecked = MainActivity().getNightTheme(this@MainActivity)

    val changeListener = object : View.OnClickListener{
        override fun onClick(p0: View?){
            putNightTheme(this@MainActivity, true)
            setDefaultNightMode(true)
        }
    }



    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {

            mService = Secondary.Stub.asInterface(service)
            killButton.isEnabled = true
            callbackText.text = "Attached."
            Log.d(TAG, "サービスと接続しました")

            // We want to monitor the service for as long as we are
            // connected to it.
//            try {
//                mService?.registerCallback(mCallback)
//            } catch (e: RemoteException) {
//
//            }
            Toast.makeText(
                this@MainActivity,
                R.string.remote_service_connected,
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onServiceDisconnected(className: ComponentName) {

            mService = null
            killButton.isEnabled = false
            callbackText.text = "Disconnected."
            Log.d(TAG, "サービスと切断が完了しました")

            Toast.makeText(
                this@MainActivity,
                R.string.remote_service_disconnected,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val mBindListener = View.OnClickListener {

        val intent = Intent(this@MainActivity, RemoteService::class.java)
        intent.action = AiRemoteService::class.java.name
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        Log.d(TAG, "バインド")
        var serveAnswer = mService?.served("hello. remote_service").toString()
        Log.d(TAG, serveAnswer)

        isBound = true
        callbackText.text = "Binding."
    }

    private val unbindListener = View.OnClickListener {
        if (isBound) {

//            try {
//                mService?.unregisterCallback(mCallback)
//            } catch (e: RemoteException) {
//
//            }

//            // Detach our existing connection.
            unbindService(mConnection)

            killButton.isEnabled = false
            isBound = false
            callbackText.text = "Unbinding."
            Log.d(TAG, "アンバインド")
        }
    }

    private val killListener = View.OnClickListener {
        try {
            secondaryService?.pid?.also { pid ->

                android.os.Process.killProcess(pid)
                callbackText.text = "Killed service process."
            }
        } catch (ex: RemoteException) {

            Toast.makeText(this@MainActivity, R.string.remote_call_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private val mCallback = object : RemoteServiceCallback.Stub() {
        override fun valueChanged(value: Int) {
            handler.sendMessage(handler.obtainMessage(BUMP_MSG, value, 0))
        }
    }

    private class InternalHandler(
        textView: TextView,
        private val weakTextView: WeakReference<TextView> = WeakReference(textView)
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BUMP_MSG -> weakTextView.get()?.text = "Received from service: ${msg.arg1}"
                else -> super.handleMessage(msg)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }


}