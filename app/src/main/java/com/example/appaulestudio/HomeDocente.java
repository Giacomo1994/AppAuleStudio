package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class HomeDocente extends AppCompatActivity {


    String strUniversita, strMatricola, strPassword, strNome, strCognome;
    static final String URL_RICHIEDICORSIFROMDOCENTE = "http://pmsc9.altervista.org/progetto/richiedi_corsi_from_docente.php";
    ArrayAdapter adapter;
    TextView textHomeDocente;
    TextView infoCorso ;
    ListView elencoCorsi;
    Button creaGruppi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_docente);
        initUI();

        new listaCorsi().execute();
    }

    private void initUI(){
        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strPassword=settings.getString("password", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);


        elencoCorsi=findViewById(R.id.elencoCorsi);
        setTitle(strNome+" "+strCognome);
       // Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>"+strMatricola+strUniversita+"</b></font>"), Toast.LENGTH_LONG).show();
        creaGruppi= findViewById(R.id.btnCreaCodici);
        creaGruppi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(HomeDocente.this, CreaCodici.class);
                intent.putExtra("nome",strNome);
                intent.putExtra("cognome",strCognome);
                intent.putExtra("matricola", strMatricola);
                intent.putExtra("universita", strUniversita);
                startActivity(intent);
            }
        });


    }

    //devo creare una classe che estende asynctask per riempire la listview dei corsi di ogni docente
    private class listaCorsi extends AsyncTask<Void, Void, Corso[]>{


        @Override
        protected Corso[] doInBackground(Void... voids) {
            //qua devo ottenere un array di corsi che ottengo scaricandolo dal db remoto
            //creo un URL con il quale chiamo un file php che interrogherà il db remoto

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


                url = new URL(URL_RICHIEDICORSIFROMDOCENTE); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);


                //devo impostare i parametri, devo passare la matricola del docente e il codice dell'uni
                //creo una stringa del tipo nome-valore, sono quelli dei parametri del codice post (li passo alla pagina php)
                params = "matricola_docente="+URLEncoder.encode(strMatricola, "UTF-8")
                        +"&codice_universita="+URLEncoder.encode(strUniversita, "UTF-8");

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


                //faccio un ciclo for per tutti gli elementi all'interno dell'array json che sono corsi
                //per ogni corso mi prendo le relative informazioni relative ad esso e mi creo un array di oggetti corso
                //che poi metterò nella listview
                Corso[] array_corso = new Corso[jArrayCorsi.length()];

                for(int i = 0; i<jArrayCorsi.length(); i++){
                    JSONObject json_data = jArrayCorsi.getJSONObject(i);
                    array_corso[i] = new Corso(json_data.getString("codice_corso"),
                                               json_data.getString("nome_corso"),
                                               json_data.getString("codice_universita"),
                                               json_data.getString("matricola_docente"));
                }

                return array_corso;
            }  catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Corso[] array_corso) {
            //qua devo riempire la listview con i corsi scaricati prima e messi nell'array di corsi
            //controllo che l'array sia stato riempito
            if(array_corso==null){//prendo i dati da sql locale perchè non riesco ad accedere ai dati in remoto
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile contattare il server: i dati potrebbero essere non aggiornati</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }

            //devo creare l'adapter per mettere nella listview gli elementi dell'array

            Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>"+array_corso.length+"</b></font>"), Toast.LENGTH_LONG).show();
            adapter = new ArrayAdapter<Corso>(HomeDocente.this, R.layout.row_layout_home_docente, array_corso ){
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
        menu.add(Menu.FIRST, 2, Menu.FIRST + 1, "Home");
        menu.add(Menu.FIRST, 3, Menu.FIRST + 2, "Crea codici");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("universita", null);
            editor.putString("nome_universita", null);
            editor.putString("email", null);
            editor.putString("email_calendar", null);
            editor.putString("matricola", null);
            editor.putString("password", null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.putString("last_update", null);
            editor.commit();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, 100);
            finish();
        }
        if (item.getItemId() == 2) {
            Intent i = new Intent(this, HomeDocente.class);
            startActivityForResult(i, 150);
            finish();
        }
        if(item.getItemId()==3){
            Intent i = new Intent(this, CreaCodici.class);
            startActivityForResult(i,300);
            finish();
        }
        return true;
    }




}
