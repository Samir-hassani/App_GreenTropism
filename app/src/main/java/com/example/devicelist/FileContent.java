package com.example.devicelist;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
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
import java.net.URLConnection;
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

    private static final String TAG = "FileContentActivity";
    public static String file_name ;
    public static String path;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    ViewPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_file_content);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager)findViewById(R.id.pager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcones();

                // to make text view Scrollable we have to caste textview in a ScrollView in .xml
                // Récupérer le nom du fichier selectionné
        Intent new_int = getIntent();
        file_name = new_int.getStringExtra(Liste_spectres.EXTRA_ADDRESS);

                 // file path
        File csv = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GreenTropism/NeoSpectraMicro");
        path = csv.getAbsolutePath()+ "/";

                // set title of the activity
        setTitle(file_name);
    }
            // Titre de chaque fragment
    private void setupViewPager (ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GrapheFragment(),"GRAPHE");
        adapter.addFragment(new CsvFragment(),"FICHIER .CSV");
        viewPager.setAdapter(adapter);
    }

            // Icone pour chaque fragment
    private void setupTabIcones(){
        tabLayout.getTabAt(0).setIcon(R.drawable.plot);
        tabLayout.getTabAt(1).setIcon(R.drawable.csv_file);

    }

            //Retour pour la flèche back sur l action bare  et partage fichier
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case(android.R.id.home):
                finish();
            break;
            case(R.id.share_button):        // Boutton Partager
                File fileToSend = new File(path+file_name);         // Chemin absolu du fichier
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");                           // Type fichier .csv
                sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(FileContent.this,BuildConfig.APPLICATION_ID + ".provider",fileToSend));
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, file_name);
                startActivity(Intent.createChooser(sharingIntent, "Partager par :"));
            break;

        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


}
