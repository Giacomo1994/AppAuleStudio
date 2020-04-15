package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrazioneActivity extends AppCompatActivity {
    Spinner spinner;
    ArrayAdapter<Universita> adapter;
    TextView output;
    Button btn_registrazione;
    EditText txt_matricola;
    EditText txt_nome;
    EditText txt_cognome;
    EditText txt_email;
    EditText txt_password;
    RadioButton radioStudente;
    RadioButton radioDocente;


    Universita universita=null;
    String matricola;
    String nome;
    String cognome;
    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        spinner=findViewById(R.id.reg_universita);
        output=findViewById(R.id.output);
        btn_registrazione=findViewById(R.id.btn_registrazione);
        txt_matricola=this.findViewById(R.id.reg_matricola);
        txt_nome=this.findViewById(R.id.reg_nome);
        txt_cognome=this.findViewById(R.id.reg_cognome);
        txt_email=this.findViewById(R.id.reg_email);
        txt_password=this.findViewById(R.id.reg_password);
        radioStudente=findViewById(R.id.radioButton5);
        radioDocente=findViewById(R.id.radioButton6);

        //riempio spinner con universita
        new riempiUniversita().execute();

        //funzione di registrazione
        btn_registrazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matricola=txt_matricola.getText().toString().trim();
                nome=txt_nome.getText().toString().trim();
                cognome=txt_cognome.getText().toString().trim();
                email=txt_email.getText().toString().trim();
                password=txt_password.getText().toString().trim();

                //controllo campi non vuoti
                if(matricola.equals("")||nome.equals("")||cognome.equals("")||email.equals("")||password.equals("")){
                    output.setText("Devi inserire tutti i campi!");
                    return;
                }

                //controllo formato mail
                String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                        "[a-zA-Z0-9_+&*-]+)*@" +
                        "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                        "A-Z]{2,7}$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(email);
                boolean isValidMail=matcher.matches();
                if(isValidMail==false){
                    output.setText("Formato email non valido!");
                    return;
                }

                //controllo lunghezza password
                if(password.length()<8){
                    output.setText("Password troppo corta!");
                    return;
                }

                //registrazione utente
                if(radioStudente.isChecked()) new registraUtente().execute();
            }
        });
    }

    private class riempiUniversita extends AsyncTask<Void, Void, Universita[]> {
        @Override
        protected Universita[] doInBackground(Void... strings) {
            try {
                URL url = new URL("http://pmsc9.altervista.org/progetto/listaUniversita.php");
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                urlConnection.connect();
                InputStream is = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();

                String result = sb.toString();

                JSONArray jArray = new JSONArray(result);

                Universita[] array_universita = new Universita[jArray.length()];
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    array_universita[i] = new Universita(json_data.getString("codice"), json_data.getString("nome"));
                }
                return array_universita;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Universita[] array_universita) {
            adapter = new ArrayAdapter(RegistrazioneActivity.this, android.R.layout.simple_list_item_1, array_universita);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    universita = (Universita) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    private class registraUtente extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... strings) {
                try {
                    URL url = new URL("http://pmsc9.altervista.org/progetto/registrazione_utente.php");
                    //URL url = new URL("http://10.0.2.2/progetto/registrazione_utente.php");
                    //Passo parametri al file php
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true); //manda dei dati al file php
                    urlConnection.setRequestMethod("POST"); //metodo Post
                    //posso passare solo stringhe --> su PHP se è intero non metto il parametro tra apici
                    String parametri = "universita=" + URLEncoder.encode(universita.codice, "UTF-8") + "&matricola=" + URLEncoder.encode(matricola, "UTF-8") + "&nome=" + URLEncoder.encode(nome, "UTF-8") + "&cognome=" + URLEncoder.encode(cognome, "UTF-8") + "&mail=" + URLEncoder.encode(email, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"); //imposto parametri da passare
                    DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                    dos.writeBytes(parametri); //passo parametri
                    dos.flush();
                    dos.close();

                    //leggo stringa di ritorno da file php
                    urlConnection.connect();
                    InputStream input = urlConnection.getInputStream();  //leggo input mandato dal file php in formato InputStream
                    byte[] buffer = new byte[1024]; //parsifico InputStram
                    int numRead = 0;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while ((numRead = input.read(buffer)) != -1) {
                        baos.write(buffer, 0, numRead);
                    }
                    input.close();
                    String stringaRicevuta = new String(baos.toByteArray()); //converto InputStram in stringa
                    return stringaRicevuta; //ritorno stringa
                } catch (Exception e) {
                    Log.e("SimpleHttpURLConnection", e.getMessage());
                    return "" + e.getMessage();
                } finally {
                }
            }

            @Override
            protected void onPostExecute(String result) {
                output.setText(result);
            }
    }
}
