package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Arrays;

public class IscrizioneActivity extends AppCompatActivity {

    final static String URL_INFOGRUPPIFROMCODICE = "http://pmsc9.altervista.org/progetto/info_gruppo_from_codice.php";
    final static String URL_ISCRIZIONE_GRUPPO= "http://pmsc9.altervista.org/progetto/iscrizione_al_gruppo.php";

    EditText codice_gruppo;
    Button iscriviti;
    String str_codice_gruppo;
   // ArrayAdapter adapter;
    TextView text_dialog, infoCodice;
    TextView output;
    //String[] array_info;
    String nomeProf, cognomeProf, nomeCorso;
    Button annulla_dialog;

    Button  conferma_dialog;
    String strUniversita, strMatricola, strPassword, strNome, strCognome;



    ImageView close_dialog;

    public void initUI() {

        iscriviti = findViewById(R.id.iscriviti);
        codice_gruppo = findViewById(R.id.codice_gruppo);
        //array_info = new String[3];
        nomeProf=""; cognomeProf=""; nomeCorso="";
        output= findViewById(R.id.output);
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
        setContentView(R.layout.activity_iscrizione);
        this.initUI();

        iscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                str_codice_gruppo=codice_gruppo.getText().toString().trim();
                //output.setText(str_codice_gruppo);
                new get_info_dialog().execute();
                //output.setText(nomeProf+nomeCorso);

                //output.append(nomeProf+" "+cognomeProf+" "+nomeCorso);
                /*
                *//*text_dialog.setText("Ti stai iscrivendo al gruppo/n"+"Corso: "+nomeCorso+
                        "/nProfessore: "+nomeProf+" "+cognomeProf);*//*
                d.show();*/

            }
        });

    }




    private class get_info_dialog extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


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
                JSONArray jArrayInfo;


                url = new URL(URL_INFOGRUPPIFROMCODICE); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);


                //devo impostare i parametri, devo passare la matricola del docente e il codice dell'uni
                //creo una stringa del tipo nome-valore, sono quelli dei parametri del codice post (li passo alla pagina php)
                params = "codice_gruppo=" + URLEncoder.encode(str_codice_gruppo, "UTF-8");


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
                jArrayInfo = new JSONArray(result);  //questa decodifica crea un array di elementi json


                //faccio un ciclo for per tutti gli elementi all'interno dell'array json che sono corsi
                //per ogni corso mi prendo le relative informazioni relative ad esso e mi creo un array di oggetti corso
                //che poi metterò nella listview
                //array_info = new String[jArrayInfo.length()];

                for (int i = 0; i < jArrayInfo.length(); i++) {
                    JSONObject json_data = jArrayInfo.getJSONObject(i);
                   /* array_info[i] = (json_data.getString("nome") +
                            json_data.getString("cognome") +
                            json_data.getString("nome_corso"));*/
                   //array_info[i]=json_data[i];
                    nomeProf=json_data.getString("nome");
                    cognomeProf=json_data.getString("cognome");
                    nomeCorso=json_data.getString("nome_corso");
                }

                //return array_info;
                return null;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final Dialog d = new Dialog(IscrizioneActivity.this);
            d.setTitle("Conferma iscrizione");

            d.setCancelable(false);

            d.setContentView(R.layout.dialog_conferma_iscrizione);

            d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);


            text_dialog= d.findViewById(R.id.text_dialog);
            close_dialog = d.findViewById(R.id.close_dialog);
            annulla_dialog = d.findViewById(R.id.annulla_dialog);
            conferma_dialog = d.findViewById(R.id.conferma_dialog);

            text_dialog.setText("Ti stai iscrivendo al gruppo per il corso di "+nomeCorso+
                    " tenuto dal professor "+nomeProf+" "+cognomeProf);
            d.show();
            close_dialog.setOnClickListener(new ImageView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    d.cancel();
                }
            });
            annulla_dialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    d.cancel();
                }
            });
            conferma_dialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new aggiungi_iscrizione().execute();
                }
            });

        }
    }

    private class aggiungi_iscrizione extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... strings) {
              try {
            URL url;
            url = new URL(URL_ISCRIZIONE_GRUPPO);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(1000);
            urlConnection.setConnectTimeout(1500);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            String parametri = "codice_gruppo=" + URLEncoder.encode(str_codice_gruppo, "UTF-8") +
                    "&matricola=" + URLEncoder.encode(strMatricola, "UTF-8")+
                    "&codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8");;

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
            if(result.equals("Iscrizione effettuata con successo!")==false){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#e00700' ><b> Ops, qualcosa è andato storto"+result+"</b></font>"),Toast.LENGTH_LONG).show();
                return;
            }
            else{
                //torno all'activity precedente
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#e00700' ><b>"+result+" </b></font>"),Toast.LENGTH_LONG).show();
                Intent i = new Intent(IscrizioneActivity.this, GroupActivity.class);
                startActivity(i);
                finish();

            }

        }
    }


}


