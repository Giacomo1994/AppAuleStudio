package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Home extends AppCompatActivity{
    static final String URL_AULE_DINAMICHE="http://pmsc9.altervista.org/progetto/home_info_dinamiche_aule.php";
    static final String URL_ORARI_DEFAULT="http://pmsc9.altervista.org/progetto/home_orari_default.php";
    static final String URL_LAST_UPDATE="http://pmsc9.altervista.org/progetto/home_lastUpdate.php";

    LinearLayout ll_start,ll_home;
    ArrayAdapter adapter;
    ListView elencoAule;
    TextView nomeAula_home,luogoAula_home,postiLiberi_home,flagGruppi_home, statoAula_home;
    ImageView immagine_home;
    Button mappa;
    ProgressBar bar;

    Aula[] array_aule=null;
    ArrayList<Aula> aule_da_aggiornare=new ArrayList<Aula>();
    boolean from_login=true, start_app=false;
    int ready=-1;

    Intent intent;
    String strUniversita, strNomeUniversita, strMatricola, strNome, strCognome;
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
        strNomeUniversita=settings.getString("nome_universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        action_bar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initUI();
        database=new SqliteManager(Home.this);

        //intent
        intent=getIntent();
        if(intent.hasExtra("start_from_login")) start_app=true;
        if(intent.hasExtra("start_from_login") && intent.getBooleanExtra("start_from_login",true)==true) from_login=true;
        else from_login=false;

        //if(from_login==false)
        if(start_app==true){
            getSupportActionBar().hide();
            new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if(ready!=-1 && millisUntilFinished<28000){
                        ll_start.setVisibility(View.GONE);
                        ll_home.setVisibility(View.VISIBLE);
                        if(ready==1) MyToast.makeText(getApplicationContext(), "Sei offline! I dati delle aule potrebbero non essere aggiornati!",false).show();
                        getSupportActionBar().show();
                        cancel();
                    }
                }
                public void onFinish() {
                    //schermata_iniziale_gone=true;
                    if(ready==0){
                        ll_home.setVisibility(View.VISIBLE);
                        ll_start.setVisibility(View.GONE);
                        getSupportActionBar().show();
                    }
                    else if(ready==1){
                        ll_home.setVisibility(View.VISIBLE);
                        ll_start.setVisibility(View.GONE);
                        getSupportActionBar().show();
                        MyToast.makeText(getApplicationContext(), "Sei offline! I dati delle aule potrebbero non essere aggiornati!",false).show();
                    }
                }
            }.start();
        }
        else{
            ll_start.setVisibility(View.GONE);
            ll_home.setVisibility(View.VISIBLE);
        }

        new check_last_update_universita().execute();
        new listaAule().execute();

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
    }

    protected void onRestart() {
        super.onRestart();
        bar.setVisibility(ProgressBar.VISIBLE);
        new listaAule().execute();
        new check_last_update_universita().execute();
    }

    @SuppressLint("WrongConstant")
    private void action_bar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar =view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText("Home");
        final Dialog d = new Dialog(Home.this);
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
                Intent i = new Intent(Home.this, MainActivity.class);
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

    //se non c'è connessione mostra nella listview i dati da SQLITE
    private void mostraOffline(){
        ArrayList<Aula> aule=database.readListaAule();
        if(aule==null){
            MyToast.makeText(getApplicationContext(), "Errore: impossibile mostrare le aule!", false).show();
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
    private class check_last_update_universita extends AsyncTask<Void, Void, Universita> {
        @Override
        protected Universita doInBackground(Void... voide) {
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
                Universita uni = null;
                for (int i = 0; i < jArrayLastUpdate.length(); i++) {
                    JSONObject json_data = jArrayLastUpdate.getJSONObject(i);
                    uni = new Universita(json_data.getString("codice"), json_data.getString("nome"),
                            json_data.getDouble("latitudine"), json_data.getDouble("longitudine"),
                            json_data.getInt("ingresso"), json_data.getInt("pausa"),
                            json_data.getInt("slot"), json_data.getString("first_slot"),
                            json_data.getString("url_registrazione"),json_data.getString("url_corsi"));
                    uni.setLast_update(json_data.getString("last_update"));
                }

                return uni;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(Universita universita) {
            if(universita==null) return;
            else {
                //new aggiornaSQLITE().execute();
                //if(from_login==false) new aggiornaPreferenzeUniversita().execute();
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("latitudine", ""+universita.getLatitudine());
                editor.putString("longitudine", ""+universita.getLongitudine());
                editor.putString("ingresso", ""+universita.getIngresso());
                editor.putString("pausa", ""+universita.getPausa());
                editor.putString("slot", ""+universita.getSlot());
                editor.putString("first_slot", universita.getFirst_slot());
                editor.putString("last_update", universita.getLast_update());
                editor.putString("last_update", universita.getLast_update());
                editor.commit();
            }
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
                    aa.setLast_update(json_data.getString("last_update"));
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
                if(start_app==false) MyToast.makeText(getApplicationContext(),"Sei offline! I dati delle aule potrebbero non essere aggiornati", false).show();
                ready=1;
                mostraOffline();
            } else {
                database.delete_aule_offline(array_aula);
                aule_da_aggiornare=database.getAuleDaAggiornare(array_aula);
                ready=0;
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

                if(aule_da_aggiornare.size()!=0){
                    for(Aula a:aule_da_aggiornare){
                        database.aggiornaAula(a);
                    }
                    new salva_orari_offline().execute();
                }
            }
        }
    }

    private class salva_orari_offline extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url=new URL(URL_ORARI_DEFAULT);
                for(Aula a:array_aule){
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(1500);
                    urlConnection.setConnectTimeout(1000);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    String parametri = "id_aula=" + URLEncoder.encode(a.getIdAula(), "UTF-8");
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
                    JSONArray jArrayOrariDefault = new JSONArray(result);
                    HashMap<Integer,Orario> orari_default=new HashMap<Integer, Orario>();
                    for (int i = 0; i < jArrayOrariDefault.length(); i++) {
                        JSONObject json_data = jArrayOrariDefault.getJSONObject(i);
                        int giorno=json_data.getInt("giorno");
                        String apertura=json_data.getString("apertura");
                        String chiusura=json_data.getString("chiusura");
                        orari_default.put(giorno, new Orario(apertura,chiusura));
                    }
                    database.aggiornaOrariAula(a,orari_default);
                }
                return "ok";
            } catch (Exception e) { return null;}
        }
        protected void onPostExecute(String result) {
            //if(result!=null) MyToast.makeText(getApplicationContext(),result,true).show();
        }
    }


    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 2, Menu.FIRST, "Aggiorna");
        menu.add(Menu.FIRST, 3, Menu.FIRST+2, "Gestione Gruppi");
        menu.add(Menu.FIRST, 4, Menu.FIRST+1, "Prenotazioni");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 2) {
            Intent i = new Intent(this, Home.class);
            startActivity(i);
            finish();
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
