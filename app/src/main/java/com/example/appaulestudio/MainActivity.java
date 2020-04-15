package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
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

public class MainActivity extends AppCompatActivity {
    TextView txt_toRegistrazione;
    Spinner spinner;
    ArrayAdapter<Universita> adapter;
    EditText txtMatricola;
    EditText txtPassword;
    Button btn_login;
    RadioButton radioStudente;

    Universita universita=null;
    String matricola;
    String password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        boolean logged=settings.getBoolean("logged", false);

        if(logged==true){
            Intent i=new Intent(MainActivity.this, Home.class);
            startActivityForResult(i,2);
        }

        txt_toRegistrazione=findViewById(R.id.log_toRegistrazione);
        spinner=findViewById(R.id.log_spinner);
        txtMatricola=findViewById(R.id.log_matricola);
        txtPassword=findViewById(R.id.log_password);
        btn_login=findViewById(R.id.btn_login);
        radioStudente=findViewById(R.id.radioButton);

        //link a registrazione
        String stringa="Oppure registrati";
        SpannableString ss=new SpannableString(stringa);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,RegistrazioneActivity.class);
                startActivityForResult(i,1);
            }
        };

        ss.setSpan(clickableSpan1, 7, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt_toRegistrazione.setText(ss);
        txt_toRegistrazione.setMovementMethod(LinkMovementMethod.getInstance());

        //riempi universita
        new riempiUniversita().execute();


        //funzione bottone
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matricola=txtMatricola.getText().toString().trim();
                password=txtPassword.getText().toString().trim();

                //controllo campi vuoti
                if(matricola.equals("")||password.equals("")){
                    Toast.makeText(getApplicationContext(),"Devi inserire tutti i campi!",Toast.LENGTH_SHORT).show();
                    return;
                }

                //chiamo asyc task
                if(radioStudente.isChecked()) new checkUtente().execute();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        boolean logged=settings.getBoolean("logged", false);

        if(logged==true){
            finish();
        }
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
            adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, array_universita);
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

    private class checkUtente extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... strings) {
            try {
                URL url = new URL("http://pmsc9.altervista.org/progetto/login_utente.php");
                //URL url = new URL("http://10.0.2.2/progetto/login_utente.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST"); //metodo Post
                urlConnection.setDoOutput(true); //manda dei dati al file php
                urlConnection.setDoInput(true);
                String parametri = "universita=" + URLEncoder.encode(universita.codice, "UTF-8") + "&matricola=" + URLEncoder.encode(matricola, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"); //imposto parametri da passare
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri); //passo parametri
                dos.flush();
                dos.close();

                //leggo stringa di ritorno da file php
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
                User user=null;
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    user = new User(json_data.getString("matricola"),json_data.getString("codice_universita"), json_data.getString("password"),true );
                }
                return user;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if(user==null) {
                Toast.makeText(getApplicationContext(),"Impossibile effettuare login!",Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("universita",universita.codice);
                editor.putString("matricola",matricola);
                editor.putString("password",password);
                editor.putBoolean("studente", true);
                editor.putBoolean("logged", true);
                editor.commit();


                Intent i=new Intent(MainActivity.this, Home.class);
                startActivityForResult(i,2);
                finish();
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode== Activity.RESULT_OK){
                txtMatricola.setText(data.getStringExtra("matricola"));
                txtPassword.setText(data.getStringExtra("password"));
                Toast.makeText(getApplicationContext(),"Registrazione avvenuta con successo!",Toast.LENGTH_LONG).show();
            }
        }
    }


}
