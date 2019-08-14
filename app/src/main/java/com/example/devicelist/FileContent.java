package com.example.devicelist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class FileContent extends AppCompatActivity {

    Button showData;
    String file_name ;
    TextView file_content;
    String[] wavelengthStr;
    String[] absorbanceStr;
    LineChartView lineChartViewFileCsv ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_file_content);


        showData = (Button)findViewById(R.id.showData);
        file_content = (TextView)findViewById(R.id.textViewContent);
        file_content.setVisibility(View.INVISIBLE);
        lineChartViewFileCsv = (LineChartView)findViewById(R.id.chart);
        lineChartViewFileCsv.setVisibility(View.INVISIBLE);

                // to make text view Scrollable we have to caste textview in a ScrollView in .xml
                // Recuperer le nom du fichier selectionné
        Intent new_int = getIntent();
        file_name = new_int.getStringExtra(Liste_spectres.EXTRA_ADDRESS);

                // file path
        File csv = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GreenTropism/NeoSpectraMicro");
        String path = csv.getAbsolutePath()+ "/";


                // set title of the activity
        setTitle(file_name);

                // Read content of file selected and make it in a textView
       // FileInputStream is;
       // BufferedReader reader;
        final File file = new File(path + file_name);

        if (file.exists()) {
            try {
                FileInputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line1 = reader.readLine();
                wavelengthStr = line1.split(",");
                file_content.append("WaveLength : ");
                file_content.append("\n"+line1);

                String line2 = reader.readLine();
                absorbanceStr = line2.split(",");
                file_content.append("\nAbsorbance : ");
                file_content.append("\n"+line2);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getApplicationContext(),"File nto found ",Toast.LENGTH_SHORT).show();
        }

        float[] wavelength = new float[4096];
        float[] absorbance = new float[4096];
        for(int i=0; i<absorbance.length; i++) {
            wavelengthStr[i] = wavelengthStr[i].substring(1,wavelengthStr[i].length()-1);
            absorbanceStr[i] = absorbanceStr[i].substring(1,absorbanceStr[i].length()-1);
            wavelength[i] = Float.valueOf(wavelengthStr[i]);     // Conversion des valeurs de waveLength de (str) en ==> "Float"
            absorbance[i] = Float.valueOf(absorbanceStr[i]);    // Conversion des valeurs de absorbance de (str) en ==> "Float"
        }

                // Plot
        String[] xAxisData = wavelengthStr;
        float[] yAxisData = absorbance;

        List yAxisValues = new ArrayList();
        List xAxisValues = new ArrayList();

        Line line = new Line(yAxisValues).setColor(Color.parseColor("#050586"));

        for (int i = 0; i < xAxisData.length; i++) {
            xAxisValues.add(i, new AxisValue(i).setLabel(xAxisData[i]));
            yAxisValues.add(new PointValue(i, yAxisData[i]));
        }

        lineChartViewFileCsv.setVisibility(View.VISIBLE);
        List lines = new ArrayList();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        line.setHasPoints(false);   // marquer chaque point par un cercle
        line.setStrokeWidth(1);     // Epaisseur du la ligne du graphe

        Axis xAxis = new Axis();
        xAxis.setValues(xAxisValues);
        xAxis.setName("WaveLength");
        xAxis.setTextSize(10);
        xAxis.setTextColor(Color.parseColor("#5A5858"));
        data.setAxisXBottom(xAxis);
        xAxis.setHasTiltedLabels(true);     //Auto generate values

        Axis yAxis = new Axis();
        yAxis.setName("Absorbance");
        yAxis.setTextSize(10);
        yAxis.setTextColor(Color.parseColor("#5A5858"));
        data.setAxisYLeft(yAxis);
        yAxis.setHasTiltedLabels(true);     //Auto generate values

        lineChartViewFileCsv.setLineChartData(data);


                // bouton pour naviguer entre plot et data
        showData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lineChartViewFileCsv.isShown()) {
                    lineChartViewFileCsv.setVisibility(View.INVISIBLE);
                    file_content.setVisibility(View.VISIBLE);
                    showData.setText("Show Plot");
                }else{
                    lineChartViewFileCsv.setVisibility(View.VISIBLE);
                    file_content.setVisibility(View.INVISIBLE);
                    showData.setText("Show Data");
                }

            }
        });
    }
            //Retour pour la fleche back sur l action bare
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

}
