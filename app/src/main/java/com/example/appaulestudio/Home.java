package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Home extends AppCompatActivity{
    //controllare se l'utente è ancora iscritto all'universita --> Se non è più iscritto cancello le preferenze e lancio intento sulla pagina di login
    //controllare la connessione alla rete: se c'è prendo i dati dal server, li mostro e li copio in tabella locale, se non c'è li prendo dalla tabella locale
    static final String URL_RICHIEDIAULE="http://pmsc9.altervista.org/progetto/richiedi_aule.php";
    static final String URL_ORARIDEFAULT="http://pmsc9.altervista.org/progetto/richiedi_orari_default.php";
    static final String URL_CHECKCONNECTION="http://pmsc9.altervista.org/progetto/check_connection.php";
    static final String URL_CHECKAULAPERTA="http://pmsc9.altervista.org/progetto/check_aula_aperta.php";
    static final String URL_LOGIN="http://pmsc9.altervista.org/progetto/login_studente.php";
    static final String URL_LAST_UPDATE="http://pmsc9.altervista.org/progetto/home_lastUpdate.php";

    FrameLayout fl;
    LinearLayout frameLista, frameMappa;
    ArrayAdapter adapter;
    ListView elencoAule;
    TextView nomeAula_home,luogoAula_home,postiLiberi_home,flagGruppi_home, statoAula_home;
    ImageView immagine_home;
    Button mappa,lista;
    ProgressBar bar;

    Intent intent;
    String strUniversita, strMatricola, strPassword;
    boolean utente_non_piu_registrato;
    SQLiteDatabase db;
    private Cursor cursor;


protected void initUI(){
     fl= findViewById(R.id.fl);
     elencoAule= findViewById(R.id.elencoAule);
     utente_non_piu_registrato=false;
     frameLista = (LinearLayout)findViewById(R.id.frameLista);
     frameMappa = (LinearLayout)findViewById(R.id.frameMappa);
     mappa= findViewById(R.id.mappa);
     lista = findViewById(R.id.lista);
     bar=findViewById(R.id.bar);
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
            checkConnection("restart");
        }

    });

     //prendo preferenze
     SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
     strUniversita=settings.getString("universita", null);
     strMatricola=settings.getString("matricola", null);
     strPassword=settings.getString("password", null);
     setTitle(strMatricola);

}

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //inizializzo variabili
        initUI();
        intent=getIntent();
        boolean b=intent.getBooleanExtra("from_login",false);

        //task asincroni
        if(b==false) new checkUtente().execute();
        checkConnection("create");

        registerForContextMenu(elencoAule);
    }

//MENU CONTESTUALE
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Aula a= (Aula) elencoAule.getItemAtPosition(info.position);
        menu.add(Menu.FIRST, 0, Menu.FIRST,"Visualizza info aula");
        if(a.aperta==true) menu.add(Menu.FIRST, 1, Menu.FIRST+1,"Prenota posto");
        if(a.gruppi==0) menu.add(Menu.FIRST, 2, Menu.FIRST+2,"Prenota per gruppo");
        if(a.posti_liberi==0) menu.add(Menu.FIRST, 3, Menu.FIRST+3,"Avvisami quando si libera posto");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getItemId()==0){
            Aula a= (Aula) elencoAule.getItemAtPosition(info.position);
            Intent intent=new Intent(Home.this,InfoAulaActivity.class);
            Bundle bundle=new Bundle();
            bundle.putParcelable("aula",a);
            bundle.putParcelable("orario",a.orario);
            intent.putExtra("bundle", bundle);
            startActivityForResult(intent, 3);
        }

        return true;
    }

//check connection
    public void checkConnection(final String metodo){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url =URL_CHECKCONNECTION;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        new listaAule().execute();
                        if(metodo.equals("create")) new check_last_update().execute();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile contattare il server: i dati potrebbero essere non aggiornati</b></font>"), Toast.LENGTH_LONG).show();
                bar.setVisibility(View.GONE);
                mostraOffline();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(2000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

//se non c'è connessione mostra nella listview i dati da SQLITE
    public void mostraOffline(){
        db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM info_aule_offline";
        cursor = db.rawQuery(sql, null);
        ArrayList<Aula> aule=new ArrayList<Aula>();
        if(cursor==null ||cursor.getCount()==0){
            return;
        }
        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            String id=cursor.getString(cursor.getColumnIndex("id"));
            String nome=cursor.getString(cursor.getColumnIndex("nome"));
            String luogo=cursor.getString(cursor.getColumnIndex("luogo"));
            double latitudine=cursor.getDouble(cursor.getColumnIndex("latitudine"));
            double longitudine=cursor.getDouble(cursor.getColumnIndex("longitudine"));
            int flag_gruppi=cursor.getInt(cursor.getColumnIndex("flag_gruppi"));
            int posti_totali=cursor.getInt(cursor.getColumnIndex("posti_totali"));
            int posti_liberi=-1;
            //int posti_liberi=cursor.getInt(cursor.getColumnIndex("posti_liberi"));
            String servizi=cursor.getString(cursor.getColumnIndex("servizi"));
            Aula a=new Aula(id,nome,luogo,latitudine,longitudine,flag_gruppi,posti_totali,posti_liberi,servizi);
            aule.add(a);
        }
        sql = "SELECT * FROM orari_offline";
        cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0){
            return;
        }
        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            String id=cursor.getString(cursor.getColumnIndex("id_aula"));
            int giorno=cursor.getInt(cursor.getColumnIndex("giorno"));
            String apertura=cursor.getString(cursor.getColumnIndex("apertura"));
            String chiusura=cursor.getString(cursor.getColumnIndex("chiusura"));
            for(Aula a: aule){
                if(a.idAula.equals(id)){
                    a.orari.put(giorno,new Orario(apertura,chiusura));
                }
            }
        }
        adapter = new ArrayAdapter<Aula>(Home.this, R.layout.row_layout_home, aule) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Aula item = getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_home, parent, false);
                nomeAula_home = convertView.findViewById(R.id.nomeAula_home);
                luogoAula_home = convertView.findViewById(R.id.luogoAula_home);
                postiLiberi_home = convertView.findViewById(R.id.postiLiberi_home);
                flagGruppi_home = convertView.findViewById(R.id.flagGruppi_home);
                immagine_home = convertView.findViewById(R.id.row_image_home);
                statoAula_home = convertView.findViewById(R.id.statoAula_home);

                nomeAula_home.setText(item.nome);
                luogoAula_home.setText(item.luogo);
                postiLiberi_home.setText("Posti totali: " + item.posti_totali);

                //per gruppi o no
                if (item.gruppi == 0) {
                    flagGruppi_home.setText("Disponibile per i gruppi");
                    immagine_home.setImageResource(R.drawable.group);
                } else {
                    flagGruppi_home.setText("Non è disponibile per i gruppi");
                    immagine_home.setImageResource(R.drawable.singolo);
                }
                //orario
                Calendar calendar = Calendar.getInstance();
                int today = calendar.get(Calendar.DAY_OF_WEEK);
                statoAula_home.setText(""+item.orari.get(today).apertura+" - "+item.orari.get(today).chiusura);
                return convertView;
            }
        };
        elencoAule.setAdapter(adapter);
        return;

    }
//controllo ultimo aggiornamento aule --> Se è il caso allora aggiorno i dati su SQLITE (task asincrono successivo)
    private class check_last_update extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voide) {
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
                JSONArray jArrayLastUpdate;

                url = new URL(URL_LAST_UPDATE);
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
                jArrayLastUpdate = new JSONArray(result);
                JSONObject data = jArrayLastUpdate.getJSONObject(0);
                String last_update=data.getString("last_update");
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                String last_update_prefs=settings.getString("last_update", null);
                if(last_update_prefs!=null && last_update_prefs.equals(last_update)) return null;
                return last_update;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(String result) {
            if(result==null) return;
            else {
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("last_update", result);
                editor.commit();
                new aggiornaSQLITE().execute();
            }
        }
    }

// aggiorno i dati statici delle aule + orari default su SQLITE alla creazione dell'activity
    private class aggiornaSQLITE extends AsyncTask<Void, Void, Aula[]> {
        @Override
        protected Aula[] doInBackground(Void... voide) {
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

                Aula[] array_aula = new Aula[jArrayAule.length()];
                for (int i = 0; i < jArrayAule.length(); i++) {
                    JSONObject json_data = jArrayAule.getJSONObject(i);
                    array_aula[i] = new Aula(json_data.getString("id"), json_data.getString("nome"),
                            json_data.getString("luogo"), json_data.getDouble("latitudine"),
                            json_data.getDouble("longitudine"), json_data.getInt("gruppi"),
                            json_data.getInt("posti_totali"), json_data.getInt("posti_liberi"), json_data.getString("servizi"));
                }
                for (int i = 0; i < jArrayOrariDefault.length(); i++) {
                    JSONObject json_data = jArrayOrariDefault.getJSONObject(i);
                    String id = json_data.getString("id_aula");
                    int day=json_data.getInt("giorno");
                    String apertura = json_data.getString("apertura");
                    String chiusura = json_data.getString("chiusura");
                    for (Aula a : array_aula) {
                        if (a.idAula.equals(id)) a.orari.put(day,new Orario(apertura,chiusura));
                    }
                }
                return array_aula;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(Aula[] array_aula) {
            if(array_aula==null) return;
            db=dbHelper.getWritableDatabase();
            String sql1 = "DELETE FROM info_aule_offline";
            String sql2="DELETE FROM orari_offline";
            db.execSQL(sql2);
            db.execSQL(sql1);
                for (Aula a : array_aula) {
                    String sql =
                            "INSERT INTO info_aule_offline (id, nome, luogo, latitudine, longitudine,posti_totali,posti_liberi, flag_gruppi, servizi) " +
                                    "VALUES ('" + a.idAula + "', '" + a.nome + "', '" + a.luogo + "', " + a.latitudine + "," + a.longitudine + "," + a.posti_totali + "," + a.posti_liberi + "," + a.gruppi + ",'" + a.servizi + "')";
                    db.execSQL(sql);
                }
                for(Aula a : array_aula){
                    for(int i = 1; i<=7; i++) {
                        String sql = "INSERT INTO orari_offline (id_aula, giorno, apertura, chiusura)" +
                                "VALUES('" + a.idAula + "', " + i + ", '" + a.orari.get(i).apertura + "','" + a.orari.get(i).chiusura + "')";
                        db.execSQL(sql);
                    }
                }
        }
    }

//RIEMPIO LISTVIEW CON I DATI CHE MI SERVONO, PRENDENDOLI DA REMOTO SE C'E' CONNESSIONE OPPURE DA SQLITE
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
                JSONArray jArrayAuleAperte;

                url = new URL(URL_RICHIEDIAULE);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
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


                url = new URL(URL_CHECKAULAPERTA);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
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
                jArrayAuleAperte = new JSONArray(result);


                Aula[] array_aula = new Aula[jArrayAule.length()];
                for (int i = 0; i < jArrayAule.length(); i++) {
                    JSONObject json_data = jArrayAule.getJSONObject(i);
                    array_aula[i] = new Aula(json_data.getString("id"), json_data.getString("nome"),
                            json_data.getString("luogo"), json_data.getDouble("latitudine"),
                            json_data.getDouble("longitudine"), json_data.getInt("gruppi"),
                            json_data.getInt("posti_totali"), json_data.getInt("posti_liberi"), json_data.getString("servizi"));
                }

                for (int i = 0; i < jArrayAuleAperte.length(); i++) {
                    JSONObject json_data = jArrayAuleAperte.getJSONObject(i);
                    String id = json_data.getString("id_aula");
                    for (Aula a : array_aula) {
                        if (a.idAula.equals(id)) a.aperta=true;
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
            bar.setVisibility(ProgressBar.GONE);
            if (array_aula == null) { //prendo i dati da sql locale perchè non riesco ad accedere ai dati in remoto
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile contattare il server: i dati potrebbero essere non aggiornati</b></font>"), Toast.LENGTH_LONG).show();
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
                    immagine_home = convertView.findViewById(R.id.row_image_home);
                    statoAula_home = convertView.findViewById(R.id.statoAula_home);

                    nomeAula_home.setText(item.nome);
                    luogoAula_home.setText(item.luogo);
                    postiLiberi_home.setText("Posti liberi: " + item.posti_liberi + " su " + item.posti_totali);

                    //per gruppi o no
                    if (item.gruppi == 0) {
                        flagGruppi_home.setText("Disponibile per i gruppi");
                        immagine_home.setImageResource(R.drawable.group);
                    } else {
                        flagGruppi_home.setText("Non è disponibile per i gruppi");
                        immagine_home.setImageResource(R.drawable.singolo);
                    }
                    //chiusa-aperta
                    if (item.aperta == true){
                        statoAula_home.setTextColor(Color.argb(255, 12, 138, 17));
                        statoAula_home.setText("Attualmente Aperta");
                    }
                    else{
                        statoAula_home.setTextColor(Color.RED);
                        statoAula_home.setText("Attualmente Chiusa");
                    }

                    return convertView;
                }
            };
            elencoAule.setAdapter(adapter);
        }
    }

//TASK ASINCRONO PER VERIFICARE SE L'UTENTE ESISTE ANCORA ED E' ISCRITTO AD UNIVERSITA' --> SE NON LO E' PIU' VIENE PORTATO A PAGINA LOGIN
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
                    urlConnection = (HttpURLConnection) url.openConnection();
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

                    User user = null;
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        user = new User(json_data.getString("matricola"),json_data.getString("nome"),json_data.getString("cognome"), json_data.getString("codice_universita"), json_data.getString("mail"), json_data.getString("password"), true, json_data.getString("mail_calendar"));
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
                if (user == 1) {
                    SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("universita", null);
                    editor.putString("nome_universita", null);
                    editor.putString("email", null);
                    editor.putString("email_calendar", null);
                    editor.putString("matricola", null);
                    editor.putString("password", null);
                    editor.putBoolean("studente", true);
                    editor.putBoolean("logged", false);
                    editor.commit();
                    Intent i = new Intent(Home.this, MainActivity.class);
                    startActivityForResult(i, 100);
                    finish();
                }
            }
        }

//ON RESTART
        protected void onRestart() {
            super.onRestart();
            bar.setVisibility(ProgressBar.VISIBLE);
            checkConnection("restart");
        }

//CREAZIONE MENU IN ALTO
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
            menu.add(Menu.FIRST, 2, Menu.FIRST + 1, "Home");
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
                editor.putString("email_calendar", null);
                editor.putString("matricola", null);
                editor.putString("password", null);
                editor.putBoolean("studente", true);
                editor.putBoolean("logged", false);
                editor.putString("last_update", null);
                editor.commit();
                Intent i = new Intent(this, MainActivity.class);
                startActivityForResult(i, 100);
                finish();
            }
            if (item.getItemId() == 2) {
                Intent i = new Intent(this, Home.class);
                startActivityForResult(i, 100);
                finish();
            }
            return true;
        }

//DATABASE SQLITE
        private final SQLiteOpenHelper dbHelper = new SQLiteOpenHelper(Home.this, "info_aule_offline", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {

                String sql = "CREATE TABLE \"info_aule_offline\" (\n" +
                        "\t\"id\"\tTEXT,\n" +
                        "\t\"nome\"\tTEXT,\n" +
                        "\t\"luogo\"\tTEXT,\n" +
                        "\t\"latitudine\"\tREAL,\n" +
                        "\t\"longitudine\"\tREAL,\n" +
                        "\t\"posti_totali\"\tINTEGER,\n" +
                        "\t\"posti_liberi\"\tINTEGER,\n" +
                        "\t\"flag_gruppi\"\tINTEGER,\n" +
                        "\t\"servizi\"\tTEXT,\n" +
                        "\tPRIMARY KEY(\"id\")\n" +
                        ")";
                db.execSQL(sql);

                String sql1 = "CREATE TABLE \"orari_offline\" (\n" +
                        "\t\"id_aula\"\tTEXT,\n" +
                        "\t\"giorno\"\tINTEGER,\n" +
                        "\t\"apertura\"\tTEXT,\n" +
                        "\t\"chiusura\"\tTEXT,\n" +
                        "\tPRIMARY KEY(\"id_aula\",\"giorno\")\n" +
                        ");";
                db.execSQL(sql1);
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            }
        };


}
