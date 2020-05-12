package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrenotazioneGruppoActivity extends AppCompatActivity {
    TextView nomeAula, output;
    SubsamplingScaleImageView piantaAula;
    ArrayList<Orario_Ufficiale> orariUfficiali;
    String nomeStudente, matricolaStudente,codiceUniversita;
    Aula aula;
    Intent intent;
    static final String URL_GRUPPI_DA_MATRICOLA="http://pmsc9.altervista.org/progetto/gruppi_da_matricola.php";
    GridView grigliaGruppi;
    Gruppo[] array_gruppo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazione_gruppo);
        initUI();
    }

    //CREAZIONE MENU IN ALTO
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 3, Menu.FIRST, "Gestisci Gruppi");
        menu.add(Menu.FIRST, 1, Menu.FIRST+2, "Logout");
        menu.add(Menu.FIRST, 2, Menu.FIRST + 1, "Home");
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
            editor.putString("email_calendar", null);
            editor.putString("matricola", null);
            editor.putString("password", null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.putString("last_update", null);
            editor.commit();
            Intent i = new Intent(this, MainActivity.class);
            startActivityForResult(i, 100);
            finish();
        }
        if (item.getItemId() == 2) {
            Intent i = new Intent(this, Home.class);
            startActivityForResult(i, 47);
            finish();
        }
        if(item.getItemId()==3){
            Intent i = new Intent(this, GroupActivity.class);
            startActivityForResult(i, 159);
            finish();
        }
        return true;
    }

    private void initUI(){
        //array_gruppo=new Gruppo[1];
        /*Gruppo g=new Gruppo("codice1","nome1", "corso1",
                "b", 3,
       3, "domani");
        array_gruppo[0]=g;
        array_gruppo[1]=g;
        array_gruppo[2]=g;*/
        nomeAula=findViewById(R.id.nomeAula);
        output=findViewById(R.id.output);
        piantaAula=findViewById(R.id.piantaAula);
        intent =getIntent();
        Bundle bundle=intent.getBundleExtra("dati");
        aula=bundle.getParcelable("aula");
        nomeAula.setText(aula.getNome());
        orariUfficiali=bundle.getParcelableArrayList("orari");
        Collections.sort(orariUfficiali);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        nomeStudente=settings.getString("nome", null);
        matricolaStudente=settings.getString("matricola", null);
        codiceUniversita=settings.getString("universita", null);
        setTitle(nomeStudente);
        new prendiGruppi().execute();


        //output.setText("");

    }
//il gruppo può scegliere un giorno da oggi a 5 giorni e può prenotare tot slot orari di 1ora e mezza ciascuno
    //prendo il gruppo e valuto se esiste e ha ore residue e non è scaduto

    private class prendiGruppi extends AsyncTask<Void, Void, Gruppo[]> {

        @Override
        protected Gruppo[] doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_GRUPPI_DA_MATRICOLA);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "matricola_studente=" + URLEncoder.encode(matricolaStudente, "UTF-8")
                        +"&codice_universita="+URLEncoder.encode(codiceUniversita, "UTF-8");

                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri); //passo i parametri
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
                JSONArray jArrayCorsi = new JSONArray(result);

                array_gruppo = new Gruppo[jArrayCorsi.length()];

                for (int i = 0; i < jArrayCorsi.length(); i++) {
                    JSONObject json_data = jArrayCorsi.getJSONObject(i);
                    array_gruppo[i] = new Gruppo(json_data.getString("codice_gruppo"),
                            json_data.getString("nome_gruppo"),
                            json_data.getString("codice_corso"),
                            json_data.getString("matricola_docente"),
                            json_data.getInt("componenti_max"),
                            json_data.getInt("ore_disponibili"),
                            json_data.getString("data_scadenza"));
                }

                return array_gruppo;


            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Gruppo[] array_gruppo) {
            super.onPostExecute(array_gruppo);
            if (array_gruppo == null) {
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Nessun gruppo disponibile</b></font>"), Toast.LENGTH_LONG).show();

                return;
            }
            /*Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Gruppi disponibili</b></font>"), Toast.LENGTH_LONG).show();

            for (Gruppo g:array_gruppo){
                output.append(g.getCodice_gruppo());
            }*/

            Dialog d = new Dialog(PrenotazioneGruppoActivity.this);
            d.setTitle("Seleziona il gruppo con cui vuoi studiare");
            d.setCancelable(false);
            d.setContentView(R.layout.dialog_scegli_gruppo);
            grigliaGruppi=d.findViewById(R.id.grigliaGruppi);

            GridViewAdapter booksAdapter = new GridViewAdapter(PrenotazioneGruppoActivity.this, array_gruppo);
            grigliaGruppi.setAdapter(booksAdapter);
            d.show();
        }
    }




}









