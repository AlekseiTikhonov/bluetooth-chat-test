package com.example.aatik.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity  {

    Button listDevices,listen;
    TextView status;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Intent btEnablingIntent;
    public static BluetoothSocket socket;
    public static String deviceName;

    int REQUEST_ENABLE_BLUETOOTH=1;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVER = 5;

    public static final String APP_NAME = "BTChat";
    public static final UUID MY_UUID = UUID.fromString("8ea1208d-ac72-47f6-985a-62828280325a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = (TextView) findViewById(R.id.status);
        listDevices = (Button) findViewById(R.id.listDevices);
        listen = (Button) findViewById(R.id.listen);


        if(!bluetoothAdapter.isEnabled())
        {
            btEnablingIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnablingIntent, REQUEST_ENABLE_BLUETOOTH);
        }
        implementListeners();
    }

    private void implementListeners() {
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(MainActivity.this, list.class);
                    startActivity(intent);
                } else { Toast.makeText(getApplicationContext(), "Bluetooth is not enabled", Toast.LENGTH_SHORT).show(); }

            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isEnabled()) {
                    ServerClass serverClass = new ServerClass();
                    serverClass.start();
                } else { Toast.makeText(getApplicationContext(), "Bluetooth is not enabled", Toast.LENGTH_SHORT).show(); }
            }
        });

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case STATE_CONNECTING:
                    status.setText("Status: connecting to the server");
                    break;
                case STATE_LISTENING:
                    status.setText("Status: waiting for incoming connections");
                    break;
                case STATE_CONNECTED:
                    status.setText("Status: connected");
                    Intent i = new Intent(MainActivity.this, chat.class);
                    startActivity(i);
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Status: connection failed");
                    break;
            }
            return true;
        }
    });


        private class ServerClass extends Thread {
            private BluetoothServerSocket serverSocket;

            public ServerClass() {
                try {
                    serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void run() {
                socket = null;

                while (socket == null) {
                    try {
                        Message message = Message.obtain();
                        message.what = STATE_LISTENING;
                        handler.sendMessage(message);
                        socket = serverSocket.accept();
                        deviceName = socket.getRemoteDevice().getName();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Message message = Message.obtain();
                        message.what = STATE_CONNECTION_FAILED;
                        handler.sendMessage(message);
                    }
                    if (socket != null) {
                        Message message = Message.obtain();
                        message.what = STATE_CONNECTED;
                        handler.sendMessage(message);
                        break;
                    }
                }
            }
        }

}
