package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.prefs.Preferences;

public class Home extends AppCompatActivity {
    //controllare se l'utente è ancora iscritto all'universita --> Se non è più iscritto cancello le preferenze e lancio intento sulla pagina di login
    //controllare la connessione alla rete: se c'è prendo i dati dal server, li mostro e li copio in tabella locale, se non c'è li prendo dalla tabella locale
    static final String URL_RICHIEDIAULE="http://pmsc9.altervista.org/progetto/richiedi_aule.php";
    static final String URL_LOGIN="http://pmsc9.altervista.org/progetto/login_studente.php";

    FrameLayout fl;
    LinearLayout frameLista, frameMappa;
    ArrayAdapter adapter;
    ListView elencoAule;
    TextView nomeAula_home,luogoAula_home,postiLiberi_home,flagGruppi_home;
    ImageView immagine_home;
    Button mappa,lista;
    Intent intent;
    String strUniversita, strMatricola, strPassword;
    boolean utente_non_piu_registrato;



protected void initUI(){
     fl= findViewById(R.id.fl);
     elencoAule= findViewById(R.id.elencoAule);
    utente_non_piu_registrato=false;
    //doppio frame
    frameLista = (LinearLayout)findViewById(R.id.frameLista);
    frameMappa = (LinearLayout)findViewById(R.id.frameMappa);
    mappa= findViewById(R.id.mappa);
    lista = findViewById(R.id.lista);
    frameLista.setVisibility(fl.VISIBLE);
    frameMappa.setVisibility(fl.GONE);

    //passo da lista a mappa
    mappa.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
            frameLista.setVisibility(fl.GONE);
            frameMappa.setVisibility(fl.VISIBLE);
        }

    });
    //passo da mappa a lista
    lista.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
            frameMappa.setVisibility(fl.GONE);
            frameLista.setVisibility(fl.VISIBLE);
            new listaAule().execute();
        }

    });
     SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
     strUniversita=settings.getString("universita", null);
     strMatricola=settings.getString("matricola", null);
     strPassword=settings.getString("password", null);
}

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //inizializzo variabili
        initUI();
        //controllo se utente esiste ancora
            intent=getIntent();
            boolean b=intent.getBooleanExtra("from_login",false);
            if(b==false) new checkUtente().execute();
            //aggiorno lista
            new listaAule().execute();

        //click listener
            elencoAule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Aula a = (Aula) parent.getItemAtPosition(position);
                    Intent intent=new Intent(Home.this,InfoAula.class);
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("aula",a);
                    intent.putExtra("bundle_aula", bundle);
                    startActivityForResult(intent, 3);
                }
            });
    }

    //richiedi info aule al database
    private class listaAule extends AsyncTask<Void, Void, Aula[]> {
        @Override
        protected Aula[] doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_RICHIEDIAULE);
                //URL url = new URL("http://10.0.2.2/progetto/richiedi_aule.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8"); //imposto parametri da passare
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
                dos.flush();
                dos.close();

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
                Aula[] array_aula = new Aula[jArray.length()];
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    array_aula[i] = new Aula(json_data.getString("id"), json_data.getString("nome"),
                            json_data.getString("luogo"), json_data.getDouble("latitudine"),
                            json_data.getDouble("longitudine"), json_data.getInt("gruppi"),
                            json_data.getInt("posti_liberi"));
                }
                return array_aula;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }

        }

        @Override
        protected void onPostExecute(Aula[] array_aula) {
            if (array_aula == null) {
                return;
            }
                adapter = new ArrayAdapter<Aula>(Home.this, R.layout.row_layout_home, array_aula) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        Aula item = getItem(position);
                        //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        //View rowView = inflater.inflate(R.layout.row_layout_home, null);
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_home, parent, false);
                        nomeAula_home = convertView.findViewById(R.id.nomeAula_home);
                        luogoAula_home = convertView.findViewById(R.id.luogoAula_home);
                        postiLiberi_home = convertView.findViewById(R.id.postiLiberi_home);
                        flagGruppi_home = convertView.findViewById(R.id.flagGruppi_home);
                        immagine_home=convertView.findViewById(R.id.row_image_home);
                        nomeAula_home.setText(item.nome);
                        luogoAula_home.setText(item.luogo);
                        postiLiberi_home.setText("Numero posti liberi: " + item.posti_liberi);
                        if(item.gruppi==0) {
                            flagGruppi_home.setText("Disponibile per i gruppi");
                            immagine_home.setImageResource(R.drawable.group);
                        }
                        else{
                            flagGruppi_home.setText("Non è disponibile per i gruppi");
                            immagine_home.setImageResource(R.drawable.singolo);
                        }
                        return convertView;

                    }
                };
                elencoAule.setAdapter(adapter);
            }
        }

    //TASK ASINCRONO PER VERIFICARE SE L'UTENTE ESISTE ANCORA ED E' ISCRITTO AD UNIVERSITA'
    private class checkUtente extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_LOGIN);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "universita=" + URLEncoder.encode(strUniversita, "UTF-8") + "&matricola=" + URLEncoder.encode(strMatricola, "UTF-8") + "&password=" + URLEncoder.encode(strPassword, "UTF-8"); //imposto parametri da passare
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
                    user = new User(json_data.getString("matricola"), json_data.getString("codice_universita"),json_data.getString("mail"), json_data.getString("password"),true, json_data.getString("mail_calendar") );
                    return 0;
                }
                return 1;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return 2;
            }
        }

        @Override
        protected void onPostExecute(Integer user) {
            if(user==1){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Non sei abilitato a vedere le informazioni: riapri l'applicazione per fare login</b></font>"),Toast.LENGTH_LONG).show();
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("logged", false);
                editor.commit();
                finish();
            }
        }
    }

    protected void onRestart(){
        super.onRestart();
        new listaAule().execute();
    }

    @Override //creazione menu in alto
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
        menu.add(Menu.FIRST,2, Menu.FIRST+1,"Home");
        return true;
    }

    @Override //se premo "Inserisci componente mi porta alla seconda activity"
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("universita",null);
            editor.putString("nome_universita",null);
            editor.putString("email",null);
            editor.putString("email_calendar",null);
            editor.putString("matricola",null);
            editor.putString("password",null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.commit();
        }
        Intent i=new Intent(this,MainActivity.class);
        startActivityForResult(i,100);
        finish();
        return true;
    }
}
