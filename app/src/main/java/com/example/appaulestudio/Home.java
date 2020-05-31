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
import android.os.CountDownTimer;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Home extends AppCompatActivity{
    static final String URL_AULE_DINAMICHE="http://pmsc9.altervista.org/progetto/home_info_dinamiche_aule.php";
    static final String URL_ORARI_DEFAULT="http://pmsc9.altervista.org/progetto/home_orari_default.php";
    static final String URL_LAST_UPDATE="http://pmsc9.altervista.org/progetto/home_lastUpdate.php";
    static final String URL_TEMPI="http://pmsc9.altervista.org/progetto/home_dati_universita.php";

    LinearLayout ll_start,ll_home;
    ArrayAdapter adapter;
    ListView elencoAule;
    TextView nomeAula_home,luogoAula_home,postiLiberi_home,flagGruppi_home, statoAula_home;
    ImageView immagine_home;
    Button mappa;
    ProgressBar bar;
    Aula[] array_aule=null;
    boolean from_login=true;
    int ready=-1, ready_update=-1;

    Intent intent;
    String strUniversita, strMatricola, strNome, strToken, strCognome;
    SqliteManager database;

    protected void initUI(){
         ll_start=findViewById(R.id.ll_start);
         ll_home=findViewById(R.id.ll_home);
         elencoAule= findViewById(R.id.elencoAule);
         mappa= findViewById(R.id.mappa);
         bar=findViewById(R.id.bar);
         ll_start.setVisibility(View.VISIBLE);
         ll_home.setVisibility(View.GONE);
         //prendo preferenze
         SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
         strUniversita=settings.getString("universita", null);
         strMatricola=settings.getString("matricola", null);
         strNome=settings.getString("nome", null);
         strCognome=settings.getString("cognome", null);
         strToken=settings.getString("token", null);

         setTitle(strNome+" "+strCognome);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initUI();
        database=new SqliteManager(Home.this);

        intent=getIntent();
        if(intent.hasExtra("start_from_login") && intent.getBooleanExtra("start_from_login",true)==true) from_login=true;
        else from_login=false;
        if(from_login==true || from_login==false){
            new listaAule().execute();
        }
        //listener listview
        elencoAule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Aula a= (Aula) parent.getItemAtPosition(position);
                    Intent intent=new Intent(Home.this,InfoAulaActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("aula",a);
                    HashMap<Integer,Orario> orari_aula=database.readOrariAula(a.getIdAula());
                    bundle.putSerializable("orari",orari_aula);
                    intent.putExtra("bundle", bundle);
                    startActivityForResult(intent, 3);
                }
        });
        //listener pulsante
        mappa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List aule_list= Arrays.asList(array_aule);
                ArrayList<Aula> aule_array_list=new ArrayList<Aula>();
                aule_array_list.addAll(aule_list);
                Intent intent_to_map = new Intent(Home.this,MapActivity.class);
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("aule",aule_array_list);
                intent_to_map.putExtra("bundle_aule",bundle);
                startActivity(intent_to_map);
            }
        });
        if(intent.hasExtra("start_from_login")){
            getSupportActionBar().hide();
            new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if(ready!=-1 && ready_update!=-1 && millisUntilFinished<28000){
                        ll_start.setVisibility(View.GONE);
                        ll_home.setVisibility(View.VISIBLE);
                        if(ready==1) MyToast.makeText(getApplicationContext(), "Sei offline: i dati potrebbero non essere aggiornati", false).show();
                        getSupportActionBar().show();
                        cancel();
                    }
                }
                public void onFinish() {
                    if(ready==0){
                        ll_home.setVisibility(View.VISIBLE);
                        ll_start.setVisibility(View.GONE);
                        getSupportActionBar().show();
                    }
                    else if(ready==1){
                        ll_home.setVisibility(View.VISIBLE);
                        ll_start.setVisibility(View.GONE);
                        getSupportActionBar().show();
                        MyToast.makeText(getApplicationContext(), "Sei offline: i dati potrebbero non essere aggiornati", false).show();
                    }
                    else if(ready==-1) {
                        ll_start.setVisibility(View.GONE);
                        ll_home.setVisibility(View.GONE);
                        MyToast.makeText(getApplicationContext(), "Impossibile mostrare i dati", false).show();
                    }
                }
            }.start();
        }
        else{
            ll_start.setVisibility(View.GONE);
            ll_home.setVisibility(View.VISIBLE);
        }
    }


//se non c'è connessione mostra nella listview i dati da SQLITE
    public void mostraOffline(){
        ArrayList<Aula> aule=database.readListaAule();
        if(aule==null){
            MyToast.makeText(getApplicationContext(), "Errore: impossibile mostrare aule!", false).show();
            return;
        }
        array_aule = new Aula[aule.size()];
        array_aule = aule.toArray(array_aule);

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

                nomeAula_home.setText(item.getNome());
                luogoAula_home.setText(item.getLuogo());
                postiLiberi_home.setText("Posti totali: " + item.getPosti_totali());

                //per gruppi o no
                if (item.getGruppi() == 0) {
                    flagGruppi_home.setText("Disponibile per i gruppi");
                    immagine_home.setImageResource(R.drawable.group);
                } else {
                    flagGruppi_home.setText("Non è disponibile per i gruppi");
                    immagine_home.setImageResource(R.drawable.singolo);
                }
                //orario
                Calendar calendar = Calendar.getInstance();
                int today = calendar.get(Calendar.DAY_OF_WEEK);
                statoAula_home.setText("Orario odierno: "+item.getOrari().get(today).getApertura().substring(0,5)+" - "+item.getOrari().get(today).getChiusura().substring(0,5));
                return convertView;
            }
        };
        elencoAule.setAdapter(adapter);
        return;

    }
//controllo ultimo aggiornamento aule --> Se non coincide con quello salvato nell preferenze allora aggiorno i dati su SQLITE (task asincrono successivo)
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
            if(result==null) {
                ready_update=0;
                return;
            }
            else {
                new aggiornaSQLITE().execute();
                if(from_login==false)new aggiornaPreferenzeUniversita().execute();
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("last_update", result);
                editor.commit();
            }
        }
    }

    //controllo ultimo aggiornamento aule --> Se non coincide con quello salvato nell preferenze allora aggiorno i dati su SQLITE (task asincrono successivo)
    private class aggiornaPreferenzeUniversita extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... voide) {
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

                url = new URL(URL_TEMPI);
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
                String[] ar_prefs= new String[4];
                JSONObject data = jArrayLastUpdate.getJSONObject(0);
                ar_prefs[0]=""+data.getInt("ingresso");
                ar_prefs[1]=""+data.getInt("pausa");
                ar_prefs[2]=""+data.getInt("slot");
                ar_prefs[3]=""+data.getString("first_slot");
                return ar_prefs;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(String[] result) {
            if(result==null) return;

            else {
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("ingresso", result[0]);
                editor.putString("pausa", result[1]);
                editor.putString("slot", result[2]);
                editor.putString("first_slot", ""+result[3]);
                editor.commit();
            }
        }
    }

// aggiorno i dati statici delle aule + orari default su SQLITE
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

                url = new URL(URL_ORARI_DEFAULT);
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

                Aula[] array_aula=array_aule;
                for (int i = 0; i < jArrayOrariDefault.length(); i++) {
                    JSONObject json_data = jArrayOrariDefault.getJSONObject(i);
                    String id = json_data.getString("id_aula");
                    int day=json_data.getInt("giorno");
                    String apertura = json_data.getString("apertura");
                    String chiusura = json_data.getString("chiusura");
                    for (Aula a : array_aula) {
                        if (a.getIdAula().equals(id)) a.getOrari().put(day,new Orario(apertura,chiusura));
                    }
                }
                return array_aula;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(Aula[] array_aula) {
            ready_update=0;
            if(array_aula==null) return;
            else database.writeAuleOrari(array_aula);
        }
    }

//RIEMPIO LISTVIEW CON I DATI CHE MI SERVONO, PRENDENDOLI DA REMOTO
    private class listaAule extends AsyncTask<Void, Void, Aula[]> {
        @Override
        protected Aula[] doInBackground(Void... strings) {
            try {
                URL url= new URL(URL_AULE_DINAMICHE);
                HttpURLConnection urlConnection= (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2000);
                urlConnection.setConnectTimeout(2000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8");
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
                    Aula aa=new Aula(json_data.getString("id"), json_data.getString("nome"),
                            json_data.getString("luogo"), json_data.getDouble("latitudine"),
                            json_data.getDouble("longitudine"), json_data.getInt("gruppi"),
                            json_data.getInt("posti_totali"), json_data.getInt("posti_liberi"), json_data.getString("servizi"));
                    if(json_data.getInt("is_aperta")==0) aa.setAperta(true);
                    array_aula[i] = aa;
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
            array_aule=array_aula;
            if (array_aula == null) {
                ready=1;
                ready_update=0;
                mostraOffline();
            } else {
                ready=0;
                new check_last_update().execute();
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

                        nomeAula_home.setText(item.getNome());
                        luogoAula_home.setText(item.getLuogo());
                        if (item.isAperta())
                            postiLiberi_home.setText("Posti liberi: " + item.getPosti_liberi() + " su " + item.getPosti_totali());
                        else postiLiberi_home.setText("Posti totali: " + item.getPosti_totali());

                        //per gruppi o no
                        if (item.getGruppi() == 0) {
                            flagGruppi_home.setText("Disponibile per i gruppi");
                            immagine_home.setImageResource(R.drawable.group);
                        } else {
                            flagGruppi_home.setText("Non è disponibile per i gruppi");
                            immagine_home.setImageResource(R.drawable.singolo);
                        }
                        //chiusa-aperta
                        if (item.isAperta() == true) {
                            statoAula_home.setTextColor(Color.argb(255, 12, 138, 17));
                            statoAula_home.setText("Attualmente Aperta");
                        } else {
                            statoAula_home.setTextColor(Color.RED);
                            statoAula_home.setText("Attualmente Chiusa");
                        }

                        return convertView;
                    }
                };
                elencoAule.setAdapter(adapter);
            }
        }
    }


//ON RESTART
        protected void onRestart() {
            super.onRestart();
            bar.setVisibility(ProgressBar.VISIBLE);
            new listaAule().execute();
        }

//CREAZIONE MENU IN ALTO
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(Menu.FIRST, 1, Menu.FIRST+3, "Logout");
            menu.add(Menu.FIRST, 2, Menu.FIRST, "Home");
            menu.add(Menu.FIRST, 3, Menu.FIRST+2, "Gestione Gruppi");
            menu.add(Menu.FIRST, 4, Menu.FIRST+1, "Prenotazioni");
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == 1) {
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("logged", false);
                editor.commit();
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
            if (item.getItemId() == 2) {
                Intent i = new Intent(this, Home.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
            if(item.getItemId() == 3){
                Intent i = new Intent(this, GroupActivity.class);
                startActivity(i);
            }
            if(item.getItemId() == 4){
                Intent i = new Intent(this, PrenotazioniAttiveActivity.class);
                startActivity(i);
            }
            return true;
        }

}
