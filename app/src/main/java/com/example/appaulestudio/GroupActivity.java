package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Arrays;


public class GroupActivity extends AppCompatActivity {
    static final String URL_ABBANDONA_GRUPPO= "http://pmsc9.altervista.org/progetto/abbandona_gruppo.php";
    static final String URL_RICHIEDIGRUPPIFROMSTUDENTE="http://pmsc9.altervista.org/progetto/richiedi_gruppi_from_iscrizione.php";
    static final String URL_COMPONENTI_DA_GRUPPO="http://pmsc9.altervista.org/progetto/componenti_gruppo.php";
    String strUniversita, strMatricola, strPassword, strNome, strCognome,strCodiceGruppo;
    Gruppo g;
    SqliteManager database;
    ListView gruppiPerStudente;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        gruppiPerStudente = findViewById(R.id.listaGruppi);
        database=new SqliteManager(GroupActivity.this);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strPassword=settings.getString("password", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        setTitle(""+strNome+" "+strCognome);

        new listaGruppi().execute();
        registerForContextMenu(gruppiPerStudente);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new listaGruppi().execute();
    }

    //MENU CONTESTUALE
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView list=(ListView) v;
        g=(Gruppo) list.getItemAtPosition(info.position);
        strCodiceGruppo = g.getCodice_gruppo();
        menu.add(Menu.FIRST,1,Menu.FIRST+1,"Abbandona gruppo");
        menu.add(Menu.FIRST,2,Menu.FIRST,"Dettagli gruppo");
    }
    //ESCO DAL GRUPPO -> DEVO CANCELLARE LA MIA ISCRIZIONE DATA LA MIA MATRICOLA E OL CODICE DEL GRUPPO
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            new abbandonaGruppo().execute();
        }
        else if(item.getItemId()==2){
            new dettagliGruppo().execute();
        }
        return true;
    }

    private class listaGruppi extends AsyncTask<Void, Void, Gruppo[]>{
        @Override
        protected Gruppo[] doInBackground(Void... voids) {
            try {
                String params;
                URL url;
                HttpURLConnection urlConnection; //serve per aprire connessione
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArrayGruppi;
                url = new URL(URL_RICHIEDIGRUPPIFROMSTUDENTE); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                params = "matricola="+ URLEncoder.encode(strMatricola, "UTF-8") + "&codice_universita="+ URLEncoder.encode(strUniversita, "UTF-8");
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(params);
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
                jArrayGruppi = new JSONArray(result);
                Gruppo[] array_gruppo = new Gruppo[jArrayGruppi.length()];
                for(int i = 0; i<jArrayGruppi.length(); i++){
                    JSONObject json_data = jArrayGruppi.getJSONObject(i);
                    array_gruppo[i] = new Gruppo(json_data.getString("codice_gruppo"),
                            json_data.getString("nome_gruppo"),
                            json_data.getString("codice_corso"),
                            json_data.getString("matricola_docente"),
                            json_data.getInt("componenti_max"),
                            json_data.getDouble("ore_disponibili"),
                            json_data.getString("data_scadenza"));
                    array_gruppo[i].setNome_docente(json_data.getString("nome"));
                    array_gruppo[i].setCognome_docente(json_data.getString("cognome"));
                    array_gruppo[i].setNome_corso(json_data.getString("nome_corso"));
                }
                return array_gruppo;
            }  catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Gruppo[] array_gruppo) {
            if(array_gruppo==null){//prendo i dati da sql locale perchè non riesco ad accedere ai dati in remoto
                MyToast.makeText(getApplicationContext(), "Impossibile contattare il server! I dati potrbbero non essere aggiornati.",false).show();
                ArrayList<Gruppo> arrayList_gruppo=database.selectGruppi();
                if(arrayList_gruppo==null || arrayList_gruppo.size()==0)
                    MyToast.makeText(getApplicationContext(), "Non ci sono iscrizioni", false).show();
                else{
                    ArrayAdapter<Gruppo> adapter = new ArrayAdapter<Gruppo>(GroupActivity.this, R.layout.row_layout_group_activity, arrayList_gruppo ){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            Gruppo item = getItem(position);
                            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_group_activity, parent, false);
                            TextView codGrup = convertView.findViewById(R.id.codGrup);
                            TextView nomeGrup = convertView.findViewById(R.id.nomeGrup);
                            codGrup.setText("Codice gruppo: "+item.getCodice_gruppo());
                            nomeGrup.setText(""+item.getNome_gruppo());

                            return convertView;
                        }
                    };
                    gruppiPerStudente.setAdapter(adapter);
                }
                return;
            }
            if(array_gruppo.length==0){
                MyToast.makeText(getApplicationContext(), "Non ci sono iscrizioni", false).show();
            }
            ArrayAdapter<Gruppo> adapter = new ArrayAdapter<Gruppo>(GroupActivity.this, R.layout.row_layout_group_activity, array_gruppo ){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Gruppo item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_group_activity, parent, false);
                    TextView codGrup = convertView.findViewById(R.id.codGrup);
                    TextView nomeGrup = convertView.findViewById(R.id.nomeGrup);
                    codGrup.setText("Codice gruppo: "+item.getCodice_gruppo());
                    nomeGrup.setText(""+item.getNome_gruppo());

                    return convertView;
                }
            };
            gruppiPerStudente.setAdapter(adapter);
            database.insertGruppi(array_gruppo);
        }
    }

// Prendo da database remoto i componenti del gruppo. Se offline prendo i dati del gruppo da SQLITE
    private class dettagliGruppo extends AsyncTask<Void, Void, User[]> { //OK
        @Override
        protected User[] doInBackground(Void... voids) {
            try {
                String params;
                URL url;
                HttpURLConnection urlConnection;
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArray;
                url = new URL(URL_COMPONENTI_DA_GRUPPO);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                params = "codice_gruppo=" + URLEncoder.encode(g.getCodice_gruppo(), "UTF-8");
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(params);
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
                User[] array_componenti = new User[jArray.length()];
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    array_componenti[i] = new User(json_data.getString("matricola"),
                            json_data.getString("nome"),
                            json_data.getString("cognome"),
                            json_data.getString("codice_universita"),
                            json_data.getString("mail"),
                            json_data.getString("password"),
                            true);
                }
                return array_componenti;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(User[] componenti) {
            final Dialog d = new Dialog(GroupActivity.this);
            d.setCancelable(false);
            d.setContentView(R.layout.dialog_dettagli_gruppo);
            d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
            TextView txt_nome_gruppo= d.findViewById(R.id.txt_dettagli_nome);
            TextView txt_codice_gruppo=d.findViewById(R.id.txt_dettagli_codice);
            TextView txt_corso=d.findViewById(R.id.txt_dettagli_corso);
            TextView txt_docente=d.findViewById(R.id.txt_dettagli_docente);
            TextView txt_ore=d.findViewById(R.id.txt_dettagli_ore);
            TextView txt_scadenza=d.findViewById(R.id.txt_dettagli_scadenza);
            TextView eti_componenti=d.findViewById(R.id.eti_dettagli_componenti);
            ListView list_componenti=d.findViewById(R.id.list_dettagli_componenti);
            Button btnok=d.findViewById(R.id.btn_dettagli_gruppo);
            txt_nome_gruppo.setText(g.getNome_gruppo());
            txt_codice_gruppo.setText(g.getCodice_gruppo());
            txt_corso.setText(g.getNome_corso());
            txt_docente.setText(g.getNome_docente()+" "+g.getCognome_docente());
            int ore_int= (int) g.getOre_disponibili();
            int ore_round= (int) Math.ceil(g.getOre_disponibili());
            if(ore_int==ore_round) txt_ore.setText(""+ore_int+"h");
            else{
                int min=(int)((g.getOre_disponibili()-(double)ore_int)*60);
                txt_ore.setText(""+ore_int+"h "+min+"min");
            }
            try {
                txt_scadenza.setText(new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(g.getData_scadenza())));
            } catch (ParseException e) { }
            d.show();
            btnok.setOnClickListener(new ImageView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    d.dismiss();
                }
            });
            if(componenti==null){
                eti_componenti.setVisibility(View.GONE);
                return;
            }
            ArrayAdapter<User> user_adapter=new ArrayAdapter<User>(GroupActivity.this, R.layout.row_layout_dettagli_gruppo, componenti ){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    User item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_dettagli_gruppo, parent, false);
                    TextView txt_comp = convertView.findViewById(R.id.txt_dettagli_componente);
                    txt_comp.setText(item.getNome()+" "+item.getCognome()+", "+item.getMatricola());
                    return convertView;
                }
            };
            list_componenti.setAdapter(user_adapter);
        }
    }

    public void iscrizione_gruppo(View v){
        Intent i = new Intent(GroupActivity.this, IscrizioneActivity.class);
        startActivity(i);
        finish();
    }


//task asincrono per cancellare una riga dalla tabella iscrizione ovvero abbandonare un gruppo --> OK
    private class abbandonaGruppo extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... strings) {
            try {
                URL url;
                url = new URL(URL_ABBANDONA_GRUPPO);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "matricola=" + URLEncoder.encode(strMatricola, "UTF-8") +
                                   "&codice_gruppo=" + URLEncoder.encode(strCodiceGruppo, "UTF-8");

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
                return "Impossibile contattare il server";
            } finally {
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Hai abbandonato il gruppo")==false){
                MyToast.makeText(getApplicationContext(), "Ops, qualcosa è andato storto: " + result,false).show();
                return;
            }
            else{
                database.deleteGruppo(g);
                new listaGruppi().execute();
                MyToast.makeText(getApplicationContext(), result,true).show();            }
        }
    }

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
            editor.putString("matricola", null);
            editor.putString("email", null);
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
