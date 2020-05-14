package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import com.itextpdf.text.List;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrenotazioniAttiveActivity extends AppCompatActivity {
    static final String URL_PRENOTAZIONI="http://pmsc9.altervista.org/progetto/prenotazioniAttive.php";
    static final String URL_OPERAZIONI="http://pmsc9.altervista.org/progetto/prenotazioniAttive_gestionePrenotazione.php";
    LinearLayout ll_in_corso, ll_future, ll_concluse;
    ListView list_in_corso, list_future, list_concluse;
    String strUniversita,strMatricola,strNome;
    ArrayAdapter<Prenotazione> adapter;
    View v1, v2;
    SqliteManager database;
    IntentIntegrator qrScan;
    Prenotazione p=null;
    int richiesta=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazioni_attive);
        ll_in_corso=findViewById(R.id.prenInCorso_ll);
        ll_future=findViewById(R.id.prenFuture_ll);
        ll_concluse=findViewById(R.id.prenCocluse_ll);
        list_in_corso=findViewById(R.id.list_inCorso);
        list_future=findViewById(R.id.list_future);
        list_concluse=findViewById(R.id.list_concluse);
        v1=findViewById(R.id.delimiter_incorso_future);
        v2=findViewById(R.id.delimiter_future_concluse);
        database=new SqliteManager(PrenotazioniAttiveActivity.this);
        qrScan = new IntentIntegrator(this);

        //prendo preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        setTitle(strNome);

        new getPrenotazioni().execute();
        registerForContextMenu(list_in_corso);
        registerForContextMenu(list_future);
        registerForContextMenu(list_concluse);
    }

// RISULTATO RITORNATO DA QR SCANNER --> apertura dialog oppure messaggio di errore
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                try {
                    String s=result.getContents();
                    int first=s.indexOf('"')+1;
                    int second=s.lastIndexOf('"');
                    String id_aula=s.substring(first,second);
                    String nome_aula=database.getNomeAula(id_aula);
                    String entrata_uscita=s.substring(0,first-1);

                    if(!nome_aula.equals(p.getAula())){
                        Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034'><b>Hai sbagliato aula!</b></font>"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(entrata_uscita.equals("entrata") && richiesta!=0 ){
                        Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Non sei abilitato ad entrare in aula</b></font>"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(entrata_uscita.equals("uscita") && richiesta!=2 ){
                        Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Non sei abilitato ad effettuare la pausa</b></font>"), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    AlertDialog.Builder builder = new AlertDialog.Builder(PrenotazioniAttiveActivity.this);
                    builder.setTitle(entrata_uscita+" "+nome_aula);
                    builder.setMessage("Vuoi procedere?");
                    // Set click listener for alert dialog buttons
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
                    dialog.show();

                } catch (Exception e) {
                    Toast.makeText(this, Html.fromHtml("<font color='#eb4034' ><b>Errore nella lettura del codice QR!</b></font>"), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

//CONTEXT MENU
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView list=(ListView) v;
        Prenotazione p= (Prenotazione) list.getItemAtPosition(info.position);
        //Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>"+p.getId_aula()+"</b></font>"), Toast.LENGTH_SHORT).show();

        if(list.equals(list_in_corso)){
            if(p.getStato()==1 || p.getStato()==2){
                menu.add(Menu.FIRST, 0, Menu.FIRST,"Entra in aula");
                menu.add(Menu.FIRST, 1, Menu.FIRST+1,"Termina prenotazione");
            }
            if(p.getStato()==0){
                menu.add(Menu.FIRST, 2, Menu.FIRST,"Effettua pausa");
                menu.add(Menu.FIRST, 3, Menu.FIRST+1,"Termina prenotazione");
            }
        }
        else if(list.equals(list_future)){
            menu.add(Menu.FIRST, 8, Menu.FIRST,"Sincronizza con calendario");
            menu.add(Menu.FIRST, 4, Menu.FIRST+1,"Cancella prenotazione");
            if(!p.getGruppo().equals("null")) menu.add(Menu.FIRST, 7, Menu.FIRST+2,"Cancella prenotazione gruppo");
        }
        else{
            menu.add(Menu.FIRST, 5, Menu.FIRST,"Entra in aula");
            menu.add(Menu.FIRST, 6, Menu.FIRST+1,"Esci dall'aula");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        richiesta=item.getItemId();

        if(richiesta<=3) p= (Prenotazione) list_in_corso.getItemAtPosition(info.position);
        else if(richiesta==4 || richiesta==7 || richiesta==8) p= (Prenotazione) list_future.getItemAtPosition(info.position);
        else p=(Prenotazione) list_concluse.getItemAtPosition(info.position);

        if(richiesta!=0 && richiesta!=2){
            new doOperazione().execute();
        }
        else qrScan.initiateScan();

        return true;
    }

//
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
                        "&gruppo=" + URLEncoder.encode(p.getGruppo(), "UTF-8");
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
            if(result==null){ //problema di connessione o perchè qualcuno ha occupato il tavolo al posto tuo
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Errore nella connessione al server!</b></font>"), Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>"+result+"</b></font>"), Toast.LENGTH_LONG).show();
            Intent i=new Intent(PrenotazioniAttiveActivity.this,PrenotazioniAttiveActivity.class);
            startActivity(i);
            finish();
        }
    }


//SCARICO PRENOTAZIONI --> incorso, future, terminate e le metto in 3 list view diverse
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
            if(array_prenotazioni==null){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile contattare il server</b></font>"), Toast.LENGTH_LONG).show();
                ll_in_corso.setVisibility(View.GONE);
                ll_future.setVisibility(View.GONE);
                ll_concluse.setVisibility(View.GONE);
                v2.setVisibility(View.GONE);
                v1.setVisibility(View.GONE);
                return;
            }
            if(array_prenotazioni.length==0){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Non ci sono prenotazioni</b></font>"), Toast.LENGTH_LONG).show();
                ll_in_corso.setVisibility(View.GONE);
                ll_future.setVisibility(View.GONE);
                ll_concluse.setVisibility(View.GONE);
                v2.setVisibility(View.GONE);
                v1.setVisibility(View.GONE);
                return;
            }
            ArrayList<Prenotazione> prenotazioni_in_corso=new ArrayList<Prenotazione>();
            ArrayList<Prenotazione> prenotazioni_future=new ArrayList<Prenotazione>();
            ArrayList<Prenotazione> prenotazioni_concluse=new ArrayList<Prenotazione>();

            for(Prenotazione p:array_prenotazioni){
                if(p.getIn_corso().equals("in_corso")) prenotazioni_in_corso.add(p);
                else if(p.getIn_corso().equals("futura")) prenotazioni_future.add(p);
                else if(p.getIn_corso().equals("conclusa")) prenotazioni_concluse.add(p);
            }
            if(prenotazioni_in_corso.size()==0){
                ll_in_corso.setVisibility(View.GONE);
                v1.setVisibility(View.GONE);
            }
            else{
                adapter = new ArrayAdapter<Prenotazione>(PrenotazioniAttiveActivity.this, R.layout.row_layout_prenotazioni_attive_activity, prenotazioni_in_corso) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        Prenotazione item = getItem(position);
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_prenotazioni_attive_activity, parent, false);
                        TableRow riga_gruppo= convertView.findViewById(R.id.riga_gruppo);
                        TextView row_luogo = convertView.findViewById(R.id.row_aula_tavolo);
                        TextView row_inizio = convertView.findViewById(R.id.row_inizio);
                        TextView row_fine = convertView.findViewById(R.id.row_fine);
                        TextView row_gruppo = convertView.findViewById(R.id.row_gruppo);
                        TextView row_stato = convertView.findViewById(R.id.row_stato);

                        row_luogo.setText(item.getAula()+", Tavolo "+item.getNum_tavolo());
                        row_inizio.setText(item.getOrario_prenotazione().substring(8,10)+"/"+item.getOrario_prenotazione().substring(5,7)+" "+item.getOrario_prenotazione().substring(11,16));
                        row_fine.setText(item.getOrario_fine_prenotazione().substring(8,10)+"/"+item.getOrario_fine_prenotazione().substring(5,7)+" "+item.getOrario_fine_prenotazione().substring(11,16));
                        if(item.getGruppo().equals("null")){
                            riga_gruppo.setVisibility(View.GONE);
                        }
                        else{
                            riga_gruppo.setVisibility(View.VISIBLE);
                            row_gruppo.setText(item.getGruppo());
                        }
                        if(item.getStato()==1) row_stato.setText("Non ancora in aula");
                        else if(item.getStato()==2) row_stato.setText("In pausa dalle ore "+item.getOrario_ultima_uscita().substring(11,16));
                        else row_stato.setText("In aula");

                        return convertView;
                    }
                };
                list_in_corso.setAdapter(adapter);
            }

            if(prenotazioni_future.size()==0){
                ll_future.setVisibility(View.GONE);
                v2.setVisibility(View.GONE);
            }
            else{
                adapter = new ArrayAdapter<Prenotazione>(PrenotazioniAttiveActivity.this, R.layout.row_layout_prenotazioni_attive_activity, prenotazioni_future) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        Prenotazione item = getItem(position);
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_prenotazioni_attive_activity, parent, false);
                        TableRow riga_gruppo= convertView.findViewById(R.id.riga_gruppo);
                        TableRow riga_stato= convertView.findViewById(R.id.riga_stato);
                        TextView row_luogo = convertView.findViewById(R.id.row_aula_tavolo);
                        TextView row_inizio = convertView.findViewById(R.id.row_inizio);
                        TextView row_fine = convertView.findViewById(R.id.row_fine);
                        TextView row_gruppo = convertView.findViewById(R.id.row_gruppo);

                        riga_stato.setVisibility(View.GONE);
                        row_luogo.setText(item.getAula()+", Tavolo "+item.getNum_tavolo());
                        row_inizio.setText(item.getOrario_prenotazione().substring(8,10)+"/"+item.getOrario_prenotazione().substring(5,7)+" "+item.getOrario_prenotazione().substring(11,16));
                        row_fine.setText(item.getOrario_fine_prenotazione().substring(8,10)+"/"+item.getOrario_fine_prenotazione().substring(5,7)+" "+item.getOrario_fine_prenotazione().substring(11,16));

                        if(item.getGruppo().equals("null")){
                            riga_gruppo.setVisibility(View.GONE);
                        }
                        else{
                            riga_gruppo.setVisibility(View.VISIBLE);
                            row_gruppo.setText(item.getGruppo());
                        }
                        return convertView;
                    }
                };
                list_future.setAdapter(adapter);
            }

            if(prenotazioni_concluse.size()==0) ll_concluse.setVisibility(View.GONE);
            else {
                adapter = new ArrayAdapter<Prenotazione>(PrenotazioniAttiveActivity.this, R.layout.row_layout_prenotazioni_attive_activity, prenotazioni_concluse) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        Prenotazione item = getItem(position);
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_prenotazioni_attive_activity, parent, false);
                        TableRow riga_gruppo = convertView.findViewById(R.id.riga_gruppo);
                        TableRow riga_stato = convertView.findViewById(R.id.riga_stato);
                        TableRow riga_fine = convertView.findViewById(R.id.riga_ora_fine);
                        TextView row_luogo = convertView.findViewById(R.id.row_aula_tavolo);
                        TextView row_inizio = convertView.findViewById(R.id.row_inizio);
                        TextView row_gruppo = convertView.findViewById(R.id.row_gruppo);

                        riga_stato.setVisibility(View.GONE);
                        riga_fine.setVisibility(View.GONE);
                        row_luogo.setText(item.getAula() + ", Tavolo " + item.getNum_tavolo());
                        row_inizio.setText(item.getOrario_prenotazione().substring(8, 10) + "/" + item.getOrario_prenotazione().substring(5, 7) + " " + item.getOrario_prenotazione().substring(11, 16));
                        if (item.getGruppo().equals("null")) {
                            riga_gruppo.setVisibility(View.GONE);
                        } else {
                            riga_gruppo.setVisibility(View.VISIBLE);
                            row_gruppo.setText(item.getGruppo());
                        }
                        return convertView;
                    }
                };
                list_concluse.setAdapter(adapter);
            }
        }
    }

//MENU IN ALTO
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST+3, "Logout");
        menu.add(Menu.FIRST, 2, Menu.FIRST, "Home");
        menu.add(Menu.FIRST, 3, Menu.FIRST+2, "Gestisci Gruppi");
        menu.add(Menu.FIRST, 4, Menu.FIRST+1, "Prenotazioni");
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
            editor.putString("matricola", null);
            editor.putString("nome", null);
            editor.putString("cognome", null);
            editor.putString("password", null);
            editor.putString("token", null);
            editor.putBoolean("studente", true);
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
