package com.example.devicelist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    View view;
    ImageButton image_button, fleche_liste ;
    TextView  listBT;
    TextView  textviewBT;
    BluetoothAdapter myBluetooth;
    ListView pairedBT;
    Set<BluetoothDevice> devices;
    public static String EXTRA_ADDRESS = "device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            // change background color
        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.white);

        listBT = (TextView) findViewById(R.id.textView);
        textviewBT = (TextView) findViewById(R.id.textViewBT);
        pairedBT = (ListView)findViewById(R.id.listView);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        devices = myBluetooth.getBondedDevices();
        image_button = (ImageButton) findViewById(R.id.image_button);
        fleche_liste = (ImageButton) findViewById(R.id.fleche_liste);


        if(devices.isEmpty()){
            Toast.makeText(getApplicationContext(), "No paired Device !", Toast.LENGTH_LONG).show();};


            // changer l image bluetooth selon son état
        if(myBluetooth.isEnabled()){
            image_button.setImageResource(R.drawable.bt_active);
            textviewBT.setText("Bluetooth activé");
        }else {
            image_button.setImageResource(R.drawable.bt_desactive);
            textviewBT.setText("Bluetooth désactivé ");
        }

        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices = myBluetooth.getBondedDevices();

                if (myBluetooth == null) {
                    Toast.makeText(getApplicationContext(), "No device bluetooth", Toast.LENGTH_LONG).show();
                    finish();
                }
                if (myBluetooth.isEnabled())
                {
                    myBluetooth.disable();
                    Toast.makeText(getApplicationContext(), "Bluetooth disabled", Toast.LENGTH_SHORT).show();
                    image_button.setImageResource(R.drawable.bt_desactive);
                    textviewBT.setText("Bluetooth désactivé");
                }
                else if(!myBluetooth.isEnabled()){
                    Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(turnBTon);
                    image_button.setImageResource(R.drawable.bt_active);
                    textviewBT.setText("Bluetooth activé");}
            }
        });

            // liste des peripheriques connectes
        fleche_liste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!myBluetooth.isEnabled()){
                    Toast.makeText(getApplicationContext(), "Please Activate Bluetooth !", Toast.LENGTH_LONG).show();
                }
                else if(!pairedBT.isShown()){
                    devices = myBluetooth.getBondedDevices();
                    getPairedList();
                    pairedBT.setVisibility(View.VISIBLE);       // show listeView
                    fleche_liste.setImageResource(R.drawable.fleche_liste_rev);
                }
                else if(pairedBT.isShown()){
                    pairedBT.setVisibility(View.GONE);      // hide listeView
                    fleche_liste.setImageResource(R.drawable.fleche_liste);
                }
                }

            });
    }

    private void getPairedList(){
        ArrayList pairlist = new ArrayList();
        if (devices.size()>0){
            for (BluetoothDevice bt : devices){
                pairlist.add(bt.getName() + " , " + bt.getAddress());
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"No device in listView",Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, pairlist);
        pairedBT.setAdapter(adapter);
        pairedBT.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
                     // Get the device MAC address, the last 17 chars in the View
          String name = ((TextView) v).getText().toString();
          String address = name.substring(name.length() - 17);

                    // Make an intent to start next activity.
            Intent myintent = new Intent(MainActivity.this,ScanActivity.class);

                    //Change the activity.
          myintent.putExtra(EXTRA_ADDRESS, address);        //this will be received at ScanActivity (class)
          startActivityForResult(myintent,0);
        }
    };

}
