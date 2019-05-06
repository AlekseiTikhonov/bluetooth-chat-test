package com.example.aatik.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;


public class list extends MainActivity {
    BluetoothDevice[] btArray;
    ListView listView1;
    static TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialoglist);

        listView1=(ListView) findViewById(R.id.test);
        status = (TextView) findViewById(R.id.status);

        getDevices();
    }

    public void getDevices() {
                Set<BluetoothDevice> bt=bluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index=0;
                if (bt.size()>0)
                {
                    for (BluetoothDevice device : bt)
                    {
                        btArray[index]= device;
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strings);
                    listView1.setAdapter(arrayAdapter);
                }
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                ClientClass clientClass= new ClientClass(btArray[i]);
                clientClass.start();
                status.setText("Status: connecting");
            }
        });
            }



    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case STATE_CONNECTING:
                    status.setText("Status: connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Status: connected to "+deviceName);
                    Intent i = new Intent(list.this, chat.class);
                    startActivity(i);
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Status: connection failed");
                    break;
            }
            return true;
        }
    });

    public class ClientClass extends Thread
    {
        private BluetoothDevice device;

        public ClientClass (BluetoothDevice device1)
        {
            device = device1;
            try {
                socket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run()
        {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what=STATE_CONNECTED;
                deviceName = socket.getRemoteDevice().getName();
                handler.sendMessage(message);

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }
}

