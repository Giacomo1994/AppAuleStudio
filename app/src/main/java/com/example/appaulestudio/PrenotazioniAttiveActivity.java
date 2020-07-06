package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import com.itextpdf.text.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class PrenotazioniAttiveActivity extends AppCompatActivity {
    static final String URL_PRENOTAZIONI="http://pmsc9.altervista.org/progetto/prenotazioniAttive.php";
    static final String URL_OPERAZIONI="http://pmsc9.altervista.org/progetto/prenotazioniAttive_gestionePrenotazione.php";
    static final String URL_RICHIESTA_TORNELLO="http://pmsc9arduino.altervista.org/inserisci_richiesta.php";

    LinearLayout ll_in_corso,ll_cronologia;
    ListView list_in_corso, list_cronologia;
    ArrayAdapter<Prenotazione> adapter;
    TextView txt_legenda;
    LinearLayout ll_offline;

    SqliteManager database;
    Intent intent_ricevuto;
    //IntentIntegrator qrScan;
    Prenotazione p=null;
    int richiesta=-1, ingresso, pausa;
    String strUniversita, strNomeUniversita, strMatricola,strNome, strCognome;
    boolean  offline;
    ArrayList<CalendarAccount> array_list_account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazioni_attive);
        ll_in_corso=findViewById(R.id.prenInCorso_ll);
        ll_cronologia=findViewById(R.id.prenCronologia_ll);
        list_in_corso=findViewById(R.id.list_inCorso);
        list_cronologia=findViewById(R.id.list_cronologia);
        txt_legenda=findViewById(R.id.text_legenda);
        ll_offline=findViewById(R.id.ll_prenattive_offline);
        ll_offline.setVisibility(View.GONE);
        txt_legenda.setVisibility(View.VISIBLE);
        array_list_account = new ArrayList<CalendarAccount>();

        //legenda
        String stringa="Legenda";
        SpannableString ss=new SpannableString(stringa);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final Dialog d = new Dialog(PrenotazioniAttiveActivity.this);
                d.setCancelable(true);
                d.setContentView(R.layout.dialog_legenda);
                d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                d.show();
            }
        };

        ss.setSpan(clickableSpan1, 0,7,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt_legenda.setText(ss);
        txt_legenda.setMovementMethod(LinkMovementMethod.getInstance());

        //sqlite e qrscan
        database=new SqliteManager(PrenotazioniAttiveActivity.this);
        //qrScan = new IntentIntegrator(this);

        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strNomeUniversita = settings.getString("nome_universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        ingresso=Integer.parseInt(settings.getString("ingresso", null))-300;
        pausa=Integer.parseInt(settings.getString("pausa", null))-300;
        offline=settings.getBoolean("offline",false);
        action_bar();

        //intent
        intent_ricevuto=getIntent();
        if(intent_ricevuto.getAction()!=null && intent_ricevuto.getAction().equals("salva_prenotazione")){
            int id_prenotazione_intent=intent_ricevuto.getIntExtra("id_prenotazione",-1);
            String orario_prenotazione_intent=intent_ricevuto.getStringExtra("orario_prenotazione");
            String nome_aula_intent=intent_ricevuto.getStringExtra("nome_aula");
            int tavolo_intent=intent_ricevuto.getIntExtra("tavolo",-1);
            String gruppo_intent=intent_ricevuto.getStringExtra("gruppo");
            //String orario_alarm_intent=intent_ricevuto.getStringExtra("orario_alarm");
            database.insertPrenotazione(id_prenotazione_intent,orario_prenotazione_intent, nome_aula_intent, tavolo_intent, gruppo_intent);
            String orario_alarm=create_alarm_singolo(id_prenotazione_intent,orario_prenotazione_intent);
            database.insertAlarm(id_prenotazione_intent,orario_alarm);
            MyToast.makeText(getApplicationContext(), "Prenotazione avvenuta con successo!", true).show();
        }

        //task asincrono
        new getPrenotazioni().execute();
        registerForContextMenu(list_in_corso);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ll_offline.setVisibility(View.GONE);
        txt_legenda.setVisibility(View.VISIBLE);
        new getPrenotazioni().execute();
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
        txt_actionbar.setText(getString(R.string.header_prenattive));
        final Dialog d = new Dialog(PrenotazioniAttiveActivity.this);
        d.setCancelable(true);
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
                Intent i = new Intent(PrenotazioniAttiveActivity.this, MainActivity.class);
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

    //TASK ASINCRONO --> Richiesta accesso a tornello
    private class doRichiestaTornello extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_RICHIESTA_TORNELLO);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "id_aula=" + URLEncoder.encode(p.getId_aula(), "UTF-8") + "&richiesta=" + URLEncoder.encode(""+richiesta, "UTF-8");
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
                dos.flush();
                dos.close();
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
        protected void onPostExecute(String result) {}
    }

    ///////TASK ASINCRONO --> Operazione su prenotazione
    private class doOperazione extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_OPERAZIONI);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "id_aula=" + URLEncoder.encode(p.getId_aula(), "UTF-8") +
                        "&richiesta=" + URLEncoder.encode(""+richiesta, "UTF-8") +
                        "&id_prenotazione=" + URLEncoder.encode(""+p.getId_prenotazione(), "UTF-8") +
                        "&matricola=" + URLEncoder.encode(strMatricola, "UTF-8") +
                        "&inizio_prenotazione=" + URLEncoder.encode(p.getOrario_prenotazione(), "UTF-8") +
                        "&fine_prenotazione=" + URLEncoder.encode(p.getOrario_fine_prenotazione(), "UTF-8") +
                        "&gruppo=" + URLEncoder.encode(p.getGruppo(), "UTF-8");
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
                dos.flush();
                dos.close();
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
            if(result==null){ //problema di connessione o perchè qualcuno ha occupato il tavolo al posto tuo
                MyToast.makeText(getApplicationContext(), "Impossibile contattare il server!", false).show();
                return;
            }
            if(result.equals("Accesso non consentito") || result.equals("Impossibile effettuare pausa") || result.equals("Impossibile cancellare prenotazione") || result.equals("Impossibile procedere"))
                MyToast.makeText(getApplicationContext(), result, false).show();
            else MyToast.makeText(getApplicationContext(), result, true).show();

            if(richiesta==0 && result.equals("Accesso consentito")){
                String orario_alarm=create_alarm(p, false, false);
                database.insertAlarm(p.getId_prenotazione(),orario_alarm);
                new doRichiestaTornello().execute();
            }
            else if(richiesta==1 && result.equals("Prenotazione terminata")){
                cancel_alarm(p);
                database.deleteAlarm(p.getId_prenotazione());
            }
            else if(richiesta==2 && result.equals("Pausa iniziata")){
                String orario_alarm=create_alarm(p, false,true);
                database.insertAlarm(p.getId_prenotazione(),orario_alarm);
                new doRichiestaTornello().execute();
            }
            else if(richiesta==3 && result.equals("Prenotazione terminata")){
                cancel_alarm(p);
                database.deleteAlarm(p.getId_prenotazione());
                new doRichiestaTornello().execute();
            }
            else if(richiesta==4 && result.equals("Cancellazione avvenuta con successo")){
                cancel_alarm(p);
                database.deletePrenotazione(p.getId_prenotazione());
                database.deleteAlarm(p.getId_prenotazione());
                ArrayList<CalendarEvent> eventi=database.getEventiFromPrenotazione(p.getId_prenotazione());
                if(eventi!=null){
                    for(CalendarEvent ev:eventi){
                        database.deleteEventoCalendario(p.getId_prenotazione());
                        delete_event(ev);
                    }
                }
            }
            else if((richiesta==5 && result.equals("Entrata consentita")) || (richiesta==6) && result.equals("Uscita consentita")) new doRichiestaTornello().execute();
            else if(richiesta==10 && result.equals("Conferma avvenuta")){
                String orario_alarm=create_alarm(p, false, false);
                database.insertAlarm(p.getId_prenotazione(),orario_alarm);
            }
            new getPrenotazioni().execute();
        }
    }

    //////TASK ASINCRONO --> scarico prenotazioni prenotazioni in corso, future o terminate nella giornata --> Se non c'è connessione prendo i dati da sqlite
    private class getPrenotazioni extends AsyncTask<Void, Void, Prenotazione[]> {
        @Override
        protected Prenotazione[] doInBackground(Void... strings) {
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
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "universita=" + URLEncoder.encode(strUniversita, "UTF-8")+"&matricola=" + URLEncoder.encode(strMatricola, "UTF-8"); //imposto parametri da passare
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

                Prenotazione[] array_prenotazioni = new Prenotazione[jArray.length()];
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    array_prenotazioni[i] = new Prenotazione(json_data.getInt("id"), json_data.getString("matricola"),
                            json_data.getString("id_aula"),json_data.getString("nome"),
                            json_data.getInt("tavolo"),json_data.getString("orario_prenotazione"),
                            json_data.getString("orario_ultima_uscita"),json_data.getString("orario_fine_prenotazione"),
                            json_data.getInt("stato"),json_data.getString("gruppo"), json_data.getString("in_corso"));
                }
                return array_prenotazioni;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Prenotazione[] array_prenotazioni) {
            ll_in_corso.setVisibility(View.VISIBLE);
            ll_cronologia.setVisibility(View.VISIBLE);
            list_cronologia.setAdapter(null);
            list_in_corso.setAdapter(null);
            //offline
            if(array_prenotazioni==null){
                ll_in_corso.setVisibility(View.GONE);
                ll_cronologia.setVisibility(View.VISIBLE);
                ll_offline.setVisibility(View.VISIBLE);
                txt_legenda.setVisibility(View.GONE);

                ArrayList<Prenotazione> prenotazioni_offline=database.selectPrenotazioni();
                if(prenotazioni_offline==null || prenotazioni_offline.size()==0){
                    final Dialog d = new Dialog(PrenotazioniAttiveActivity.this);
                    d.setCancelable(false);
                    d.setContentView(R.layout.dialog_warning);
                    d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                    Button btn=d.findViewById(R.id.btn_dialog_warning);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(PrenotazioniAttiveActivity.this, Home.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            d.dismiss();
                        }
                    });
                    d.show();
                    return;
                }
                Collections.sort(prenotazioni_offline, Collections.<Prenotazione>reverseOrder());

                adapter = new ArrayAdapter<Prenotazione>(PrenotazioniAttiveActivity.this, R.layout.row_layout_prenotazioni_attive_activity, prenotazioni_offline) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        Prenotazione item = getItem(position);
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_prenotazioni_attive_activity, parent, false);
                        TableLayout table_riga=convertView.findViewById(R.id.row_table);
                        TableRow riga_gruppo= convertView.findViewById(R.id.riga_gruppo);
                        TableRow riga_stato= convertView.findViewById(R.id.riga_stato);
                        TableRow riga_fine= convertView.findViewById(R.id.riga_ora_fine);
                        TextView row_luogo = convertView.findViewById(R.id.row_aula_tavolo);
                        TextView row_inizio = convertView.findViewById(R.id.row_inizio);
                        TextView row_gruppo = convertView.findViewById(R.id.row_gruppo);

                        riga_stato.setVisibility(View.GONE);
                        riga_fine.setVisibility(View.GONE);
                        table_riga.setBackgroundResource(R.drawable.forma_dialog);
                        row_luogo.setText(item.getAula()+", Tavolo "+item.getNum_tavolo());
                        row_inizio.setText(item.getOrario_prenotazione().substring(8,10)+"/"+item.getOrario_prenotazione().substring(5,7)+" ore "+item.getOrario_prenotazione().substring(11,16));

                        if(item.getGruppo().equals("null")){
                            riga_gruppo.setVisibility(View.GONE);
                        }
                        else{
                            riga_gruppo.setVisibility(View.VISIBLE);
                            String nome_gruppo=database.selectNomegruppo(item.getGruppo());
                            if(nome_gruppo!=null) row_gruppo.setText(nome_gruppo);
                            else row_gruppo.setText(item.getGruppo());
                        }
                        return convertView;
                    }
                };
                list_cronologia.setAdapter(adapter);
                return;
            }

            //online
            ll_cronologia.setVisibility(View.GONE);
            ll_in_corso.setVisibility(View.VISIBLE);
            ArrayList<Prenotazione> lista_prenotazioni=new ArrayList<Prenotazione>();
            for(Prenotazione p:array_prenotazioni){//p.getIn_corso().equals("futura") &&
                if(p.getStato()==1 && !p.getGruppo().equals("null") && (p.getIn_corso().equals("futura") || p.getIn_corso().equals("in_corso"))){
                    if(database.isAllarmeGiaInserito(p.getId_prenotazione())==false){
                        String orario_alarm=create_alarm(p,true,false);
                        database.insertAlarm(p.getId_prenotazione(),orario_alarm);
                        //MyToast.makeText(getApplicationContext(),"Allarme inserito",true).show();
                    }

                }
                lista_prenotazioni.add(p);
            }
            Collections.sort(lista_prenotazioni);

            if(lista_prenotazioni.size()==0){
                final Dialog d = new Dialog(PrenotazioniAttiveActivity.this);
                d.setCancelable(false);
                d.setContentView(R.layout.dialog_warning);
                d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                Button btn=d.findViewById(R.id.btn_dialog_warning);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(PrenotazioniAttiveActivity.this, Home.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        d.dismiss();
                    }
                });
                d.show();
                return;
            }
            adapter = new ArrayAdapter<Prenotazione>(PrenotazioniAttiveActivity.this, R.layout.row_layout_prenotazioni_attive_activity, lista_prenotazioni) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Prenotazione item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_prenotazioni_attive_activity, parent, false);
                    TableLayout table_riga=convertView.findViewById(R.id.row_table);
                    TableRow riga_gruppo= convertView.findViewById(R.id.riga_gruppo);
                    TableRow riga_fine= convertView.findViewById(R.id.riga_ora_fine);
                    TableRow riga_stato= convertView.findViewById(R.id.riga_stato);
                    TextView row_luogo = convertView.findViewById(R.id.row_aula_tavolo);
                    TextView row_inizio = convertView.findViewById(R.id.row_inizio);
                    TextView row_fine = convertView.findViewById(R.id.row_fine);
                    TextView row_gruppo = convertView.findViewById(R.id.row_gruppo);
                    TextView row_stato = convertView.findViewById(R.id.row_stato);

                    if(item.getIn_corso().equals("in_corso")){
                        table_riga.setBackgroundResource(R.drawable.layout_prenotazione_corso_complete);
                        row_luogo.setText(item.getAula()+", Tavolo "+item.getNum_tavolo());
                        row_inizio.setText(item.getOrario_prenotazione().substring(8,10)+"/"+item.getOrario_prenotazione().substring(5,7)+" ore "+item.getOrario_prenotazione().substring(11,16));
                        row_fine.setText(item.getOrario_fine_prenotazione().substring(8,10)+"/"+item.getOrario_fine_prenotazione().substring(5,7)+" ore "+item.getOrario_fine_prenotazione().substring(11,16));
                        if(item.getGruppo().equals("null")) riga_gruppo.setVisibility(View.GONE);
                        else{
                            riga_gruppo.setVisibility(View.VISIBLE);
                            String nome_gruppo=database.selectNomegruppo(item.getGruppo());
                            if(nome_gruppo!=null) row_gruppo.setText(nome_gruppo);
                            else row_gruppo.setText(item.getGruppo());
                        }
                        if(item.getStato()==1) row_stato.setText("Non ancora in aula");
                        else if(item.getStato()==2) row_stato.setText("In pausa");
                        else row_stato.setText("In aula");
                    }

                    else if(item.getIn_corso().equals("futura")){
                        table_riga.setBackgroundResource(R.drawable.layout_prenotazione_futura_complete);
                        row_luogo.setText(item.getAula()+", Tavolo "+item.getNum_tavolo());
                        row_inizio.setText(item.getOrario_prenotazione().substring(8,10)+"/"+item.getOrario_prenotazione().substring(5,7)+" ore "+item.getOrario_prenotazione().substring(11,16));
                        row_fine.setText(item.getOrario_fine_prenotazione().substring(8,10)+"/"+item.getOrario_fine_prenotazione().substring(5,7)+" ore "+item.getOrario_fine_prenotazione().substring(11,16));

                        if(item.getGruppo().equals("null")) riga_gruppo.setVisibility(View.GONE);
                        else{
                            riga_gruppo.setVisibility(View.VISIBLE);
                            String nome_gruppo=database.selectNomegruppo(item.getGruppo());
                            if(nome_gruppo!=null) row_gruppo.setText(nome_gruppo);
                            else row_gruppo.setText(item.getGruppo());
                        }
                        row_stato.setText("Non ancora iniziata");
                    }

                    else{
                        table_riga.setBackgroundResource(R.drawable.layout_prenotazione_terminata_complete);
                        riga_fine.setVisibility(View.GONE);
                        row_luogo.setText(item.getAula() + ", Tavolo " + item.getNum_tavolo());
                        row_inizio.setText(item.getOrario_prenotazione().substring(8, 10) + "/" + item.getOrario_prenotazione().substring(5, 7) + " ore " + item.getOrario_prenotazione().substring(11, 16));
                        if (item.getGruppo().equals("null")) riga_gruppo.setVisibility(View.GONE);
                        else {
                            riga_gruppo.setVisibility(View.VISIBLE);
                            String nome_gruppo=database.selectNomegruppo(item.getGruppo());
                            if(nome_gruppo!=null) row_gruppo.setText(nome_gruppo);
                            else row_gruppo.setText(item.getGruppo());
                        }
                        row_stato.setText("Terminata");
                    }
                    return convertView;
                }
            };
            list_in_corso.setAdapter(adapter);
            database.insertPrenotazioni(lista_prenotazioni);
        }
    }

    //////CALENDARIO
    //richiesta permessi per sincronizzare dopo chiama get_account_from_calendar
    public void sincronizza() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) MyToast.makeText(getApplicationContext(),"Non puoi accedere al calendario! Hai negato il permesso!",false).show();
            else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR}, 1);
        } else dialog_pick_calendar(get_account_from_calendar());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 1) dialog_pick_calendar(get_account_from_calendar());
            else if(requestCode==2) startScan();
        } else
            if(requestCode==1) MyToast.makeText(getApplicationContext(), "Non puoi accedere ai calendari! Hai negato il permesso!", false).show();
            else if(requestCode==2 ) MyToast.makeText(getApplicationContext(), "Non puoi accedere alla camera! Hai negato il permesso!", false).show();
    }

    //dialog per scegliere tra i vari account del calendario
    public void dialog_pick_calendar(final ArrayList<CalendarAccount> lista_account) {
        if(lista_account==null || lista_account.size()==0){
            MyToast.makeText(getApplicationContext(), "Impossibile sincronizzare: non hai nessun calendario!",false).show();
            return;
        }

        final Dialog dialog = new Dialog(PrenotazioniAttiveActivity.this);
        dialog.setTitle("Seleziona account");
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_seleziona_calendario);
        ListView lv_account = dialog.findViewById(R.id.lv_account);
        Button conferma = dialog.findViewById(R.id.btn_account_conferma);
        Button indietro = dialog.findViewById(R.id.btn_account_indietro);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);

        ArrayAdapter<CalendarAccount> adapter_calendar = new ArrayAdapter<CalendarAccount>(PrenotazioniAttiveActivity.this, R.layout.row_layout_dialog_calendario, lista_account) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final CalendarAccount item = getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_dialog_calendario, parent, false);
                CheckBox check_cal = convertView.findViewById(R.id.check_account);
                if(item.getId()==1) check_cal.setText(item.getName_account());
                else check_cal.setText(item.getName());
                check_cal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked == true) array_list_account.add(item);
                        else {
                            if (array_list_account.contains(item)) array_list_account.remove(item);
                        }
                    }
                });
                return convertView;
            }
        };
        lv_account.setAdapter(adapter_calendar);

        conferma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (array_list_account.size() == 0)
                    MyToast.makeText(getApplicationContext(), "Selezionare un account", true).show();
                else {
                    boolean sync_success = write_event();
                    if (sync_success == true)
                        MyToast.makeText(getApplicationContext(), "Sincronizzazione effettuata", true).show();
                    else
                        MyToast.makeText(getApplicationContext(), "Impossibile sincronizzare", false).show();
                    dialog.dismiss();
                    array_list_account.clear();
                }
            }
        });

        indietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                array_list_account.clear();
            }
        });
        dialog.show();
        return;
    }
    //Uso CalendarContract.Calendars, classe Contract
    //Uso tabelle .Calendars, .Events, .Reminders
    public ArrayList<CalendarAccount> get_account_from_calendar() {
        ArrayList<CalendarAccount> lista = new ArrayList<CalendarAccount>();
        try {
            //recupero calendari disponibili tramite URI
            Cursor cursor = getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID));
                String accountName = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME));
                String name = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.NAME));
                String type = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE));
                String owner=cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT));

                if(id==1 && !accountName.contains("@")) lista.add(new CalendarAccount(id, name, accountName, type, owner));
                else if(name.contains("@")) lista.add(new CalendarAccount(id, name, accountName, type, owner));
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return lista;
    }

    //inserisco la prenotazione nel calendario selezionato
    public boolean write_event() {
        for (CalendarAccount c : array_list_account) {
            ContentResolver cr = getContentResolver();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date_inizio = null;
            Date date_fine = null;
            try {
                date_inizio = df.parse(p.getOrario_prenotazione());
                date_fine = df.parse(p.getOrario_fine_prenotazione());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date_inizio == null || date_fine == null) return false;

            Calendar cal_begin = Calendar.getInstance();
            cal_begin.setTime(date_inizio);
            Calendar cal_end = Calendar.getInstance();
            cal_end.setTime(date_fine);

            ContentValues values = new ContentValues();
            //inserisco l'evento sfruttando l'id del calendario ove voglio inserirlo
            //inserisco l'allarme
            values.put(CalendarContract.Events.DTSTART, cal_begin.getTimeInMillis());
            values.put(CalendarContract.Events.DTEND, cal_end.getTimeInMillis());
            values.put(CalendarContract.Events.TITLE, "StudyAround - Prenotazione");
            values.put(CalendarContract.Events.DESCRIPTION, p.getAula()+" Tavolo "+p.getNum_tavolo());
            values.put(CalendarContract.Events.CALENDAR_ID, c.getId());
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Rome\n");
            values.put(CalendarContract.Events.EVENT_LOCATION, strNomeUniversita);
            values.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "1");
            values.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "1");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            Uri uri=cr.insert(CalendarContract.Events.CONTENT_URI, values);
            int eventID = (int) Long.parseLong(uri.getLastPathSegment());
            values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, 60);
            values.put(CalendarContract.Reminders.EVENT_ID, (long)eventID);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);

            database.insertEventoCalendario(p.getId_prenotazione(), (int) c.getId(),eventID);
        }
        return true;
    }

    public void delete_event(CalendarEvent event){
        ContentResolver cr = getContentResolver();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, (long) event.getId_event());
        cr.delete(deleteUri, null, null);
    }


    //////ALARM
    public String create_alarm(Prenotazione prenotazione, boolean inizio, boolean pausa){
        Calendar cal_allarme = Calendar.getInstance();
        if(pausa==true) cal_allarme.add(Calendar.SECOND, this.pausa); //allarme per pausa
        else if(inizio==true){ //allarme per ingresso
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date_allarme = null;
            try {
                date_allarme = df.parse(prenotazione.getOrario_prenotazione());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal_allarme.setTime(date_allarme);
            cal_allarme.add(Calendar.SECOND, ingresso);
        }
        else{ //allarme per scadenza prenotazione
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date_allarme = null;
            try {
                date_allarme = df.parse(prenotazione.getOrario_fine_prenotazione());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal_allarme.setTime(date_allarme);
            cal_allarme.add(Calendar.SECOND, -300);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction("StudyAround");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, prenotazione.getId_prenotazione(), intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal_allarme.getTimeInMillis(), pendingIntent);

        String strTarget=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal_allarme.getTime());
        return strTarget;
    }
    public void cancel_alarm(Prenotazione prenotazione){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction("StudyAround");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, prenotazione.getId_prenotazione(), intent, 0);
        alarmManager.cancel(pendingIntent);
    }


    private String create_alarm_singolo(int id_prenotazione, String orario_prenotazione){
        //cancel_alarm(id_prenotazione);
        String myTime = orario_prenotazione;
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
        intent.setAction("StudyAround");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id_prenotazione, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

        String strOra=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
        return strOra;
    }




    //////QR SCANNER
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==24 && resultCode== CommonStatusCodes.SUCCESS && data!=null){
            Barcode barcode=data.getParcelableExtra("barcode");
            String s=barcode.displayValue;
            int first=s.indexOf('"')+1;
            int second=s.lastIndexOf('"');
            String id_aula=s.substring(first,second);
            String nome_aula=database.getNomeAula(id_aula);
            String entrata_uscita=s.substring(0,first-1);

            if(!nome_aula.equals(p.getAula())){
                MyToast.makeText(getApplicationContext(),"Impossibile procedere\nHai sbagliato aula!", false).show();
                return;
            }
            if(entrata_uscita.equals("tavolo") && richiesta!=10){
                MyToast.makeText(getApplicationContext(),"Impossibile procedere\nNon sei abilitato ad effettuare l'operazione!", false).show();
                return;
            }
            if(entrata_uscita.equals("entrata") && richiesta!=0 && richiesta!=5){
                MyToast.makeText(getApplicationContext(),"Impossibile procedere\nNon sei abilitato ad entrare in aula!", false).show();
                return;
            }
            if(entrata_uscita.equals("uscita") && richiesta!=2 && richiesta!=3 && richiesta!=6){
                MyToast.makeText(getApplicationContext(),"Impossibile procedere\nNon sei abilitato ad uscire dall'aula!", false).show();
                return;
            }
            if(richiesta==10){
                new doOperazione().execute();
                return;
            }


            final Dialog d = new Dialog(PrenotazioniAttiveActivity.this);
            d.setCancelable(false);
            d.setContentView(R.layout.dialog_qr_code);
            d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
            TextView txt_qr= d.findViewById(R.id.et_qr);
            Button btn_yes=d.findViewById(R.id.btn_yes_qr);
            Button btn_no=d.findViewById(R.id.btn_no_qr);
            txt_qr.setText(entrata_uscita+" "+nome_aula);
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new doOperazione().execute();
                    d.dismiss();
                }
            });
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            d.show();
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }

    public void scanQRcode(){
        if (ActivityCompat.checkSelfPermission(PrenotazioniAttiveActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(PrenotazioniAttiveActivity.this, Manifest.permission.CAMERA)) MyToast.makeText(getApplicationContext(),"Non puoi accedere alla camera! Hai negato il permesso!",false).show();
            else ActivityCompat.requestPermissions(PrenotazioniAttiveActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.CAMERA}, 2);
        }
        else {
           startScan();
        }
    }

    public void startScan(){
        if(richiesta==0 || richiesta==5) MyToast.makeText(getApplicationContext(), "Scannerizza QR code all'ingresso", true).show();
        else if(richiesta==10) MyToast.makeText(getApplicationContext(), "Scannerizza un qualsiasi QR code dentro l'aula", true).show();
        else MyToast.makeText(getApplicationContext(), "Scannerizza QR code all'uscita", true).show();
        Intent i = new Intent(PrenotazioniAttiveActivity.this, ScanQRCodeActivity.class);

        startActivityForResult(i, 24);
    }


    //CONTEXT MENU
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView list=(ListView) v;
        Prenotazione p= (Prenotazione) list.getItemAtPosition(info.position);
        //Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>"+p.getId_aula()+"</b></font>"), Toast.LENGTH_SHORT).show();

        if(p.getIn_corso().equals("in_corso")){
            if(p.getStato()==1 || p.getStato()==2){
                menu.add(Menu.FIRST, 0, Menu.FIRST,"Entra in aula");
                if(p.getStato()==1) menu.add(Menu.FIRST, 10, Menu.FIRST+1,"Sono già in aula");
                menu.add(Menu.FIRST, 1, Menu.FIRST+1,"Termina prenotazione");
            }
            if(p.getStato()==0){
                menu.add(Menu.FIRST, 2, Menu.FIRST,"Effettua pausa");
                menu.add(Menu.FIRST, 3, Menu.FIRST+1,"Termina prenotazione");
            }
        }
        else if(p.getIn_corso().equals("futura")){
            menu.add(Menu.FIRST, 8, Menu.FIRST,"Sincronizza con calendario");
            menu.add(Menu.FIRST, 4, Menu.FIRST+1,"Cancella prenotazione");
        }
        else if(p.getIn_corso().equals("conclusa") && p.getStato()!=1){
            menu.add(Menu.FIRST, 5, Menu.FIRST,"Entra in aula");
            menu.add(Menu.FIRST, 6, Menu.FIRST+1,"Esci dall'aula");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        richiesta=item.getItemId();
        p= (Prenotazione) list_in_corso.getItemAtPosition(info.position);
        if(richiesta==1 || richiesta==4) new doOperazione().execute();
        else if(richiesta==8) sincronizza();
        else scanQRcode();

        return true;
    }

    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST+1, "Home");
        menu.add(Menu.FIRST, 2, Menu.FIRST, "Aggiorna");
        menu.add(Menu.FIRST, 3, Menu.FIRST+3, "Gestione Gruppi");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent i = new Intent(this, Home.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        if (item.getItemId() == 2) {
            Intent i = new Intent(this, PrenotazioniAttiveActivity.class);
            startActivity(i);
            finish();
        }
        if(item.getItemId() == 3){
            Intent i = new Intent(this, GroupActivity.class);
            startActivity(i);
            finish();
        }
        return true;
    }

}
