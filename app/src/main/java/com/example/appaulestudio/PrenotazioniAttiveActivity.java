package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrenotazioniAttiveActivity extends AppCompatActivity {
    static final String URL_PRENOTAZIONI="http://pmsc9.altervista.org/progetto/prenotazioniAttive.php";
    LinearLayout ll_in_corso, ll_future, ll_concluse;
    ListView list_in_corso, list_future, list_concluse;
    String strUniversita,strMatricola,strNome;
    ArrayAdapter<Prenotazione> adapter;
    View v1, v2;

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

        //prendo preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        setTitle(strNome);

        new getPrenotazioni().execute();
    }

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
                JSONArray jArrayAule;
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
                    array_prenotazioni[i] = new Prenotazione(json_data.getInt("id"), json_data.getString("matricola"),json_data.getString("nome"),
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
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Non ci sono prenotazioni</b></font>"), Toast.LENGTH_LONG).show();
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
                        else if(item.getStato()==2) row_stato.setText("In pausa");
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


}
