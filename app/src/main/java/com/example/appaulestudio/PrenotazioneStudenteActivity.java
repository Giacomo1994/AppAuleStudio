package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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

public class PrenotazioneStudenteActivity extends AppCompatActivity {
    static final String URL_TAVOLI_APERTA="http://pmsc9.altervista.org/progetto/prenotazioneSingolo_tavoli_aulaAperta.php";
    static final String URL_TAVOLI_CHIUSA="http://pmsc9.altervista.org/progetto/prenotazioneSingolo_tavoli_aulaChiusa.php";

    Intent intent;
    Bundle bundle;
    Aula aula;
    ArrayList<Orario_Ufficiale> orari_ufficiali;
    ArrayList<Tavolo> tavoli;
    String data_prenotazione;
    String orario_inizio_prenotazione;
    String orario_fine_prenotazione;

    SubsamplingScaleImageView imgView;
    Spinner spinner;
    ArrayAdapter<Tavolo> adapter;
    TextView txt_data, txt_inizio, txt_fine, txt_nome_aula;
    TableLayout tab_layout;
    Button btn_prenota;
    LinearLayout linear_spinner;

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

        intent =getIntent();
        bundle=intent.getBundleExtra("dati");
        aula=bundle.getParcelable("aula");
        orari_ufficiali=bundle.getParcelableArrayList("orari");
        Collections.sort(orari_ufficiali);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        String strNome=settings.getString("nome", null);
        setTitle(strNome);
        txt_nome_aula.setText(aula.getNome());


        //esecuzione task asincroni
        new load_image().execute(); //caricamento cartina aula

        Calendar c=Calendar.getInstance();
        Date d=c.getTime();
        String date_now=new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(d);
        String time_now =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(d);

        for(int i=0;i<orari_ufficiali.size();i++){
            if(orari_ufficiali.get(i).getData().equals(date_now)){
                if(orari_ufficiali.get(i).getApertura()!=null&&time_now.compareTo(orari_ufficiali.get(i).getApertura()) > 0 && time_now.compareTo(orari_ufficiali.get(i).getChiusura()) < 0)
                    //se in questo momento l'aula è aperta
                    new get_tavoli_aperta().execute();
                else if((orari_ufficiali.get(i).getApertura()==null&&orari_ufficiali.get(i+1).getApertura()==null) || (orari_ufficiali.get(i+1).getApertura()==null&& time_now.compareTo(orari_ufficiali.get(i).getChiusura()) > 0))
                    //se oggi e domani è chiusa oppure oggi è aperta, domani è chiusa ma oggi siamo oltre orario chiusura
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Il servizio di prenotazione non è disponibile a causa della chiusura dell'aula oggi e domani</b></font>"), Toast.LENGTH_LONG).show();
                else
                    //se l'aula è chiusa ma aprirà di oggi oppure apre domani
                    new get_tavoli_chiusa().execute();
                break;
            }
        }

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
            if(result==null){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Errore Immagine</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }
            imgView.setImage(ImageSource.bitmap(result));
        }
    }

    private class get_tavoli_aperta extends AsyncTask<Void, Void, String> {
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

                url = new URL(URL_TAVOLI_APERTA);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2000);
                urlConnection.setConnectTimeout(2000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "id_aula=" + URLEncoder.encode(aula.getIdAula(), "UTF-8");
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
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Errore nella connessione</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }
            if(tavoli.size()==0){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Non ci sono tavoli disponibili</b></font>"), Toast.LENGTH_LONG).show();
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
                    Calendar cal=Calendar.getInstance();
                    Date date=cal.getTime();
                    String dataString=new SimpleDateFormat("yyyy-MM-dd").format(date);
                    for(Orario_Ufficiale of:orari_ufficiali){
                        if(of.getData().equals(dataString)){
                            data_prenotazione=of.getData();
                            String giorno="";
                            try { giorno=new SimpleDateFormat("E", Locale.ITALY).format(new SimpleDateFormat("yyyy-MM-dd").parse(data_prenotazione));
                            } catch (ParseException e) { }
                            txt_data.setText(giorno.toUpperCase()+" "+data_prenotazione.substring(8,10)+"/"+data_prenotazione.substring(5,7));
                            txt_inizio.setText("A prenotazione confermata");
                            orario_fine_prenotazione=of.getChiusura();
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

    private class get_tavoli_chiusa extends AsyncTask<Void, Void, String> {
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

                url = new URL(URL_TAVOLI_CHIUSA);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2000);
                urlConnection.setConnectTimeout(2000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "id_aula=" + URLEncoder.encode(aula.getIdAula(), "UTF-8");
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
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Errore nella connessione</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }
            if(tavoli.size()==0){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Non ci sono tavoli disponibili</b></font>"), Toast.LENGTH_LONG).show();
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
                    Calendar cal=Calendar.getInstance();
                    Date date=cal.getTime();
                    String dataString=new SimpleDateFormat("yyyy-MM-dd").format(date);
                    String orarioString =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(date);
                    for(int i=0;i<orari_ufficiali.size();i++){
                        if(orari_ufficiali.get(i).getData().equals(dataString)){
                            if(orari_ufficiali.get(i).getChiusura()==null||orarioString.compareTo(orari_ufficiali.get(i).getChiusura())>0){
                                data_prenotazione=orari_ufficiali.get(i+1).getData();
                                orario_inizio_prenotazione=orari_ufficiali.get(i+1).getApertura();
                                orario_fine_prenotazione=orari_ufficiali.get(i+1).getChiusura();
                            }
                            else if(orarioString.compareTo(orari_ufficiali.get(i).getApertura())<0){
                                data_prenotazione=orari_ufficiali.get(i).getData();
                                orario_inizio_prenotazione=orari_ufficiali.get(i).getApertura();
                                orario_fine_prenotazione=orari_ufficiali.get(i).getChiusura();
                            }
                            String giorno="";
                            try { giorno=new SimpleDateFormat("E", Locale.ITALY).format(new SimpleDateFormat("yyyy-MM-dd").parse(data_prenotazione));
                            } catch (ParseException e) { }
                            txt_data.setText(giorno.toUpperCase()+" "+data_prenotazione.substring(8,10)+"/"+data_prenotazione.substring(5,7));
                            txt_inizio.setText(orario_inizio_prenotazione.substring(0,5));
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



}

