package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
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
// PRENOTAZIONI MOSTRATE (si connessione)
    //tutte le prenotazioni in corso e future
    //tutte le prenotazioni terminate/scadute della giornata odierna
    //no prenotazioni cancellate, no prenotazioni scadute nei giorni precedenti
// PRENOTAZIONI MOSTRATE (no connessione)
    // tutte le prenotazioni da oggi in poi
    // no la prenotazioni da ieri in giù
    // no le prenotazioni cancellate
//QR SCANNER:
    //1) Quando l'utente vuole entrare in aula deve sempre fotografarlo
    //2) Quando l'utente vuole uscire dall'aula deve fotografarlo solo se vuole andare in pausa
//TORNELLO: la richiesta di apertura tornello viene mandata quando
    //1) Vuole entrare in aula
    //2) Vuole fare pausa
    //3) Termina prenotazione ed è dentro l'aula
    //4) Vuole entrare ed uscire dall'aula e la sua prenotazione è terminata per recuperare gli oggetti (quindi non per stato=1)
public class PrenotazioniAttiveActivity extends AppCompatActivity {
    static final String URL_PRENOTAZIONI="http://pmsc9.altervista.org/progetto/prenotazioniAttive.php";
    static final String URL_OPERAZIONI="http://pmsc9.altervista.org/progetto/prenotazioniAttive_gestionePrenotazione.php";
    static final String URL_RICHIESTA_TORNELLO="http://pmsc9arduino.altervista.org/inserisci_richiesta.php";

    LinearLayout ll_in_corso,ll_cronologia;
    ListView list_in_corso, list_cronologia;
    ArrayAdapter<Prenotazione> adapter;

    SqliteManager database;
    IntentIntegrator qrScan;
    public Prenotazione p=null;
    public int richiesta=-1;
    String strUniversita, strNomeUniversita, strMatricola,strNome, strCognome;
    int ingresso, pausa;
    ArrayList<CalendarAccount> array_list_account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazioni_attive);
        ll_in_corso=findViewById(R.id.prenInCorso_ll);
        ll_cronologia=findViewById(R.id.prenCronologia_ll);
        list_in_corso=findViewById(R.id.list_inCorso);
        list_cronologia=findViewById(R.id.list_cronologia);
        array_list_account = new ArrayList<CalendarAccount>();

        database=new SqliteManager(PrenotazioniAttiveActivity.this);
        qrScan = new IntentIntegrator(this);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strNomeUniversita = settings.getString("nome_universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        ingresso=Integer.parseInt(settings.getString("ingresso", null))-300;
        pausa=Integer.parseInt(settings.getString("pausa", null))-300;
        setTitle(strNome+" "+strCognome);

//String strAlarm=settings.getString("alarm_time", null);
//if(strAlarm!=null) MyToast.makeText(getApplicationContext(),strAlarm,true).show();
//else MyToast.makeText(getApplicationContext(),"No alarm",false).show();

        new getPrenotazioni().execute();
        registerForContextMenu(list_in_corso);

        LinkedList<AlarmClass> allarmi_attivi=database.getAlarms();
        if(allarmi_attivi!=null)
            MyToast.makeText(getApplicationContext(),""+allarmi_attivi.get(0).getId_prenotazione()+" "+allarmi_attivi.get(0).getOrario_alarm(),true).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new getPrenotazioni().execute();
    }

//creazione alarm
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

//rimozione alarm
    public void cancel_alarm(Prenotazione prenotazione){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.setAction("StudyAround");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, prenotazione.getId_prenotazione(), intent, 0);
        alarmManager.cancel(pendingIntent);
    }

// RISULTATO RITORNATO DA QR SCANNER --> apertura dialog oppure messaggio di errore
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                MyToast.makeText(getApplicationContext(),"Risultato non trovato!", false).show();
            } else {
                try {
                    String s=result.getContents();
                    int first=s.indexOf('"')+1;
                    int second=s.lastIndexOf('"');
                    String id_aula=s.substring(first,second);
                    String nome_aula=database.getNomeAula(id_aula);
                    String entrata_uscita=s.substring(0,first-1);

                    if(!nome_aula.equals(p.getAula())){
                        MyToast.makeText(getApplicationContext(),"Hai sbagliato aula!", false).show();
                        return;
                    }
                    if(entrata_uscita.equals("entrata") && richiesta!=0 && richiesta!=5){
                        MyToast.makeText(getApplicationContext(),"Non sei abilitato ad entrare in aula!", false).show();
                        return;
                    }
                    if(entrata_uscita.equals("uscita") && richiesta!=2 && richiesta!=3 && richiesta!=6){
                        MyToast.makeText(getApplicationContext(),"Non sei abilitato ad uscire dall'aula!", false).show();
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


                    /*AlertDialog.Builder builder = new AlertDialog.Builder(PrenotazioniAttiveActivity.this);
                    builder.setTitle(entrata_uscita+" "+nome_aula);
                    builder.setMessage("Vuoi procedere?");
                    //click listener for alert dialog buttons --> Se sì esegui task asincrono
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch(which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    new doOperazione().execute();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };
                    builder.setPositiveButton("Si", dialogClickListener);
                    builder.setNegativeButton("No",dialogClickListener);
                    AlertDialog dialog = builder.create();
                    dialog.show();*/
                } catch (Exception e) {MyToast.makeText(getApplicationContext(),"Errore nella lettura del codice QR. Riprova!",false).show();}
            }
        } else super.onActivityResult(requestCode, resultCode, data);
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
            //if(!p.getGruppo().equals("null")) menu.add(Menu.FIRST, 7, Menu.FIRST+2,"Cancella prenotazione gruppo");
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
        //richiata =1,3,4,6 no scanner
        if(richiesta==1 || richiesta==4) new doOperazione().execute();
        else if(richiesta==8) sincronizza();
        else qrScan.initiateScan();

        return true;
    }

//TASK ASINCRONO --> Richiesta accesso a tornello
    private class doRichiestaTornello extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_RICHIESTA_TORNELLO);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
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

//TASK ASINCRONO --> Operazione su prenotazione --> ritorna una stringa di errore o successo
    private class doOperazione extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_OPERAZIONI);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
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
                finish();
                return;
            }
            if(result.equals("Accesso non consentito") || result.equals("Impossibile effettuare pausa") || result.equals("Impossibile cancellare prenotazione"))
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

            new getPrenotazioni().execute();
        }
    }


//SCARICO PRENOTAZIONI --> prenotazioni incorso, future o terminate nella giornata e le metto in list view --> Se non c'è connessione prendo i dati da sqlite
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
                urlConnection.setReadTimeout(2000);
                urlConnection.setConnectTimeout(2000);
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
            list_cronologia.setAdapter(null);
            list_in_corso.setAdapter(null);
            //offline
            if(array_prenotazioni==null){
                MyToast.makeText(getApplicationContext(),"Impossibile contattare il server! I dati potrebbero non essere aggiornati", false).show();

                ArrayList<Prenotazione> prenotazioni_offline=database.selectPrenotazioni();
                if(prenotazioni_offline==null || prenotazioni_offline.size()==0){
                    MyToast.makeText(getApplicationContext(),"Non ci sono prenotazioni!", false).show();
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
            ArrayList<Prenotazione> lista_prenotazioni=new ArrayList<Prenotazione>();
            for(Prenotazione p:array_prenotazioni){
                if(p.getStato()==1 && p.getIn_corso().equals("futura") && !p.getGruppo().equals("null")){
                    String orario_alarm=create_alarm(p,true,false);
                    database.insertAlarm(p.getId_prenotazione(),orario_alarm);
                }
                lista_prenotazioni.add(p);
            }
            Collections.sort(lista_prenotazioni);

            if(lista_prenotazioni.size()==0){
                MyToast.makeText(getApplicationContext(),"Non ci sono prenotazioni!", false).show();
                database.insertPrenotazioniGruppi(lista_prenotazioni);
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
            database.insertPrenotazioniGruppi(lista_prenotazioni);
        }
    }

    //////CALENDARIO
    public void sincronizza() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {
            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR}, 1);
        } else dialog_pick_calendar(get_account_from_calendar());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(PrenotazioniAttiveActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
            if (requestCode == 1) dialog_pick_calendar(get_account_from_calendar());
        } else
            MyToast.makeText(getApplicationContext(), "Non puoi accedere ai calendari", false).show();

    }

    public void dialog_pick_calendar(final ArrayList<CalendarAccount> lista_account) {
        final Dialog dialog = new Dialog(PrenotazioniAttiveActivity.this);
        dialog.setTitle("Seleziona account");
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_seleziona_calendario);
        ListView lv_account = dialog.findViewById(R.id.lv_account);
        Button conferma = dialog.findViewById(R.id.btn_account_conferma);
        Button indietro = dialog.findViewById(R.id.btn_account_indietro);

        ArrayAdapter<CalendarAccount> adapter_calendar = new ArrayAdapter<CalendarAccount>(PrenotazioniAttiveActivity.this, R.layout.row_layout_dialog_calendario, lista_account) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final CalendarAccount item = getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_dialog_calendario, parent, false);
                CheckBox check_cal = convertView.findViewById(R.id.check_account);
                check_cal.setText(item.getName());
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


    public ArrayList<CalendarAccount> get_account_from_calendar() {
        ArrayList<CalendarAccount> lista = new ArrayList<CalendarAccount>();
        @SuppressLint("MissingPermission") Cursor cursor = getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null, CalendarContract.Calendars.VISIBLE + " = 1", null, null);
        while (cursor.moveToNext()) {
            String accountName = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME));
            long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID));
            String name = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.NAME));
            String type = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE));
            if (name.contains("Holidays") || name.contains("Festività") || name.equals("Contacts")) continue;
            lista.add(new CalendarAccount(id, name, accountName, type));
        }
        return lista;
    }


    @SuppressLint("MissingPermission")
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

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(2020, 4, 26, 8, 30);

            Calendar endTime = Calendar.getInstance();
            endTime.set(2020, 4, 26, 20, 30);

            ContentValues values = new ContentValues();
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




//MENU IN ALTO
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
            editor.putString("email", null);
            editor.putString("matricola", null);
            editor.putString("nome", null);
            editor.putString("cognome", null);
            editor.putString("password", null);
            editor.putString("token", null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.putString("universita", null);
            editor.putString("nome_universita", null);
            editor.putString("last_update", null);
            editor.putString("ingresso", null);
            editor.putString("pausa", null);
            editor.putString("slot", null);
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
            finish();
        }
        if(item.getItemId() == 4){
            Intent i = new Intent(this, PrenotazioniAttiveActivity.class);
            startActivity(i);
            finish();
        }
        return true;
    }



}
