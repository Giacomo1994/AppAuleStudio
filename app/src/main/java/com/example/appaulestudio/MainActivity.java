package com.example.appaulestudio;

import androidx.appcompat.app.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import android.graphics.*;

public class MainActivity extends AppCompatActivity {
    TextView txt_toRegistrazione;
    Spinner spinner;
    ArrayAdapter<Universita> adapter;
    EditText txtMatricola, txtPassword;
    Button btn_login;
    RadioButton radioStudente;
    RadioButton radioDocente;
    Universita universita=null;
    String matricola, password;
    boolean isStudente;
    boolean studentePassato;
    CheckBox checkRicordami;
    ImageView studente_docente;


    static final String URL_UNIVERSITA="http://pmsc9.altervista.org/progetto/listaUniversita.php";
    static final String URL_LOGIN_STUDENTE="http://pmsc9.altervista.org/progetto/login_studente.php";
    static final String URL_LOGIN_DOCENTE="http://pmsc9.altervista.org/progetto/login_docente.php";

    private void initUI(){
        checkRicordami=findViewById(R.id.checkRicordami);
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        boolean logged=settings.getBoolean("logged", false);
        boolean is_studente = settings.getBoolean("studente", false);
        if(logged==true&&is_studente==true){
            Intent i=new Intent(MainActivity.this, Home.class);
            i.putExtra("from_login",false);
            startActivityForResult(i,2);
            return;
        }
        if(logged==true&&is_studente==false){
            Intent i=new Intent(MainActivity.this, HomeDocente.class);
            i.putExtra("from_login",false);
            startActivityForResult(i,23);
            return;
        }

        txt_toRegistrazione=findViewById(R.id.log_toRegistrazione);
        spinner=findViewById(R.id.log_spinner);
        txtMatricola=findViewById(R.id.log_matricola);
        txtPassword=findViewById(R.id.log_password);
        btn_login=findViewById(R.id.btn_login);
        radioStudente=findViewById(R.id.radioButton);
        radioDocente=findViewById(R.id.radioDocente);
        studente_docente = findViewById(R.id.imageView9);


        if(radioStudente.isChecked()){
            studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.studente));
        }else {
            studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.docente));
        }


       radioStudente.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {

           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               if(radioStudente.isChecked()){
                   studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.studente));
               }else {
                   studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.docente));
               }

           }
       });


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
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>" + "Devi inserire tutti i campi!" + "</b></font>"),Toast.LENGTH_LONG).show();
                    return;
                }
                if(radioStudente.isChecked()){
                    isStudente=true;
                }
                else isStudente=false;
                //sposto tutto nella funzione
                //chiamo asyc task
                //if(radioStudente.isChecked()) new checkUtente().execute();
                new checkUtente().execute();
                /*if(checkRicordami.isChecked()){
                    SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("logged", true);
                }
                else{
                    SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("logged", false);
                }*/
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        boolean logged=settings.getBoolean("logged", false);

        if(logged==true) finish();
        else new riempiUniversita().execute();

    }

    //TASK ASINCRONO PER RIEMPIRE SPINNER UNIVERSITA
    private class riempiUniversita extends AsyncTask<Void, Void, Universita[]> {
        @Override
        protected Universita[] doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_UNIVERSITA);
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
            if(array_universita==null){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>" + "Impossibile connettersi!" + "</b></font>"),Toast.LENGTH_LONG).show();
                //spinner.setEnabled(false);
                return;
            }
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
    //TASK ASINCRONO PER LOGIN UTENTE
    private class checkUtente extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... strings) {
            try {
                URL url;
                if(isStudente==true) {
                     url = new URL(URL_LOGIN_STUDENTE);
                    //URL url = new URL("http://10.0.2.2/progetto/login_utente.php");
                }
                else{
                    url=new URL(URL_LOGIN_DOCENTE);
                }
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "universita=" + URLEncoder.encode(universita.getCodice(), "UTF-8") + "&matricola=" + URLEncoder.encode(matricola, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"); //imposto parametri da passare
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
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
                    if(isStudente==true) {

                        user = new User(json_data.getString("matricola"),
                                json_data.getString("nome"),
                                json_data.getString("cognome"),
                                json_data.getString("codice_universita"), json_data.getString("mail"),
                                json_data.getString("password"), true);
                    }
                    else{
                        user = new User(json_data.getString("matricola"),
                                json_data.getString("nome"),
                                json_data.getString("cognome"),
                                json_data.getString("codice_universita"), json_data.getString("mail"),
                                json_data.getString("password"), false);
                    }
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
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>" + "Impossibile effettuare login!</b></font>"),Toast.LENGTH_LONG).show();
                return;
            }
            else{
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("universita",universita.getCodice());
                editor.putString("nome_universita",universita.getCodice());
                editor.putString("email",user.getEmail());
                //editor.putString("email_calendar",user.getEmail_calendar());
                editor.putString("matricola",user.getMatricola());
                editor.putString("password",user.getPassword());
                editor.putString("nome", user.getNome());
                editor.putString("cognome", user.getCognome());
                if(user.isStudente()==true) {
                    editor.putBoolean("studente", true);
                    Intent i=new Intent(MainActivity.this, Home.class);
                    i.putExtra("from_login",true);
                    startActivityForResult(i,2);
                    finish();
                }
                else{
                    editor.putBoolean("studente", false);
                    Intent i=new Intent(MainActivity.this, HomeDocente.class);
                    i.putExtra("from_login",true);
                    startActivityForResult(i,3);
                    finish();
                }
                editor.putBoolean("logged", true);
                editor.commit();

                //Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>" + user.email_calendar + "</b></font>"),Toast.LENGTH_LONG).show();

               /* Intent i=new Intent(MainActivity.this, Home.class);
                i.putExtra("from_login",true);
                startActivityForResult(i,2);
                finish();*/
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode== Activity.RESULT_OK){
                studentePassato= (data.getBooleanExtra("isStudente", true));
                txtMatricola.setText(data.getStringExtra("matricola"));
                txtPassword.setText(data.getStringExtra("password"));

                if(studentePassato==true){
                    radioStudente.setChecked(true);
                    radioDocente.setChecked(false);
                }
                else{
                    radioDocente.setChecked(true);
                    radioStudente.setChecked(false);
                }

                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#0cb339' ><b>" + "Registrazione avvenuta con successo! Effettua Login per accedere alla pagina personale." + "</b></font>"),Toast.LENGTH_LONG).show();
            }
        }
    }


}
