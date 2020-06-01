package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
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
import android.widget.ImageView;
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
    static final String URL_PRENOTA="http://pmsc9.altervista.org/progetto/prenotazioneSingolo_gruppi_prenota.php";

    SubsamplingScaleImageView imgView;
    Spinner spinner;
    ArrayAdapter<Tavolo> adapter;
    TextView txt_data, txt_inizio, txt_fine, txt_nome_aula, txt_errore;
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
    String strMatricola, strNome, strCognome, strUniversita, strNomeUniversita, FIRST_SLOT;
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
        txt_errore=findViewById(R.id.txt_er_studgruppo);
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
        strNomeUniversita=settings.getString("nome_universita", null);
        ingresso=Integer.parseInt(settings.getString("ingresso", null));
        pausa=Integer.parseInt(settings.getString("pausa", null));
        slot_min=Integer.parseInt(settings.getString("slot", null));
        FIRST_SLOT=settings.getString("first_slot", null);
        action_bar();

        initDateTime(); //ok
        new load_image().execute(); //ok
        if(aperta==true){
            new getTavoli().execute();
        }
        else{
            MyToast.makeText(getApplicationContext(), "Il servizio di prenotazione non è disponibile a causa della chiusura dell'aula oggi e domani!", false).show();
            linear_spinner.setVisibility(View.GONE);
            tab_layout.setVisibility(View.GONE);
            btn_prenota.setVisibility(View.GONE);
            return;
        }
        btn_prenota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new prenota().execute();
            }
        });
    }

    @SuppressLint("WrongConstant")
    public void action_bar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar =view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText("Prenotazione");
        final Dialog d = new Dialog(PrenotazioneStudenteAulaGruppoActivity.this);
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
                Intent i = new Intent(PrenotazioneStudenteAulaGruppoActivity.this, MainActivity.class);
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
                linear_spinner.setVisibility(View.GONE);
                tab_layout.setVisibility(View.GONE);
                btn_prenota.setVisibility(View.GONE);
                return;
            }
            //ok
            for(Tavolo tav:list_tavoli_spinner){
                getSlot();
                for(String sl:slot){
                    int posti_liberi=tav.getPosti_totali();
                    for(Prenotazione pp: prenotazioni){
                        if(pp.getOrario_prenotazione().substring(11,19).compareTo(sl)<=0 && pp.getOrario_fine_prenotazione().substring(11,19).compareTo(sl)>0 && pp.getNum_tavolo()==tav.getNum_tavolo())
                            posti_liberi--;
                    }
                    //MyToast.makeText(getApplicationContext(),sl+" "+posti_liberi,false).show();
                    if(posti_liberi==0){
                        tav.setFine_disponibilita(sl);
                        break;
                    }
                }
                if(slot.get(0).equals(apertura)) tav.setInizio_disponibilita(apertura);
                else tav.setInizio_disponibilita("A prenotazione confermata");
                if(tav.getFine_disponibilita()==null) tav.setFine_disponibilita(slot.getLast());
            }

            Collections.sort(list_tavoli_spinner,Collections.<Tavolo>reverseOrder());

            adapter = new ArrayAdapter(PrenotazioneStudenteAulaGruppoActivity.this, android.R.layout.simple_list_item_1, list_tavoli_spinner);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    tab_layout.setVisibility(View.VISIBLE);
                    btn_prenota.setVisibility(View.VISIBLE);
                    txt_errore.setVisibility(View.GONE);

                    tavolo = (Tavolo) parent.getItemAtPosition(position);
                    getSlot();

                    if(tavolo.getInizio_disponibilita().equals("A prenotazione confermata")){
                        Calendar c=Calendar.getInstance();
                        Date d=c.getTime();
                        String time_now=new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(d);
                        if(time_now.compareTo(tavolo.getInizio_disponibilita())>=0){
                            tab_layout.setVisibility(View.GONE);
                            btn_prenota.setVisibility(View.GONE);
                            txt_errore.setVisibility(View.VISIBLE);
                            txt_errore.setText("Tavolo non più disponibile");
                            return;
                        }
                    }

                    txt_data.setText(giorno.toUpperCase()+" "+data.substring(8,10)+"/"+data.substring(5,7));
                    if(tavolo.getInizio_disponibilita().equals("A prenotazione confermata")) txt_inizio.setText(tavolo.getInizio_disponibilita());
                    else txt_inizio.setText(tavolo.getInizio_disponibilita().substring(0,5));
                    txt_fine.setText(tavolo.getFine_disponibilita().substring(0,5));
                    inizio=tavolo.getInizio_disponibilita();
                    fine=tavolo.getFine_disponibilita();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

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

                String slots=getSlotIntermedi();
                if(slot==null) return "Errore: il tavolo non è più disponibile per l'orario indicato!";

                String parametri = "id_aula=" + URLEncoder.encode(aula.getIdAula(), "UTF-8") +
                        "&universita=" + URLEncoder.encode(strUniversita, "UTF-8") +
                        "&matricola=" + URLEncoder.encode(strMatricola, "UTF-8") +
                        "&tavolo=" + URLEncoder.encode(""+tavolo.getNum_tavolo(), "UTF-8") +
                        "&posti_tavolo=" + URLEncoder.encode(""+tavolo.getPosti_totali(), "UTF-8") +
                        "&data=" + URLEncoder.encode(data, "UTF-8") +
                        "&inizio=" + URLEncoder.encode(inizio, "UTF-8") +
                        "&fine=" + URLEncoder.encode(fine, "UTF-8") +
                        "&slots=" + URLEncoder.encode(slots, "UTF-8") +
                        "&ingresso=" + URLEncoder.encode(""+ingresso, "UTF-8") +
                        "&pausa=" + URLEncoder.encode(""+pausa, "UTF-8");
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
                MyToast.makeText(getApplicationContext(), "Errore: impossibile contattare il server!", false).show();
                finish();
                return;
            }
            else if(result.equals("Errore: il tavolo non è più disponibile per l'orario indicato!") || result.equals("Impossibile procedere: hai già una prenotazione attiva nell'orario indicato")){
                MyToast.makeText(getApplicationContext(), result, false).show();
                finish();
                return;
            }
            else{
                int id_prenotazione=Integer.parseInt(result);
                String orario_alarm=create_alarm(id_prenotazione);
                //database.insertPrenotazione(id_prenotazione,data+" "+inizio, ""+aula.getNome(), tavolo.getNum_tavolo(), "null");
                //database.insertAlarm(id_prenotazione,orario_alarm);
                //MyToast.makeText(getApplicationContext(), "Prenotazione avvenuta con successo!", true).show();
                Intent i=new Intent(PrenotazioneStudenteAulaGruppoActivity.this,PrenotazioniAttiveActivity.class);
                i.setAction("salva_prenotazione");
                i.putExtra("id_prenotazione", id_prenotazione);
                i.putExtra("orario_prenotazione", data+" "+inizio);
                i.putExtra("nome_aula", aula.getNome());
                i.putExtra("tavolo", tavolo.getNum_tavolo());
                i.putExtra("gruppo", "null");
                i.putExtra("orario_alarm", orario_alarm);
                startActivity(i);
                startActivity(i);
                finish();
            }



        }
    }

    public String create_alarm(int id_prenotazione){
        //cancel_alarm(id_prenotazione);
        String myTime = data+" "+inizio;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = null;
        try {
            d = df.parse(myTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.SECOND,ingresso-300);


        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction("StudyAround");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id_prenotazione, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

        String strOra=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
        return strOra;
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
                else if(orari_ufficiali.get(i).getApertura()==null || time_now.compareTo(orari_ufficiali.get(i).getChiusura()) > 0){
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

        Calendar cc=Calendar.getInstance();
        try { cc.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY).parse(data+" "+FIRST_SLOT)); }
        catch (ParseException e) {}

        Calendar calendar_apertura=Calendar.getInstance();
        try { calendar_apertura.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY).parse(data+" "+apertura)); }
        catch (ParseException e) {}

        Calendar calendar_chiusura=Calendar.getInstance();
        try { calendar_chiusura.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY).parse(data+" "+chiusura)); }
        catch (ParseException e) {}

        Calendar calendar_now=Calendar.getInstance();
        String time_now =new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(calendar_now.getTime());

        Calendar calendar_start=null;
        if(calendar_now.after(calendar_apertura) && calendar_now.before(calendar_chiusura)){
            slot.add(time_now);
            calendar_start=calendar_now;
        }
        else{
            slot.add(apertura);
            calendar_start=calendar_apertura;
        }

           while (true) {
            String s=new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(cc.getTime());
            if(cc.compareTo(calendar_start)<=0){
                cc.add(Calendar.MINUTE, slot_min);
                continue;
            }
            else if(cc.compareTo(calendar_chiusura)<=0){
                slot.add(s);
                cc.add(Calendar.MINUTE, slot_min);
            }
            else break;
        }

    }

    public String getSlotIntermedi(){
        LinkedList<String> slotIntermedi=new LinkedList<String>();
        String result=null;
        if(inizio.equals("A prenotazione confermata")){
            String time_now=new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(Calendar.getInstance().getTime());
            if(time_now.compareTo(fine)>=0) return result;
            else inizio = time_now;
        }

        Calendar cc=Calendar.getInstance();
        try { cc.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY).parse(data+" "+FIRST_SLOT)); }
        catch (ParseException e) {}

        Calendar calendar_inizio=Calendar.getInstance();
        try { calendar_inizio.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY).parse(data+" "+inizio)); }
        catch (ParseException e) {}

        Calendar calendar_fine=Calendar.getInstance();
        try { calendar_fine.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY).parse(data+" "+fine)); }
        catch (ParseException e) {}


        while (true) {
            String s=new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(cc.getTime());
            if(cc.compareTo(calendar_inizio)<=0){
                cc.add(Calendar.MINUTE, slot_min);
                continue;
            }
            else if(s.compareTo(fine)<0){
                slotIntermedi.add(s);
                cc.add(Calendar.MINUTE, slot_min);
            }
            else break;
        }

        if(slotIntermedi.size()==0) result = "void";
        else{
            result="";
            for(String s:slotIntermedi){
                result+=(s+",");
            }
            result=result.substring(0,result.length()-1);
        }
        return result;
    }


}
