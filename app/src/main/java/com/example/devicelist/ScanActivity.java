package com.example.devicelist;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.opencsv.CSVWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

public class ScanActivity extends AppCompatActivity {

    String adressBT = null;
    Button buttonDisconnect ;
    Button btnScan ;
    Button btnBack ;
    Button btnID ;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket;
    private boolean isBtConnected = false;
    private boolean isRunBack = true;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public byte[] buffer = new byte[1024];
    public String strTT = new String();
    public int bytes ;
    InputStream tmpIn = null;
    public  boolean recv = true;
    String dataString = null;
    String[] parts ;

    private LineChartView lineChartView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        buttonDisconnect = (Button)findViewById(R.id.btnDisconnect);
        btnScan = (Button)findViewById(R.id.btnScan);
        btnBack = (Button)findViewById(R.id.btnBack);
        btnID = (Button)findViewById(R.id.btnID);
        lineChartView = (LineChartView)findViewById(R.id.chart);
        lineChartView.setVisibility(View.INVISIBLE);

        Intent newint = getIntent();
        adressBT = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);


        /*******  Call CLASS to connect ******/
        new ConnectBT().execute();


        /****** check for BLE ***********/
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){

            msg("No BLE bluetooth");
            finish();
            return;
        };

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRunBack) {
                    try {

                        btSocket.getOutputStream().write("5".toString().getBytes());

                        // Reception des donnees envoyees par le Kit NeoSpectra

                        tmpIn = btSocket.getInputStream();
                        DataInputStream mmInStream = new DataInputStream(tmpIn);

                        for (int i=0; i<71; i++){
                            bytes = mmInStream.read(buffer,0,buffer.length);
                            dataString = new String(buffer, 0, bytes);
                            strTT = strTT + dataString;

                            if(dataString.length() < 990){
                                recv = false;
                            }
                        }

                        strTT = strTT.substring(1,(strTT.length()-1));
                        parts = strTT.split(",");

                        // Extraction de waveLength a partir du paquet de donnees reçu ==> 4096 premiers elements
                        String[] wavLengthStr = new String[4096];
                        for (int i=0; i<4096; i++){
                            if(i==4095){
                                parts[i] = parts[i].substring(0,(parts[i].length()-1)); // suppression du crochet apres le dernier element de waeLength ==> "2600.0]"
                            }
                            wavLengthStr[i] = parts[i];
                        }

                        // Extraction de absorbance a partir du paquet de donnees reçu ==> à partir du 4096 ème élements
                        String[] absorbanceStr = new String[4096];
                        int j = 0;
                        for (int i=4096; i<8192; i++) {
                            if (i == 4096) {
                                parts[i] = parts[i].substring(1);   // suppression du crochet du premier element de absorbance ==> "[00...."
                            }
                            absorbanceStr[j] = parts[i];
                            j++;
                        }

                        float[] waveLength = new float[4096];
                        float[] absorbance = new float[4096];
                        for(int i=0; i<absorbance.length; i++) {
                            waveLength[i] = Float.valueOf(wavLengthStr[i]);     // Conversion des valeurs de waveLength de (str) en ==> "Float"
                            absorbance[i] = Float.valueOf(absorbanceStr[i]);    // Conversion des valeurs de absorbance de (str) en ==> "Float"
                        }
                        float max = absorbance[0];
                        for(int i=0; i<absorbance.length; i++) {
                            if(absorbance[i]> max){                     // calcul du max des valeurs de absorbances pour adapter le plot
                                max = absorbance[i];
                            }
                        }
                        lineChartView.setVisibility(View.VISIBLE);

                        String[] xAxisData = wavLengthStr;
                        float[] yAxisData = absorbance;

                        List yAxisValues = new ArrayList();
                        List axisValues = new ArrayList();

                        Line line = new Line(yAxisValues).setColor(Color.parseColor("#050586"));

                        for (int i = 0; i < xAxisData.length; i++) {
                            axisValues.add(i, new AxisValue(i).setLabel(xAxisData[i]));
                            yAxisValues.add(new PointValue(i, yAxisData[i]));
                        }

                        List lines = new ArrayList();
                        lines.add(line);
                        LineChartData data = new LineChartData();
                        data.setLines(lines);
                        line.setHasPoints(false);   // marquer chaque point par un cercle
                        line.setStrokeWidth(1);     //Epaisseur de la ligne du graphe

                        Axis xAxis = new Axis();
                        xAxis.setValues(axisValues);
                        xAxis.setName("WaveLength");
                        xAxis.setTextSize(10);
                        xAxis.setTextColor(Color.parseColor("#5A5858"));
                        data.setAxisXBottom(xAxis);
                        xAxis.setHasTiltedLabels(true);     //Auto generate values

                        Axis yAxis = new Axis();
                        yAxis.setName("Absorbance");
                        yAxis.setTextColor(Color.parseColor("#5A5858"));
                        yAxis.setTextSize(10);
                        data.setAxisYLeft(yAxis);
                        yAxis.setHasTiltedLabels(true);     //Auto generate values

                        lineChartView.setLineChartData(data);
                        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());

                        // Créer fichier.csv

                        // Check time to name file
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                        Date date = new Date();
                        final String fileName ="Spectre_" +dateFormat.format(date)+ ".csv";

                        // Creation d un repertoire dans stockage interne et écriture du fichier .csv
                        File csv = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GreenTropism/NeoSpectraMicro");
                        String csvPath = csv.getAbsolutePath()+"/" + fileName ;
                        CSVWriter writer = new CSVWriter(new FileWriter(csvPath));
                        writer.writeNext(wavLengthStr);
                        writer.writeNext(absorbanceStr);
                        writer.flush();
                        writer.close();

                    } catch (IOException e) {
                        msg("Error btnScan ! " + e);
                    }
                }else {
                    msg("Please, runBackground First !");
                }
            }
        });

        btnID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btSocket.getOutputStream().write("1".toString().getBytes());
                    tmpIn = btSocket.getInputStream();
                    DataInputStream mmInStream = new DataInputStream(tmpIn);
                    bytes = mmInStream.read(buffer,0,buffer.length);

                    String dataString = new String(buffer, 0, bytes);
                    Toast.makeText(getApplicationContext(),dataString.substring(2,9),Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    msg("Error btnID ! "+ e);

                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btSocket.getOutputStream().write("4".toString().getBytes());
                    isRunBack = true;

                } catch (IOException e) {
                    msg("Error btnBack !"+ e);
                }
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });

    }
    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.liste_spectres){
            Intent intent = new Intent(this, Liste_spectres.class);
            this.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /********** CONNEXION *********/

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ScanActivity.this, "Connecting...", "Please wait ...");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {

                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice deviceRemote = myBluetooth.getRemoteDevice(adressBT);//connects to the device's address and checks if it's available

                    btSocket = deviceRemote.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection

                }
            }
            catch (IOException e)
            {

                ConnectSuccess = false;   //if the try failed, you can check the exception here

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected");
                isBtConnected = true;
            }
            progress.dismiss();

        }
    }

    /******* Déconnexion *******/
    private void Disconnect()
    {
        if (btSocket!=null) //  si btSocket occupé
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            { msg("Error");}
        }

        finish(); //return to the first layout

    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
}
