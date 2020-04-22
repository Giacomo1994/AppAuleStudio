package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
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
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.prefs.Preferences;

public class Home extends AppCompatActivity {
    //controllare se l'utente è ancora iscritto all'universita --> Se non è più iscritto cancello le preferenze e lancio intento sulla pagina di login
    //controllare la connessione alla rete: se c'è prendo i dati dal server, li mostro e li copio in tabella locale, se non c'è li prendo dalla tabella locale
    static final String URL_RICHIEDIAULE="http://pmsc9.altervista.org/progetto/richiedi_aule.php";
    static final String URL_ORARIDEFAULT="http://pmsc9.altervista.org/progetto/richiedi_orari_default.php";
    static final String URL_ORARISPECIALI="http://pmsc9.altervista.org/progetto/richiedi_orari_speciali.php";
    static final String URL_LOGIN="http://pmsc9.altervista.org/progetto/login_studente.php";

    FrameLayout fl;
    LinearLayout frameLista, frameMappa;
    ArrayAdapter adapter;
    ListView elencoAule;
    TextView nomeAula_home,luogoAula_home,postiLiberi_home,flagGruppi_home, statoAula_home;
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
                URL url;
                HttpURLConnection urlConnection;
                String parametri;
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArrayAule;
                JSONArray jArrayOrariDefault;
                JSONArray jArrayOrariSpeciali;

                url = new URL(URL_RICHIEDIAULE);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8"); //imposto parametri da passare
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
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
                jArrayAule = new JSONArray(result);

                url = new URL(URL_ORARIDEFAULT);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8"); //imposto parametri da passare
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
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
                jArrayOrariDefault = new JSONArray(result);

                url = new URL(URL_ORARISPECIALI);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8"); //imposto parametri da passare
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
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
                jArrayOrariSpeciali = new JSONArray(result);

                Aula[] array_aula = new Aula[jArrayAule.length()];
                for (int i = 0; i < jArrayAule.length(); i++) {
                    JSONObject json_data = jArrayAule.getJSONObject(i);
                    array_aula[i] = new Aula(json_data.getString("id"), json_data.getString("nome"),
                            json_data.getString("luogo"), json_data.getDouble("latitudine"),
                            json_data.getDouble("longitudine"), json_data.getInt("gruppi"),
                            json_data.getInt("posti_liberi"));
                }

                for (int i = 0; i < jArrayOrariDefault.length(); i++) {
                    JSONObject json_data = jArrayOrariDefault.getJSONObject(i);
                    String id=json_data.getString("id_aula");
                    int day=json_data.getInt("giorno");
                    String apertura=json_data.getString("apertura");
                    String chiusura=json_data.getString("chiusura");
                    for(Aula a:array_aula){
                        if(a.idAula.equals(id)) a.addOrario(day, new Orario_Speciale(apertura,chiusura));
                    }
                }

                for (int i = 0; i < jArrayOrariSpeciali.length(); i++) {
                    JSONObject json_data = jArrayOrariSpeciali.getJSONObject(i);
                    String id=json_data.getString("id_aula");
                    String apertura=json_data.getString("riapertura");
                    String chiusura=json_data.getString("chiusura");
                    for(Aula a:array_aula){
                        if(a.idAula.equals(id)) a.setOrario_speciale(new Orario_Speciale(apertura,chiusura));
                    }
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
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_home, parent, false);
                        nomeAula_home = convertView.findViewById(R.id.nomeAula_home);
                        luogoAula_home = convertView.findViewById(R.id.luogoAula_home);
                        postiLiberi_home = convertView.findViewById(R.id.postiLiberi_home);
                        flagGruppi_home = convertView.findViewById(R.id.flagGruppi_home);
                        immagine_home=convertView.findViewById(R.id.row_image_home);
                        statoAula_home=convertView.findViewById(R.id.statoAula_home);

                        nomeAula_home.setText(item.nome);
                        luogoAula_home.setText(item.luogo);
                        postiLiberi_home.setText("Numero posti liberi: " + item.posti_liberi);

                        //per gruppi o no
                        if(item.gruppi==0) {
                            flagGruppi_home.setText("Disponibile per i gruppi");
                            immagine_home.setImageResource(R.drawable.group);
                        }
                        else{
                            flagGruppi_home.setText("Non è disponibile per i gruppi");
                            immagine_home.setImageResource(R.drawable.singolo);
                        }
                        //chiusa-aperta
                        Calendar calendar = Calendar.getInstance();
                        int today = calendar.get(Calendar.DAY_OF_WEEK)-1;
                        if(today==0) today=7;
                        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String orarioAttuale=format.format(calendar.getTime());
                        boolean isAperta=item.isAperta(today,orarioAttuale);
                        if(isAperta==true) statoAula_home.setText("Attualmente Aperta");
                        else statoAula_home.setText("Attualmente Chiusa");

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
                URL url;
                HttpURLConnection urlConnection;
                String parametri;
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArray;
                url = new URL(URL_LOGIN);
                urlConnection= (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "universita=" + URLEncoder.encode(strUniversita, "UTF-8") + "&matricola=" + URLEncoder.encode(strMatricola, "UTF-8") + "&password=" + URLEncoder.encode(strPassword, "UTF-8"); //imposto parametri da passare
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
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
                jArray = new JSONArray(result);

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
            if(user==2){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile connettersi alla rete</b></font>"),Toast.LENGTH_LONG).show();
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
