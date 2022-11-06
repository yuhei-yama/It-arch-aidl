package com.example.it_arch_aidl

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.RemoteException
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.lang.ref.WeakReference

private const val BUMP_MSG = 1
class MainActivity {

    private var mService: IRemoteService? = null

    /** Another interface we use on the service.  */
    internal var secondaryService: ISecondary? = null

    private lateinit var killButton: Button
    private lateinit var callbackText: TextView
    private lateinit var handler: InternalHandler

    private var isBound: Boolean = false

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {

            mService = IRemoteService.Stub.asInterface(service)
            killButton.isEnabled = true
            callbackText.text = "Attached."

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mService?.registerCallback(mCallback)
            } catch (e: RemoteException) {

            }

            Toast.makeText(
                this@Binding,
                R.string.remote_service_connected,
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onServiceDisconnected(className: ComponentName) {

            mService = null
            killButton.isEnabled = false
            callbackText.text = "Disconnected."


            Toast.makeText(
                this@Binding,
                R.string.remote_service_disconnected,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val secondaryConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Connecting to a secondary interface is the same as any
            // other interface.
            secondaryService = ISecondary.Stub.asInterface(service)
            killButton.isEnabled = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            secondaryService = null
            killButton.isEnabled = false
        }
    }

    private val mBindListener = View.OnClickListener {

        val intent = Intent(this@Binding, RemoteService::class.java)
        intent.action = IRemoteService::class.java.name
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        intent.action = ISecondary::class.java.name
        bindService(intent, secondaryConnection, Context.BIND_AUTO_CREATE)
        isBound = true
        callbackText.text = "Binding."
    }

    private val unbindListener = View.OnClickListener {
        if (isBound) {

            try {
                mService?.unregisterCallback(mCallback)
            } catch (e: RemoteException) {

            }

            // Detach our existing connection.
            unbindService(mConnection)
            unbindService(secondaryConnection)
            killButton.isEnabled = false
            isBound = false
            callbackText.text = "Unbinding."
        }
    }

    private val killListener = View.OnClickListener {

        try {
            secondaryService?.pid?.also { pid ->

                Process.killProcess(pid)
                callbackText.text = "Killed service process."
            }
        } catch (ex: RemoteException) {

            Toast.makeText(this@Binding, R.string.remote_call_failed, Toast.LENGTH_SHORT).show()
        }
    }


    private val mCallback = object : IRemoteServiceCallback.Stub() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.remote_service_binding)

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
}