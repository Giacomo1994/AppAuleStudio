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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {
    static final String URL_UNIVERSITA="http://pmsc9.altervista.org/progetto/login_listaUniversita.php";
    static final String URL_LOGIN_STUDENTE="http://pmsc9.altervista.org/progetto/login_studente.php";
    static final String URL_LOGIN_DOCENTE="http://pmsc9.altervista.org/progetto/login_docente.php";

    ImageView studente_docente;
    TextView txt_toRegistrazione;
    Spinner spinner;
    ArrayAdapter<Universita> adapter;
    EditText txtMatricola, txtPassword;
    Button btn_login;
    RadioButton radioStudente,radioDocente;

    Universita universita=null;
    String matricola, password, token=null;
    boolean isStudente;
    boolean studentePassato;
    boolean is_logged=false, is_studente=false;

    private void initUI(){
        txt_toRegistrazione=findViewById(R.id.log_toRegistrazione);
        spinner=findViewById(R.id.log_spinner);
        txtMatricola=findViewById(R.id.log_matricola);
        txtPassword=findViewById(R.id.log_password);
        btn_login=findViewById(R.id.btn_login);
        radioStudente=findViewById(R.id.radioButton);
        radioDocente=findViewById(R.id.radioDocente);
        studente_docente = findViewById(R.id.imageView9);

        //radio button
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

        //funzione bottone
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matricola=txtMatricola.getText().toString().trim();
                password=txtPassword.getText().toString().trim();

                //controllo campi vuoti
                if(matricola.equals("")||password.equals("")){
                    MyToast.makeText(getApplicationContext(),"Devi inserire tutti i campi!",false).show();
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
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        //ottengo token da Firebase
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this,new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken(); //salvo token in variabile globale
            }
        });

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        is_logged=settings.getBoolean("logged", false);
        is_studente = settings.getBoolean("studente", false);
        if(is_logged==true&&is_studente==true){
            Intent i=new Intent(MainActivity.this, Home.class);
            i.putExtra("start_from_login",false);
            startActivityForResult(i,-1);
            return;
        }
        if(is_logged==true&&is_studente==false){
            Intent i=new Intent(MainActivity.this, HomeDocente.class);
            i.putExtra("from_login",false);
            startActivityForResult(i,23);
            return;
        }

        new riempiUniversita().execute();

    }


    protected void onResume() {
        super.onResume();

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this,new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken(); //salvo token in variabile globale
            }
        });

        new riempiUniversita().execute();

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        is_logged=settings.getBoolean("logged", false);

        if(is_logged==true) finish();
    }

//TASK ASINCRONO PER RIEMPIRE SPINNER UNIVERSITA
    private class riempiUniversita extends AsyncTask<Void, Void, Universita[]> {
        @Override
        protected Universita[] doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_UNIVERSITA);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
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
                    array_universita[i] = new Universita(json_data.getString("codice"), json_data.getString("nome"),
                            json_data.getDouble("latitudine"), json_data.getDouble("longitudine"),
                            json_data.getInt("ingresso"), json_data.getInt("pausa"), json_data.getInt("slot"));
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
                MyToast.makeText(getApplicationContext(),"Errore: impossibile contattare il server!",false).show();
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
                String parametri = "universita=" + URLEncoder.encode(universita.getCodice(), "UTF-8")
                        + "&matricola=" + URLEncoder.encode(matricola, "UTF-8")
                        + "&password=" + URLEncoder.encode(password, "UTF-8")
                        + "&token=" + URLEncoder.encode(token, "UTF-8");
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
                MyToast.makeText(getApplicationContext(),"Impossibile effettuare il login!",false).show();
                return;
            }
            else{
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("universita",universita.getCodice());
                editor.putString("nome_universita",universita.getNome());
                editor.putString("email",user.getEmail());
                editor.putString("matricola",user.getMatricola());
                editor.putString("password",user.getPassword());
                editor.putString("nome", user.getNome());
                editor.putString("cognome", user.getCognome());
                editor.putString("token", token);
                if(user.isStudente()==true) {
                    editor.putString("latitudine", ""+universita.getLatitudine());
                    editor.putString("longitudine", ""+universita.getLongitudine());
                    editor.putString("ingresso", ""+universita.getIngresso());
                    editor.putString("pausa", ""+universita.getPausa());
                    editor.putString("slot", ""+universita.getSlot());
                    //editor.putString("inizio_slot", universita.getInizioSlot());
                    editor.putBoolean("studente", true);
                    Intent i=new Intent(MainActivity.this, Home.class);
                    i.putExtra("start_from_login",true);
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
                MyToast.makeText(getApplicationContext(),"Registrazione avvenuta con successo! Effettua Login per accedere alla pagina personale!",true).show();
            }
        }
    }


}
