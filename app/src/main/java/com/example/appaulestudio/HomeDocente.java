package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeDocente extends AppCompatActivity {


    String strMatricola, strNome, strCognome, URL_CORSI;
    ArrayAdapter adapter;
    TextView infoCorso ;
    ListView elencoCorsi;
    Button creaGruppi;
    ArrayList<Corso> corsoArrayList=new ArrayList<Corso>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_docente);
        elencoCorsi=findViewById(R.id.elencoCorsi2);
        creaGruppi= findViewById(R.id.btnCreaCodici);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        URL_CORSI=settings.getString("url_corsi",null);
        setTitle(strNome+" "+strCognome);

        new listaCorsi().execute();

        elencoCorsi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              Corso c= (Corso) parent.getItemAtPosition(position);
                Intent intent=new Intent(HomeDocente.this,GestioneGruppiDocenteActivity.class);
                Bundle bundle=new Bundle();
                bundle.putParcelable("corso",c);
                intent.putExtra("bundle", bundle);
                startActivityForResult(intent,345);
            }
        });

        creaGruppi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(HomeDocente.this, CreaCodici.class);
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("corsi",corsoArrayList);
                intent.putExtra("bundle_corsi",bundle);
                startActivity(intent);
            }
        });

    }

// asynctask per riempire la listview dei corsi del docente
    private class listaCorsi extends AsyncTask<Void, Void, Corso[]>{
        @Override
        protected Corso[] doInBackground(Void... voids) {
            try {
                //definisco le variabili
                String params;
                URL url;
                HttpURLConnection urlConnection; //serve per aprire connessione
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArrayCorsi;
                url = new URL(URL_CORSI); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                params = "matricola_docente="+ URLEncoder.encode(strMatricola, "UTF-8");
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(params);
                dos.flush();
                dos.close();
                urlConnection.connect();
                is = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                sb = new StringBuilder();
                line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                jArrayCorsi = new JSONArray(result);  //questa decodifica crea un array di elementi json
                Corso[] array_corso = new Corso[jArrayCorsi.length()];

                for(int i = 0; i<jArrayCorsi.length(); i++){
                    JSONObject json_data = jArrayCorsi.getJSONObject(i);
                    array_corso[i] = new Corso(json_data.getString("codice_corso"), json_data.getString("nome_corso"));
                }

                return array_corso;
            }  catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Corso[] array_corso) {
            if(array_corso==null){//prendo i dati da sql locale perchè non riesco ad accedere ai dati in remoto
                MyToast.makeText(getApplicationContext(), "Impossibile contattare il server", false);
                creaGruppi.setEnabled(false);
                return;
            }
            if(array_corso.length==0){
                MyToast.makeText(getApplicationContext(), "Non possiedi nessun insegnamento", false);
                creaGruppi.setEnabled(false);
                return;
            }
            List<Corso> corsi_list= Arrays.asList(array_corso);
            corsoArrayList=new ArrayList<Corso>();
            corsoArrayList.addAll(corsi_list);
            adapter = new ArrayAdapter<Corso>(HomeDocente.this, R.layout.row_layout_home_docente, corsoArrayList ){
                @Override
                public View getView(int position,  View convertView,  ViewGroup parent) {
                    Corso item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_home_docente, parent, false);
                    infoCorso = convertView.findViewById(R.id.infoCorso);
                    infoCorso.setText(""+item.getCodiceCorso()+" - "+item.getNomeCorso());

                    return convertView;
                }
            };
            elencoCorsi.setAdapter(adapter);
        }
    }

    //creo menu in alto
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("universita", null);
            editor.putString("nome_universita", null);
            editor.putString("matricola", null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.commit();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, 100);
            finish();
        }
        return true;
    }

}
