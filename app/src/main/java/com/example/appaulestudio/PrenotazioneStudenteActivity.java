package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
//QUANDO CLICCO SU TAVOLO ESEGUO TASK ASINCRONO
//QUANDO PRENOTO CON SUCCESSO SALVO IN LOCALE

public class PrenotazioneStudenteActivity extends AppCompatActivity {
    static final String URL_TAVOLI="http://pmsc9.altervista.org/progetto/prenotazioneSingolo_tavoli.php";
    static final String URL_PRENOTA="http://pmsc9.altervista.org/progetto/prenotazioneSingolo_prenota.php";

    SubsamplingScaleImageView imgView;
    Spinner spinner;
    ArrayAdapter<Tavolo> adapter;
    TextView txt_data, txt_inizio, txt_fine, txt_nome_aula;
    TableLayout tab_layout;
    Button btn_prenota;
    LinearLayout linear_spinner;

    SqliteManager database;
    Intent intent;
    Bundle bundle;
    Aula aula;
    ArrayList<Orario_Ufficiale> orari_ufficiali;
    ArrayList<Tavolo> tavoli;
    String data_prenotazione, orario_inizio_prenotazione, orario_fine_prenotazione;
    String strMatricola, strNome, strCognome, strUniversita;
    int ingresso;
    boolean aperta=false;
    Tavolo tavolo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazione_studente);
        imgView=findViewById(R.id.img_plant);
        txt_nome_aula=findViewById(R.id.pren_nome_aula);
        txt_data=findViewById(R.id.pren_et_data);
        txt_inizio=findViewById(R.id.pren_et_inizio);
        txt_fine=findViewById(R.id.pren_et_fine);
        tab_layout=findViewById(R.id.pren_tab_layout);
        btn_prenota=findViewById(R.id.pren_btn);
        linear_spinner=findViewById(R.id.linear_spinner);

        database=new SqliteManager(this);
        intent =getIntent();
        bundle=intent.getBundleExtra("dati");
        aula=bundle.getParcelable("aula");
        orari_ufficiali=bundle.getParcelableArrayList("orari");
        Collections.sort(orari_ufficiali);
        txt_nome_aula.setText(aula.getNome());

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        strMatricola=settings.getString("matricola", null);
        strUniversita=settings.getString("universita", null);
        ingresso=Integer.parseInt(settings.getString("ingresso", null));
        setTitle(strNome+" "+strCognome);


        //scarica piantina aula
        new load_image().execute();

        //prende tavoli disponibili
        Calendar c=Calendar.getInstance();
        Date d=c.getTime();
        String date_now=new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(d);
        String time_now =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(d);
        for(int i=0;i<orari_ufficiali.size();i++){
            if(orari_ufficiali.get(i).getData().equals(date_now)){
                 if((orari_ufficiali.get(i).getApertura()==null&&orari_ufficiali.get(i+1).getApertura()==null) || (orari_ufficiali.get(i+1).getApertura()==null&& time_now.compareTo(orari_ufficiali.get(i).getChiusura()) > 0)){
                    //se oggi e domani è chiusa oppure oggi è aperta, domani è chiusa ma oggi siamo oltre orario chiusura
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Il servizio di prenotazione non è disponibile a causa della chiusura dell'aula oggi e domani</b></font>"), Toast.LENGTH_LONG).show();
                    aperta=false;
                }
                else{
                    //se l'aula è chiusa ma aprirà di oggi oppure apre domani
                     new get_tavoli().execute();
                     aperta=true;
                }
                break;
            }
        }

        //funzione bottone prenota
        btn_prenota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new prenota().execute();
            }
        });
    }
//TASK ASINCRONO PRENOTA
    private class prenota extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_PRENOTA);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                String matricola=settings.getString("matricola", null);
                String now=new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(Calendar.getInstance().getTime());
                //if(orario_inizio_prenotazione!=null && now.compareTo(orario_inizio_prenotazione)>0) return "OPS";
                //if(orario_inizio_prenotazione==null && now.compareTo(orario_fine_prenotazione)>=0) return "OPS";
                if(orario_inizio_prenotazione==null) orario_inizio_prenotazione=now;
                String inizio_prenotazione=data_prenotazione+" "+orario_inizio_prenotazione;
                String fine_prenotazione=data_prenotazione+" "+orario_fine_prenotazione;

                String parametri = "id_aula=" + URLEncoder.encode(aula.getIdAula(), "UTF-8") +
                        "&tavolo=" + URLEncoder.encode(""+tavolo.getNum_tavolo(), "UTF-8") +
                        "&inizio_prenotazione=" + URLEncoder.encode(inizio_prenotazione, "UTF-8") +
                        "&fine_prenotazione=" + URLEncoder.encode(fine_prenotazione, "UTF-8") +
                        "&matricola=" + URLEncoder.encode(matricola, "UTF-8") +
                        "&universita=" + URLEncoder.encode(strUniversita, "UTF-8");
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
                return null;
            }
        }
        protected void onPostExecute(String result) {
            if(result==null){//problema di connessione
                MyToast.makeText(getApplicationContext(), "Impossibile contattare il server!", false).show();
                finish();
                return;
            }
            if(result.equals("Impossibile prenotare")){ //qualcuno ha occupato il tavolo al posto tuo
                MyToast.makeText(getApplicationContext(), "Impossibile prenotare! Non ci sono posti disponibili!", false).show();
                finish();
                return;
            }
            if(result.equals("ER")){
                MyToast.makeText(getApplicationContext(), "Impossibile prenotare! Hai già una prenotazione attiva nell'orario specificato!", false).show();
                finish();
                return;
            }
            if(result.equals("OPS")){
                MyToast.makeText(getApplicationContext(), "OPS! Si è verificato un problema! Riprova!", false).show();
                finish();
                return;
            }


            int id_prenotazione=Integer.parseInt(result);
            create_alarm(id_prenotazione);
            database.insertPrenotazione(id_prenotazione,""+strMatricola,data_prenotazione+" "+orario_inizio_prenotazione, ""+aula.getNome(), tavolo.getNum_tavolo(), "null");
            MyToast.makeText(getApplicationContext(), "Prenotazione avvenuta con successo!", true).show();
            Intent i=new Intent(PrenotazioneStudenteActivity.this,PrenotazioniAttiveActivity.class);
            startActivity(i);
            finish();
        }
    }

//creazione alarm
    public void create_alarm(int id_prenotazione){
        //cancel_alarm(id_prenotazione);
        String myTime = data_prenotazione+" "+orario_inizio_prenotazione;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;
        try {
            d = df.parse(myTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.SECOND,ingresso);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("name", ""+aula.getNome()+": Prenotazione terminata");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id_prenotazione, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }


    private class load_image extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                String uri="http://pmsc9.altervista.org/progetto/immagini/plant_"+aula.getIdAula()+".png";
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                return image;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(Bitmap result) {
            if(result==null) return;
            imgView.setImage(ImageSource.bitmap(result));
        }
    }
//TASK ASINCRONO TAVOLI DISPONIBILI
    private class get_tavoli extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
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

                url = new URL(URL_TAVOLI);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2000);
                urlConnection.setConnectTimeout(2000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "id_aula=" + URLEncoder.encode(aula.getIdAula(), "UTF-8")+"&matricola=" + URLEncoder.encode(strMatricola, "UTF-8");
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
                tavoli=new ArrayList<Tavolo>();

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Tavolo t= new Tavolo(aula.getIdAula(),json_data.getInt("num_tavolo"),json_data.getInt("posti_totali"),json_data.getInt("posti_liberi"));
                    if(t.getPosti_liberi()>0) tavoli.add(t);
                }
                return "OK";
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(String result) {
            if(tavoli==null){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile contattare il server!</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }
            if(tavoli.size()==0){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile prenotare! Non ci sono tavoli disponibili</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }

            spinner=findViewById(R.id.spinner_tavoli);
            adapter = new ArrayAdapter(PrenotazioneStudenteActivity.this, android.R.layout.simple_list_item_1, tavoli);
            spinner.setAdapter(adapter);
            linear_spinner.setVisibility(View.VISIBLE);
            tab_layout.setVisibility(View.VISIBLE);
            btn_prenota.setVisibility(View.VISIBLE);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    tavolo = (Tavolo) parent.getItemAtPosition(position);
                    Calendar cal = Calendar.getInstance();
                    Date date = cal.getTime();
                    String dataString = new SimpleDateFormat("yyyy-MM-dd").format(date);
                    String orarioString =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(date);

                    for(int i=0;i<orari_ufficiali.size();i++){
                        if(orari_ufficiali.get(i).getData().equals(dataString)){
                            if(orari_ufficiali.get(i).getChiusura()==null){ //aula chiusa oggi ma domani apre
                                data_prenotazione=orari_ufficiali.get(i+1).getData();
                                orario_inizio_prenotazione=orari_ufficiali.get(i+1).getApertura();
                                orario_fine_prenotazione=orari_ufficiali.get(i+1).getChiusura();
                            }
                            else if(orarioString.compareTo(orari_ufficiali.get(i).getApertura())>=0&&orarioString.compareTo(orari_ufficiali.get(i).getChiusura())<0){ //aula attualmente aperta
                                data_prenotazione=orari_ufficiali.get(i).getData();
                                orario_inizio_prenotazione=null;
                                orario_fine_prenotazione=orari_ufficiali.get(i).getChiusura();
                            }
                            else if(orarioString.compareTo(orari_ufficiali.get(i).getChiusura())>0){ //siamo dopo orario chiusura
                                data_prenotazione=orari_ufficiali.get(i+1).getData();
                                orario_inizio_prenotazione=orari_ufficiali.get(i+1).getApertura();
                                orario_fine_prenotazione=orari_ufficiali.get(i+1).getChiusura();
                            }
                            else if(orarioString.compareTo(orari_ufficiali.get(i).getApertura())<0){ //siamo prima di orario apertura
                                data_prenotazione=orari_ufficiali.get(i).getData();
                                orario_inizio_prenotazione=orari_ufficiali.get(i).getApertura();
                                orario_fine_prenotazione=orari_ufficiali.get(i).getChiusura();
                            }
                            String giorno="";
                            try { giorno=new SimpleDateFormat("E", Locale.ITALY).format(new SimpleDateFormat("yyyy-MM-dd").parse(data_prenotazione));
                            } catch (ParseException e) { }
                            txt_data.setText(giorno.toUpperCase()+" "+data_prenotazione.substring(8,10)+"/"+data_prenotazione.substring(5,7));
                            if(orario_inizio_prenotazione==null) txt_inizio.setText("A prenotazione confermata");
                            else txt_inizio.setText(orario_inizio_prenotazione.substring(0,5));
                            txt_fine.setText(orario_fine_prenotazione.substring(0,5));
                            return;
                        }
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

//ON RESTART
    @Override
    protected void onRestart() {
        super.onRestart();
        linear_spinner.setVisibility(View.GONE);
        tab_layout.setVisibility(View.GONE);
        btn_prenota.setVisibility(View.GONE);
        Calendar c=Calendar.getInstance();
        Date d=c.getTime();
        String date_now=new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(d);
        String time_now =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(d);
        for(int i=0;i<orari_ufficiali.size();i++){
            if(orari_ufficiali.get(i).getData().equals(date_now)){
                if((orari_ufficiali.get(i).getApertura()==null&&orari_ufficiali.get(i+1).getApertura()==null) || (orari_ufficiali.get(i+1).getApertura()==null&& time_now.compareTo(orari_ufficiali.get(i).getChiusura()) > 0)){
                    //se oggi e domani è chiusa oppure oggi è aperta, domani è chiusa ma oggi siamo oltre orario chiusura
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Il servizio di prenotazione non è disponibile a causa della chiusura dell'aula oggi e domani</b></font>"), Toast.LENGTH_LONG).show();
                    aperta=false;
                }
                else{
                    //se l'aula è chiusa ma aprirà di oggi oppure apre domani
                    new get_tavoli().execute();
                    aperta=true;
                }
                break;
            }
        }
    }
}

