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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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


public class GroupActivity extends AppCompatActivity {

/*
new AlertDialog.Builder(this)
.setTitle("Title")
.setMessage("Do you really want to whatever?")
.setIcon(android.R.drawable.ic_dialog_alert)
.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int whichButton) {
        Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
    }})
 .setNegativeButton(android.R.string.no, null).show();


 */


    String strUniversita, strMatricola, strPassword, strNome, strCognome;

    ListView corsiPerStudente;

    static final String URL_RICHIEDIGRUPPIFROMSTUDENTE="http://pmsc9.altervista.org/progetto/richiedi_gruppi_from_iscrizione.php";
    ArrayAdapter adapter;
    TextView codGrup, oreDisp, nomeGrup;


    private void initUI(){
        corsiPerStudente = findViewById(R.id.listaGruppi);
        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strPassword=settings.getString("password", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        setTitle(""+strNome+" "+strMatricola);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        this.initUI();
        new listaGruppi().execute();
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
                params = "matricola="+ URLEncoder.encode(strMatricola, "UTF-8");


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
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile contattare il server: i dati potrebbero essere non aggiornati</b></font>"), Toast.LENGTH_LONG).show();
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
            corsiPerStudente.setAdapter(adapter);
        }



    }

    public void iscrizione_gruppo(View v){
        Intent i = new Intent(GroupActivity.this, IscrizioneActivity.class);
        startActivity(i);
    }

}
