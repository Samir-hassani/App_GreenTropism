package com.example.devicelist;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;


public class Liste_spectres extends AppCompatActivity {

    TextView savedSpectres;
    public Context context;
    ListView listViewFiles;
    public static String EXTRA_ADDRESS = "name_file";
    String path;                // = "/data/data/com.example.devicelist/files/";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_spectres);

        setTitle("Précédent");

        savedSpectres = (TextView) findViewById(R.id.textViewSpectres);
        listViewFiles = (ListView) findViewById(R.id.listeViewFiles);

        registerForContextMenu(listViewFiles);

        File csv = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GreenTropism/NeoSpectraMicro");
        path = csv.getAbsolutePath()+ "/";

            // Get files name of directory
        File directory = new File(path);
        File[] files = directory.listFiles();
        String[] namefiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            namefiles[i] = files[i].getName();
        }
                // listeView of the files in directory
        ListAdapter listeFiles = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, namefiles);
        listViewFiles.setAdapter(listeFiles);
        listViewFiles.setOnItemClickListener(getMyFileCSV);


    }

    public AdapterView.OnItemClickListener getMyFileCSV = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // detecte name of item selected
            String fileSelected = ((TextView) view).getText().toString();
                // Make an intent to start next activity.
            Intent myintent = new Intent(Liste_spectres.this,FileContent.class);
                //Change the activity.
            myintent.putExtra(EXTRA_ADDRESS, fileSelected);        //this will be received at FileContent (class) Activity
            startActivityForResult(myintent,0);
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Option : ");

        if (v.getId()==R.id.listeViewFiles) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_file_csv, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // Le nom du fichier selectionné
        String key = ((TextView) info.targetView).getText().toString();
        if(item.getItemId() == R.id.supprimer){
            int index = info.position;

                // Suppression du fichier selectionné
           File fdelete = new File(path+ key);
           boolean deleted = fdelete.delete();
            if(deleted){
            Toast.makeText(getApplicationContext(),"Supprimé " ,Toast.LENGTH_SHORT).show();}
            else {
                Toast.makeText(getApplicationContext(),"Pas supprimé !" ,Toast.LENGTH_SHORT).show();
            }
            return true;
        }else if(item.getItemId() == R.id.informations){
            Toast.makeText(getApplicationContext(),"Nom du fichier sélectionné :\n"+key ,Toast.LENGTH_LONG).show();
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

}
