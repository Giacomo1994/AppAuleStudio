package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

public class IscrizioneActivity extends AppCompatActivity {

    final static String URL_INFOGRUPPIFROMCODICE = "http://pmsc9.altervista.org/progetto/info_gruppo_from_codice.php";
    final static String URL_ISCRIZIONE_GRUPPO= "http://pmsc9.altervista.org/progetto/iscrizione_al_gruppo.php";

    EditText codice_gruppo;
    TextView output;
    Button annulla_dialog, conferma_dialog, iscriviti;
    ImageView close_dialog;
    String str_codice_gruppo, str_nome_gruppo, nomeProf, cognomeProf, nomeCorso;
    String strUniversita, strNomeUniversita, strMatricola, strNome, strCognome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iscrizione);
        iscriviti = findViewById(R.id.iscriviti);
        codice_gruppo = findViewById(R.id.codice_gruppo);
        output= findViewById(R.id.output);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strNomeUniversita=settings.getString("nome_universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        action_bar();

        iscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                str_codice_gruppo=codice_gruppo.getText().toString().trim();
                if(str_codice_gruppo.equals("")){
                    MyToast.makeText(getApplicationContext(),"Inserisci un codice!",false).show();
                    return;
                }
                new get_info_dialog().execute();
            }
        });

    }

    @SuppressLint("WrongConstant")
    public void action_bar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar =view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText("Iscrizione a gruppo");
        final Dialog d = new Dialog(IscrizioneActivity.this);
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_user);
        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        TextView txt_nome=d.findViewById(R.id.txt_dialog_user_nome);
        txt_nome.setText(strNome+" "+strCognome);
        TextView txt_matricola=d.findViewById(R.id.txt_dialog_user_matricola);
        txt_matricola.setText(strMatricola);
        TextView txt_universita=d.findViewById(R.id.txt_dialog_user_università);
        txt_universita.setText(strNomeUniversita);
        Button btn_logout=d.findViewById(R.id.btn_logout);
        Button btn_continue=d.findViewById(R.id.btn_continue);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("logged", false);
                editor.commit();
                Intent i = new Intent(IscrizioneActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        image_actionbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.show();
            }
        });
    }

    private class get_info_dialog extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
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
                jArrayInfo = new JSONArray(result);
                if(jArrayInfo.length()==0) return "Gruppo inesistente!";

                for (int i = 0; i < jArrayInfo.length(); i++) {
                    JSONObject json_data = jArrayInfo.getJSONObject(i);
                    nomeProf=json_data.getString("nome");
                    cognomeProf=json_data.getString("cognome");
                    nomeCorso=json_data.getString("nome_corso");
                    str_nome_gruppo=json_data.getString("nome_gruppo");
                }
                return "OK";
            } catch (Exception e) {
                return null;
            }

        }
        @Override
        protected void onPostExecute(String result) {
            if(result==null){
                MyToast.makeText(getApplicationContext(),"Impossibile contattare il server!",false).show();
                return;
            }
            if(result.equals("Gruppo inesistente!")){
                MyToast.makeText(getApplicationContext(),result,false).show();
                return;
            }

            final Dialog d = new Dialog(IscrizioneActivity.this);
            d.setTitle("Conferma iscrizione");
            d.setCancelable(false);
            d.setContentView(R.layout.dialog_conferma_iscrizione);
            d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
            TextView txt_gruppo= d.findViewById(R.id.txt_iscr_gruppo);
            TextView txt_docente=d.findViewById(R.id.txt_iscr_docente);
            TextView txt_corso=d.findViewById(R.id.txt_iscr_corso);
            close_dialog = d.findViewById(R.id.close_dialog);
            annulla_dialog = d.findViewById(R.id.annulla_dialog);
            conferma_dialog = d.findViewById(R.id.conferma_dialog);
            txt_gruppo.setText(str_nome_gruppo);
            txt_docente.setText(nomeProf+" "+cognomeProf);
            txt_corso.setText(nomeCorso);
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
                    "&codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8");

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
            return "Impossibile contattare il server!";
        } finally {}
    }
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Iscrizione effettuata con successo!")==false){
                MyToast.makeText(getApplicationContext(),"Ops, qualcosa è andato storto: "+result,false).show();
                return;
            }
            else{
                MyToast.makeText(getApplicationContext(),result,true).show();
                Intent i = new Intent(IscrizioneActivity.this, GroupActivity.class);
                startActivity(i);
                finish();
            }
        }
    }


}


