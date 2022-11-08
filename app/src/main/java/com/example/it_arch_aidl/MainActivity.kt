package com.example.it_arch_aidl

import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

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

//    private val secondaryConnection = object : ServiceConnection {
//
//        override fun onServiceConnected(className: ComponentName, service: IBinder) {
//            // Connecting to a secondary interface is the same as any
//            // other interface.
//            secondaryService = Secondary.Stub.asInterface(service)
//            killButton.isEnabled = true
//        }
//
//        override fun onServiceDisconnected(className: ComponentName) {
//            secondaryService = null
//            killButton.isEnabled = false
//        }
//    }

    private val mBindListener = View.OnClickListener {

        val intent = Intent(this@MainActivity, RemoteService::class.java)
        intent.action = AiRemoteService::class.java.name
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        Log.d(TAG, "バインド")

//        intent.action = Secondary::class.java.name
//        bindService(intent, secondaryConnection, Context.BIND_AUTO_CREATE)
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
//            unbindService(secondaryConnection)
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

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}