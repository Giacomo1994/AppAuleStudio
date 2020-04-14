package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RegistrazioneActivity extends AppCompatActivity {
    Spinner spinner;
    //ArrayList<Universita> lista_universita;
    ArrayAdapter<Universita> adapter;
    Universita universita=null;
    TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        spinner=findViewById(R.id.reg_universita);
        output=findViewById(R.id.output);
        new riempiUniversita().execute();


        /*lista_universita=new ArrayList<Universita>();
        lista_universita.add(new Universita("polito", "politecnico"));
        lista_universita.add(new Universita("economia", "economia"));

        adapter =new ArrayAdapter(this,android.R.layout.simple_list_item_1,lista_universita);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                universita= (Universita) parent.getItemAtPosition(position);
                output.setText(universita.toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
    }


    private class riempiUniversita extends AsyncTask<Void, Void, Universita[]> {
        @Override
        protected Universita[] doInBackground(Void... strings) {
            try {
                URL url = new URL("http://pmsc9.altervista.org/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                //urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                /*urlConnection.setRequestMethod("POST"); //metodo Post
                String parametri = "nome=" + URLEncoder.encode("inter", "UTF-8");
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri);
                dos.flush();
                dos.close();*/

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

                Universita[] array_universita = new Universita[jArray.length()];
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    array_universita[i]=new Universita(json_data.getString("codice"),json_data.getString("nome"));
                }
                return array_universita;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Universita[] array_universita) {
            output.setText(""+array_universita.length);
            adapter =new ArrayAdapter(RegistrazioneActivity.this,android.R.layout.simple_list_item_1,array_universita);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    universita= (Universita) parent.getItemAtPosition(position);
                    output.setText(universita.toString());
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }


}
