package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class GroupActivity extends AppCompatActivity {
    String strUniversita, strMatricola, strPassword, strNome, strCognome;
    String strCodiceGruppo;
    Gruppo g;

    ListView gruppiPerStudente;

    static final String URL_ABBANDONA_GRUPPO= "http://pmsc9.altervista.org/progetto/abbandona_gruppo.php";
    static final String URL_RICHIEDIGRUPPIFROMSTUDENTE="http://pmsc9.altervista.org/progetto/richiedi_gruppi_from_iscrizione.php";
    ArrayAdapter adapter;
    TextView codGrup, oreDisp, nomeGrup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        gruppiPerStudente = findViewById(R.id.listaGruppi);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strPassword=settings.getString("password", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        setTitle(""+strNome+" "+strMatricola);

        new listaGruppi().execute();
        registerForContextMenu(gruppiPerStudente);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new listaGruppi().execute();
    }

    //MENU CONTESTUALE
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView list=(ListView) v;
        g=(Gruppo) list.getItemAtPosition(info.position);
        strCodiceGruppo = g.getCodice_gruppo();
        menu.add(Menu.FIRST,1,Menu.FIRST+1,"Abbandona gruppo");
        menu.add(Menu.FIRST,2,Menu.FIRST,"Dettagli gruppo");
    }
    //ESCO DAL GRUPPO -> DEVO CANCELLARE LA MIA ISCRIZIONE DATA LA MIA MATRICOLA E OL CODICE DEL GRUPPO
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            new abbandonaGruppo().execute();
        }
        return true;
    }



    //Creo task asincrono

    private class listaGruppi extends AsyncTask<Void, Void, Gruppo[]>{

        @Override
        protected Gruppo[] doInBackground(Void... voids) {

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
                JSONArray jArrayGruppi;


                url = new URL(URL_RICHIEDIGRUPPIFROMSTUDENTE); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);


                //devo impostare i parametri, devo passare la matricola del docente e il codice dell'uni
                //creo una stringa del tipo nome-valore, sono quelli dei parametri del codice post (li passo alla pagina php)
                params = "matricola="+ URLEncoder.encode(strMatricola, "UTF-8") + "&codice_universita="+ URLEncoder.encode(strUniversita, "UTF-8");


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
                jArrayGruppi = new JSONArray(result);  //questa decodifica crea un array di elementi json


                //faccio un ciclo for per tutti gli elementi all'interno dell'array json che sono corsi
                //per ogni corso mi prendo le relative informazioni relative ad esso e mi creo un array di oggetti corso
                //che poi metterò nella listview
                Gruppo[] array_gruppo = new Gruppo[jArrayGruppi.length()];

                for(int i = 0; i<jArrayGruppi.length(); i++){
                    JSONObject json_data = jArrayGruppi.getJSONObject(i);
                    array_gruppo[i] = new Gruppo(json_data.getString("codice_gruppo"),
                            json_data.getString("nome_gruppo"),
                            json_data.getString("codice_corso"),
                            json_data.getString("matricola_docente"),
                            json_data.getInt("componenti_max"),
                            json_data.getInt("ore_disponibili"),
                            json_data.getString("data_scadenza"));
                }

                return array_gruppo;
            }  catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Gruppo[] array_gruppo) {
            //qua devo riempire la listview con i corsi scaricati prima e messi nell'array di gruppi
            //controllo che l'array sia stato riempito
            if(array_gruppo==null){//prendo i dati da sql locale perchè non riesco ad accedere ai dati in remoto
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Problema di connessione, i dati potrebbero non essere aggionrati</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }

            //devo creare l'adapter per mettere nella listview gli elementi dell'array

            Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>"+array_gruppo.length+"</b></font>"), Toast.LENGTH_LONG).show();
            adapter = new ArrayAdapter<Gruppo>(GroupActivity.this, R.layout.row_layout_group_activity, array_gruppo ){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Gruppo item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_group_activity, parent, false);
                    codGrup = convertView.findViewById(R.id.codGrup);
                    oreDisp = convertView.findViewById(R.id.oreDisp);
                    nomeGrup = convertView.findViewById(R.id.nomeGrup);
                    codGrup.setText("Codice gruppo: "+item.getCodice_gruppo());
                    oreDisp.setText("Ore disponibili: "+item.getOre_disponibili());
                    nomeGrup.setText(""+item.getNome_gruppo());

                    return convertView;
                }
            };
            gruppiPerStudente.setAdapter(adapter);
        }



    }

    public void iscrizione_gruppo(View v){
        Intent i = new Intent(GroupActivity.this, IscrizioneActivity.class);
        startActivity(i);
    }


    //task asincrono per cancellare una riga dalla tabella iscrizione ovvero abbandonare un gruppo
    private class abbandonaGruppo extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... strings) {
            try {
                URL url;
                url = new URL(URL_ABBANDONA_GRUPPO);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "matricola=" + URLEncoder.encode(strMatricola, "UTF-8") +
                                   "&codice_gruppo=" + URLEncoder.encode(strCodiceGruppo, "UTF-8");

                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
                dos.flush();
                dos.close();
                //leggo stringa di ritorno da file php
                urlConnection.connect();
                InputStream input = urlConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int numRead = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((numRead = input.read(buffer)) != -1) {
                    baos.write(buffer, 0, numRead);
                }
                input.close();
                String stringaRicevuta = new String(baos.toByteArray());
                return stringaRicevuta;
            } catch (Exception e) {
                Log.e("SimpleHttpURLConnection", e.getMessage());
                return "Impossibile connettersi";
            } finally {
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Hai abbandonato il gruppo")==false){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#e00700' ><b> Ops, qualcosa è andato storto</b></font>"),Toast.LENGTH_LONG).show();
                return;
            }
            else{
                new listaGruppi().execute();
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#e00700' ><b>"+result+" </b></font>"),Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST+3, "Logout");
        menu.add(Menu.FIRST, 2, Menu.FIRST, "Home");
       // menu.add(Menu.FIRST, 3, Menu.FIRST+2, "Gestisci Gruppi");
        menu.add(Menu.FIRST, 4, Menu.FIRST+1, "Prenotazioni");
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
            editor.putString("matricola", null);
            editor.putString("nome", null);
            editor.putString("cognome", null);
            editor.putString("password", null);
            editor.putString("token", null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.putString("last_update", null);
            editor.commit();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        if (item.getItemId() == 2) {
            Intent i = new Intent(this, Home.class);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        /*if(item.getItemId() == 3){
            Intent i = new Intent(this, GroupActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }*/
        if(item.getItemId() == 4){
            Intent i = new Intent(this, PrenotazioniAttiveActivity.class);
            startActivity(i);
        }
        return true;
    }

}
