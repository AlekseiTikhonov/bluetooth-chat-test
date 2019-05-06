package com.example.aatik.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class chat extends MainActivity  {
    TextView status;
    Button send;
    EditText writeMsg;
    SendReceive sendReceive;
    ListView receivedList, sentList;

    ArrayList<String> receivedMessages;
    ArrayList<String> sentMessages;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        status = (TextView) findViewById(R.id.status);
        send = (Button) findViewById(R.id.send);
        writeMsg = (EditText) findViewById(R.id.writemsg);
        receivedList = (ListView) findViewById(R.id.receivedList);
        sentList = (ListView) findViewById(R.id.sentList);

        receivedMessages = new ArrayList();
        sentMessages = new ArrayList();

        status.setText("Status: connected to " + deviceName);
        sendReceive=new SendReceive(socket);
        sendReceive.start();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string= String.valueOf(writeMsg.getText());

                int i = 0, recCount = 0, sentCount = 0;
                recCount = receivedList.getCount();
                sentCount = sentList.getCount();

                if (recCount > sentCount){
                    i = recCount;
                } else {
                    i = sentCount;
                }

                for (int j = sentCount; j < i; j++){
                    sentMessages.add("");
                }

                sentMessages.add(i,string);
                sentList.setAdapter(new ArrayAdapter(chat.this, R.layout.text_right, sentMessages) );

                sendReceive.write(string.getBytes());
                writeMsg.getText().clear();

            }
        });

    }

    Handler handler1 = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case STATE_MESSAGE_RECEIVER:
                           byte[] readBuff= (byte[]) msg.obj;
                           String tempMsg = new String(readBuff,0,msg.arg1);

                        int i = 0, recCount = 0, sentCount = 0;
                        recCount = receivedList.getCount();
                        sentCount = sentList.getCount();

                        if (recCount > sentCount){
                            i = recCount;
                        } else {
                            i = sentCount;
                        }

                        for (int j = recCount; j < i; j++){
                            receivedMessages.add("");
                        }

                         receivedMessages.add(i,tempMsg);

                         receivedList.setAdapter(new ArrayAdapter(chat.this,R.layout.text_left, receivedMessages) );

                    break;
            }
            return true;
        }
    });



    public class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIN = null;
            OutputStream tempOUT = null;
            try {
                tempIN = bluetoothSocket.getInputStream();
                tempOUT = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIN;
            outputStream = tempOUT;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler1.obtainMessage(STATE_MESSAGE_RECEIVER,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}