package com.controller.listener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;


//SUPER UGLY CODE but this will have to do given the time
public class MainActivity extends Activity {

    private final String serviceName = "Listener";
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothServerSocket btServerSocket;
    private InputStream inputStream;

    private TextView commandsReceivedTextView;

    private Activity context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        commandsReceivedTextView = (TextView) findViewById(R.id.commands_received_text_view);
        commandsReceivedTextView.setMovementMethod(new ScrollingMovementMethod());

        try {
            btServerSocket = btAdapter().listenUsingRfcommWithServiceRecord(serviceName, uuid);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BluetoothSocket btSocket = btServerSocket.accept();
                        System.out.println("Name of connector" + btSocket.getRemoteDevice().getName());
                        inputStream = btSocket.getInputStream();
                        readLine();
                    } catch (IOException e) {
                        System.out.println("Unable to accept connections " + e);
                    }
                }
            });
            t.start();
        } catch (IOException e) {
            System.out.println("Unable to listen " + e);
        }
    }

    private void readLine() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Reading lines...");
                    final int command = inputStream.read();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            commandsReceivedTextView.setText(commandsReceivedTextView.getText() + "\n" + command);
                            //got from http://stackoverflow.com/questions/3506696/auto-scrolling-textview-in-android-to-bring-text-into-view
                            final Layout layout = commandsReceivedTextView.getLayout();
                            if(layout != null){
                                int scrollDelta = layout.getLineBottom(commandsReceivedTextView.getLineCount() - 1)
                                        - commandsReceivedTextView.getScrollY() - commandsReceivedTextView.getHeight();
                                if(scrollDelta > 0)
                                    commandsReceivedTextView.scrollBy(0, scrollDelta);
                            }
                            readLine();
                        }
                    });

                } catch (IOException e) {
                    System.out.println("Unable to read lines " + e);
                }
            }
        });
        t.start();
    }

    private BluetoothAdapter btAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }
}
