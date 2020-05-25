package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.LinkedList;
import java.util.Locale;

public class PrenotazioneStudenteAulaGruppoActivity extends AppCompatActivity {

    static final String URL_TAVOLI="http://pmsc9.altervista.org/progetto/prenotazioneSingolo_gruppi_tavoli.php";
    static final String URL_PRENOTAZIONI="http://pmsc9.altervista.org/progetto/prenotazioneSingolo_gruppi_prenotazioni.php";

    SubsamplingScaleImageView imgView;
    Spinner spinner;
    ArrayAdapter<Tavolo> adapter;
    TextView txt_data, txt_inizio, txt_fine, txt_nome_aula;
    TableLayout tab_layout;
    Button btn_prenota;
    LinearLayout linear_spinner, linear_activity;

    SqliteManager database;
    Intent intent;
    Bundle bundle;
    Aula aula=null;

    ArrayList<Orario_Ufficiale> orari_ufficiali;
    ArrayList<Tavolo> tavoli;
    LinkedList<Prenotazione> prenotazioni;
    LinkedList<String> slot;

    String data=null, giorno=null, apertura=null, chiusura=null;
    String inizio=null, fine=null;
    String strMatricola, strNome, strCognome, strUniversita;
    int ingresso, pausa, slot_min;
    boolean aperta=false;
    Tavolo tavolo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazione_studente_aula_gruppo);
        imgView=findViewById(R.id.img_plant_studgruppo);
        txt_nome_aula=findViewById(R.id.pren_nome_aula_studgruppo);
        txt_data=findViewById(R.id.pren_et_data_studgruppo);
        txt_inizio=findViewById(R.id.pren_et_inizio_studgruppo);
        txt_fine=findViewById(R.id.pren_et_fine_studgruppo);
        tab_layout=findViewById(R.id.pren_tab_layout_studgruppo);
        btn_prenota=findViewById(R.id.pren_btn_studgruppo);
        linear_spinner=findViewById(R.id.linear_spinner_studgruppo);
        linear_activity=findViewById(R.id.ll_studgruppo);
        spinner=findViewById(R.id.spinner_tavoli_studgruppo);

        slot=new LinkedList<String>();
        prenotazioni=new LinkedList<Prenotazione>();
        tavoli=null;

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
        pausa=Integer.parseInt(settings.getString("pausa", null));
        slot_min=Integer.parseInt(settings.getString("slot", null));
        setTitle(strNome+" "+strCognome);

        initDateTime(); //ok
        new load_image().execute(); //ok
        if(aperta==true){
            new getTavoli().execute();
        }
        else{
            MyToast.makeText(getApplicationContext(), "Il servizio di prenotazione non è disponibile a causa della chiusura dell'aula oggi e domani!", false).show();
            linear_activity.removeAllViews();
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
            if(result==null) return;
            imgView.setImage(ImageSource.bitmap(result));
        }
    }

    private class getTavoli extends AsyncTask<Void, Void, ArrayList<Tavolo>> {
        @Override
        protected ArrayList<Tavolo> doInBackground(Void... voids) {
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
                ArrayList<Tavolo> tables = new ArrayList<Tavolo>();

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Tavolo t = new Tavolo(aula.getIdAula(), json_data.getInt("num_tavolo"), json_data.getInt("posti_totali"), -1);
                    tables.add(t);
                }
                return tables;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(ArrayList<Tavolo> result) {
            if (result == null) {//problema di connessione
                MyToast.makeText(getApplicationContext(), "Errore: impossibile contattare il server!", false).show();
                finish();
                return;
            }
            tavoli=result;
            new getPrenotazioni().execute();
        }
    }

    private class getPrenotazioni extends AsyncTask<Void, Void, LinkedList<Prenotazione>> {
        @Override
        protected LinkedList<Prenotazione> doInBackground(Void... voids) {
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

                url = new URL(URL_PRENOTAZIONI);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2000);
                urlConnection.setConnectTimeout(2000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "id_aula=" + URLEncoder.encode(aula.getIdAula(), "UTF-8") +
                            "&data=" + URLEncoder.encode(data, "UTF-8") +
                            "&ingresso=" + URLEncoder.encode(""+ingresso, "UTF-8") +
                            "&pausa=" + URLEncoder.encode(""+pausa, "UTF-8");
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

                LinkedList<Prenotazione> prenotaziones = new LinkedList<Prenotazione>();
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    prenotaziones.add(new Prenotazione(json_data.getInt("id"), "null","null", "null",json_data.getInt("tavolo"),
                            ""+json_data.getString("orario_prenotazione"),
                            ""+json_data.getString("orario_ultima_uscita"),
                            ""+json_data.getString("orario_fine_prenotazione"),json_data.getInt("stato"),"null","null"));
                }
                return prenotaziones;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(LinkedList<Prenotazione> result) {
            if (result == null) {//problema di connessione
                MyToast.makeText(getApplicationContext(), "Errore: impossibile contattare il server!", false).show();
                linear_activity.removeAllViews();
                return;
            }
            prenotazioni=result;

            Calendar c=Calendar.getInstance();
            Date d=c.getTime();
            String datetime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY).format(d);
            if(datetime.compareTo(data+" "+apertura)<=0){
                datetime=data+" "+apertura;
            }
            ArrayList<Tavolo> list_tavoli_spinner=new ArrayList<Tavolo>();
            for(Tavolo t:tavoli){
                int posti_occupati=0;
                for(Prenotazione p: prenotazioni){
                    if(p.getNum_tavolo()==t.getNum_tavolo() && datetime.compareTo(p.getOrario_prenotazione())>=0) posti_occupati++;
                }
                int posti_liberi=t.getPosti_totali()-posti_occupati;
                if(posti_liberi>0) list_tavoli_spinner.add(t);
            }
            if(list_tavoli_spinner.size()==0){
                MyToast.makeText(getApplicationContext(), "Impossibile prenotare! Non ci sono posti disponibili!", false).show();
                linear_activity.removeAllViews();
                return;
            }
            //ok
            adapter = new ArrayAdapter(PrenotazioneStudenteAulaGruppoActivity.this, android.R.layout.simple_list_item_1, list_tavoli_spinner);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    tavolo = (Tavolo) parent.getItemAtPosition(position);
                    getSlot();

                    for(String sl:slot){
                        int posti_liberi=tavolo.getPosti_totali();
                        for(Prenotazione pp: prenotazioni){
                            if(pp.getOrario_prenotazione().substring(11,19).compareTo(sl)<=0 && pp.getOrario_fine_prenotazione().substring(11,19).compareTo(sl)>0 && pp.getNum_tavolo()==tavolo.getNum_tavolo())
                                posti_liberi--;
                        }
                        //MyToast.makeText(getApplicationContext(),sl+" "+posti_liberi,false).show();
                        if(posti_liberi==0){
                            fine=sl.substring(0,5);
                            break;
                        }
                    }
                    if(slot.get(0).equals(apertura)) inizio=apertura.substring(0,5);
                    else inizio="A prenotazione confermata";
                    if(fine==null) fine=slot.getLast().substring(0,5);

                    txt_data.setText(giorno.toUpperCase()+" "+data.substring(8,10)+"/"+data.substring(5,7));
                    txt_inizio.setText(inizio);
                    txt_fine.setText(fine);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    public void initDateTime(){
        Calendar c=Calendar.getInstance();
        Date d=c.getTime();
        String date_now=new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(d);
        String time_now =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(d);
        for(int i=0;i<orari_ufficiali.size();i++){
            if(orari_ufficiali.get(i).getData().equals(date_now)){
                if((orari_ufficiali.get(i).getApertura()==null&&orari_ufficiali.get(i+1).getApertura()==null) || (orari_ufficiali.get(i+1).getApertura()==null && time_now.compareTo(orari_ufficiali.get(i).getChiusura()) > 0)){
                    //se oggi e domani è chiusa oppure oggi è aperta, domani è chiusa ma oggi siamo oltre orario chiusura
                    aperta=false;
                }
                else if(time_now.compareTo(orari_ufficiali.get(i).getChiusura()) > 0 || orari_ufficiali.get(i).getApertura()==null){
                    data=orari_ufficiali.get(i+1).getData();
                    apertura=orari_ufficiali.get(i+1).getApertura();
                    chiusura=orari_ufficiali.get(i+1).getChiusura();
                    giorno=orari_ufficiali.get(i+1).getGiorno();
                    aperta=true;
                }
                else{
                    data=orari_ufficiali.get(i).getData();
                    apertura=orari_ufficiali.get(i).getApertura();
                    chiusura=orari_ufficiali.get(i).getChiusura();
                    giorno=orari_ufficiali.get(i).getGiorno();
                    aperta=true;
                }
                break;
            }
        }
    }

    public void getSlot(){
        slot.clear();
        String string_start="";
        String date_now =new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(Calendar.getInstance().getTime());
        String time_now =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(Calendar.getInstance().getTime());

        if(!date_now.equals(data)) string_start=apertura;
        else if(time_now.compareTo(apertura)>0 && time_now.compareTo(chiusura)<0) string_start=time_now;
        else string_start=apertura;
        slot.add(string_start);

        Calendar calendar_start=Calendar.getInstance();
        try { calendar_start.setTime(new SimpleDateFormat("HH:mm:ss", Locale.ITALY).parse(apertura)); }
        catch (ParseException e) {}

        while (true) {
            String s=new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(calendar_start.getTime());
            if(s.compareTo(string_start)<0){
                calendar_start.add(Calendar.MINUTE, slot_min);
                continue;
            }
            else if(s.compareTo(chiusura)<=0){
                slot.add(s);
                calendar_start.add(Calendar.MINUTE, slot_min);
            }
            else break;
        }
    }


}
