package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.prefs.Preferences;

public class Home extends AppCompatActivity {
    static final String URL_RICHIEDIAULE="http://pmsc9.altervista.org/progetto/richiedi_aule.php";
    ArrayAdapter adapter;
    String strUniversita;
    String strNomeUniversita;
    String strMatricola;
    String strPassword;
    String strStudente;
    String strLogged;
    SharedPreferences settings;
    protected void initUI(){
        final FrameLayout fl= findViewById(R.id.fl);
        ListView elencoAule = findViewById(R.id.elencoAule);
        /*TextView nomeAula_home= findViewById(R.id.nomeAula_home);
        TextView luogoAula_home=findViewById(R.id.luogoAula_home);
        TextView postiLiberi_home = findViewById(R.id.postiLiberi_home);*/


        //doppio frame
        final LinearLayout frameLista = (LinearLayout)findViewById(R.id.frameLista);
        final LinearLayout frameMappa = (LinearLayout)findViewById(R.id.frameMappa);
        Button mappa= findViewById(R.id.mappa);
        Button lista = findViewById(R.id.lista);

        //passo da lista a mappa
        mappa.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                frameLista.setVisibility(fl.GONE);
                frameMappa.setVisibility(fl.VISIBLE);


            }

        });
        //passo da mappa a lista
        lista.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                frameMappa.setVisibility(fl.GONE);
                frameLista.setVisibility(fl.VISIBLE);
            }

        });
         settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
         strUniversita=settings.getString("universita", null);
         strNomeUniversita=settings.getString("nome_universita", null);
         strMatricola=settings.getString("matricola", null);
         strPassword=settings.getString("password", null);
         strStudente=""+settings.getBoolean("studente", false);
         strLogged=""+settings.getBoolean("logged", false);
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initUI();
        //TextView txt=findViewById(R.id.txtHome);



        Toast.makeText(getApplicationContext(),""+strNomeUniversita+" "+strMatricola+" "+strPassword+" "+strStudente+" "+strLogged,Toast.LENGTH_LONG).show();

    }

     @Override protected void onStart(){
        super.onStart();


     }

    //richiedi info aule al database
    private class listaAule extends AsyncTask<Void, Void, Aula[]> {
        @Override
        protected Aula[] doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_RICHIEDIAULE);
                //URL url = new URL("http://10.0.2.2/progetto/richiedi_aule.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "codice_universita=" + URLEncoder.encode(strUniversita, "UTF-8"); //imposto parametri da passare
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
                    array_aula[i] = new Aula(json_data.getString("id"), json_data.getString("nome"),
                            json_data.getString("luogo"), json_data.getDouble("latitudine"),
                            json_data.getDouble("longitudine"), json_data.getBoolean("gruppi"),
                            json_data.getInt("posti_liberi"));
                }
                return array_aula;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Aula[] array_aula) {
            //adapter = new ArrayAdapter<Aula>(Home.this,R.layout, 100, array_aula);
            /*if(array_aula==null){
                Toast.makeText().show;
            }
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    universita = (Universita) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });*/
        }
    }






    @Override //creazione menu in alto
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
        menu.add(Menu.FIRST,2, Menu.FIRST+1,"Home");
        return true;
    }

    @Override //se premo "Inserisci componente mi porta alla seconda activity"
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("universita",null);
            editor.putString("nome_universita",null);
            editor.putString("matricola",null);
            editor.putString("password",null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.commit();
        }
        Intent i=new Intent(this,MainActivity.class);
        startActivityForResult(i,100);
        finish();
        return true;
    }
}
