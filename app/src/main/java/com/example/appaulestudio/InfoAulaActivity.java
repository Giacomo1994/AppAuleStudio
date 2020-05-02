package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.flexbox.FlexboxLayout;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
public class InfoAulaActivity extends AppCompatActivity {
    static final String URL_ORARI_SETTIMANA_DEFAULT="http://pmsc9.altervista.org/progetto/infoaula_orariDefault.php";
    static final String URL_ORARI_SETTIMANA_SPECIALI="http://pmsc9.altervista.org/progetto/infoaula_orariSpeciali.php";
    static final String URL_CHECK_APERTA="http://pmsc9.altervista.org/progetto/infoaula_checkAperta.php";
    static final String URL_CHECK_POSTI="http://pmsc9.altervista.org/progetto/infoaula_checkPosti.php";

    TextView infoAula_nome, infoAula_luogo, infoAula_output, infoAula_gruppi, infoAula_posti;
    Button btnNotifica, btnPrenotazioneGruppo, btnPrenotazionePosto;
    ImageView imgGruppo;

    Intent intent;
    Bundle bundle;
    Aula aula;
    HashMap<Integer, Orario> orari_default;
    LinkedList<Orario_Speciale> orari_speciali;
    ProgressBar bar;

    SqliteManager database;


//ON CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_aula);
        infoAula_nome=findViewById(R.id.infoAula_nome);
        infoAula_output=findViewById(R.id.infoAula_settimana);
        infoAula_luogo=findViewById(R.id.infoAula_luogo);
        infoAula_gruppi=findViewById(R.id.infoAula_gruppi);
        infoAula_posti=findViewById(R.id.infoAula_posti);
        btnNotifica=findViewById(R.id.infoAula_toNotifica);
        btnPrenotazionePosto=findViewById(R.id.infoAula_toPrenSingolo);
        btnPrenotazioneGruppo=findViewById(R.id.infoAula_toPrenGruppo);
        imgGruppo=findViewById(R.id.imageView2);
        bar=this.findViewById(R.id.bar2);


        intent = getIntent();
        bundle=intent.getBundleExtra("bundle");
        aula=bundle.getParcelable("aula");
        infoAula_nome.setText(aula.getNome());
        infoAula_luogo.setText(aula.getLuogo());
        if(aula.getGruppi()==0) infoAula_gruppi.setText("Disponibile per i gruppi");
        else{
            infoAula_gruppi.setText("Non disponibile per i gruppi");
            imgGruppo.setImageResource(R.drawable.singolo);
        }

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        String strNome=settings.getString("nome", null);
        setTitle(strNome);

        database=new SqliteManager(InfoAulaActivity.this);
        new check_aperta().execute();
        new mostra_orari().execute();
        riempiServizi();
    }

// riempi servizi
    public  void riempiServizi(){
        FlexboxLayout layout=findViewById(R.id.infoAula_serviziDisponibili);
        String[] servizi=aula.getServizi().split(",");

        for (String s: servizi) {
            String servizio=s.toUpperCase().trim();

            TextView text =new TextView(InfoAulaActivity.this);
            text.setPadding(15,15,15,15);

            LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,10, 10, 10);

            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 30 );
            shape.setColor(Color.argb(255,135, 204, 75));

            text.setText(servizio);
            text.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            text.setLayoutParams(params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                text.setBackground(shape);
                text.setTextColor(Color.WHITE);
            }
            layout.addView(text);
        }


    }

// ASYNC TASK --> MI DICE QUANTI POSTI SONO LIBERI
    private class check_posti extends AsyncTask<Void, Void, Integer[]> {
        @Override
        protected Integer[] doInBackground(Void... voids) {
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

                url = new URL(URL_CHECK_POSTI);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(2000);
                urlConnection.setConnectTimeout(2000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                parametri = "id_aula=" + URLEncoder.encode(aula.getIdAula(), "UTF-8") + "&is_gruppi=" + URLEncoder.encode(""+aula.getGruppi(), "UTF-8");
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
                JSONObject json_data = jArray.getJSONObject(0);
                Integer[] posti=new Integer[2];
                posti[0]=json_data.getInt("posti_totali");
                posti[1]=json_data.getInt("posti_liberi");
                return posti;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(Integer[] result) {
            if(result==null) return;
            infoAula_posti.setText("Posti Totali: "+result[0]+ " - "+"Posti Liberi: "+result[1]);
            if(aula.getGruppi()==0) btnPrenotazioneGruppo.setVisibility(View.VISIBLE);
            if(result[1]==0) btnNotifica.setVisibility(View.VISIBLE);
            else btnPrenotazionePosto.setVisibility(View.VISIBLE);
        }
    }

// ASYNC TASK --> MOSTRA ORARI
    private class mostra_orari extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
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
                JSONArray jArrayOrariSpeciali;
                JSONArray jArrayOrariDefault;

                url = new URL(URL_ORARI_SETTIMANA_DEFAULT);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1500);
                urlConnection.setConnectTimeout(1000);
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
                jArrayOrariDefault = new JSONArray(result);

                url = new URL(URL_ORARI_SETTIMANA_SPECIALI);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1500);
                urlConnection.setConnectTimeout(1000);
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
                jArrayOrariSpeciali = new JSONArray(result);

                orari_default=new HashMap<Integer, Orario>();
                for (int i = 0; i < jArrayOrariDefault.length(); i++) {
                    JSONObject json_data = jArrayOrariDefault.getJSONObject(i);
                    orari_default.put(json_data.getInt("giorno"),new Orario(json_data.getString("apertura"),json_data.getString("chiusura")));
                }

                orari_speciali=new LinkedList<Orario_Speciale>();
                for (int i = 0; i < jArrayOrariSpeciali.length(); i++) {
                    JSONObject json_data = jArrayOrariSpeciali.getJSONObject(i);
                    String data=json_data.getString("data");
                    String apertura=json_data.getString("apertura");
                    String chiusura=json_data.getString("chiusura");
                    if(apertura.equalsIgnoreCase("null")&&chiusura.equalsIgnoreCase("null")) orari_speciali.add(new Orario_Speciale(data, null, null));
                    else orari_speciali.add(new Orario_Speciale(data, apertura, chiusura));
                }
                return "OK";
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(String result) {
            bar.setVisibility(View.GONE);
            if(result==null) {
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile contattare il server: i dati potrebbero essere non aggiornati</b></font>"), Toast.LENGTH_LONG).show();
                orari_default=mostra_orari_offline();
                orari_speciali=null;
                if(orari_default==null){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Errore in visualizzazione orari</b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            stampa_orari(orari_default,orari_speciali);
        }
    }

// METODO --> RITORNA GLI ORARI PRESI DA SQLITE
    public HashMap<Integer,Orario> mostra_orari_offline(){
        return database.readOrariAula(aula.getIdAula());
    }

//METODO --> STAMPA IN UI TABELLA ORARI
    public void stampa_orari(HashMap<Integer,Orario> orari_default,LinkedList<Orario_Speciale> orari_speciali){
        //PREPARAZIONE
        try {
            if (orari_speciali == null) orari_speciali = new LinkedList<Orario_Speciale>();
            Calendar cal = null;
            Date date = null;
            String[] dateString = new String[7];
            int[] daysOfWeekInt = new int[7];
            String[] daysOfWeekString = new String[7];

            for (int i = 0; i < 7; i++) {
                if (i == 0) {
                    cal = Calendar.getInstance();
                    date = cal.getTime();
                } else {
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    date = cal.getTime();
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                dateString[i] = df.format(date);
                df = new SimpleDateFormat("E", Locale.ITALY);
                daysOfWeekString[i] = df.format(date).toUpperCase();
                daysOfWeekInt[i] = cal.get(Calendar.DAY_OF_WEEK);
            }

            //LinkedList<String> arrayList=new LinkedList<String>();
            LinkedList<Orario_Ufficiale> orari_ufficiali = new LinkedList<Orario_Ufficiale>();
            for (int i = 0; i < 7; i++) {
                String data = dateString[i]; //data
                String giorno = daysOfWeekString[i]; //LUN-MAR
                String apertura = orari_default.get(daysOfWeekInt[i]).getApertura();
                String chiusura = orari_default.get(daysOfWeekInt[i]).getChiusura();

                for (Orario_Speciale os : orari_speciali) {
                    if (os.getData().equals(data)) {
                        apertura = os.getApertura();
                        chiusura = os.getChiusura();
                        break;
                    }
                }
                orari_ufficiali.add(new Orario_Ufficiale(data, giorno, apertura, chiusura));
            }

            Collections.sort(orari_ufficiali);

            String inizio=new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(dateString[0])).substring(0,5);
            String fine=new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(dateString[6])).substring(0,5);
            infoAula_output.setText("Orari da " + inizio + " a " + fine);
            LinearLayout layout = findViewById(R.id.infAula_linear);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            for (Orario_Ufficiale ouf : orari_ufficiali) {
                TextView text = new TextView(InfoAulaActivity.this);
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                text.setPadding(10, 0, 10, 0);
                Spannable spannable = null;
                if (ouf.getApertura() == null && ouf.getChiusura() == null)
                    spannable = new SpannableStringBuilder(ouf.getGiorno() + "\n" + "Chiusa");
                else
                    spannable = new SpannableStringBuilder(ouf.getGiorno() + "\n" + ouf.getApertura().substring(0, 5) + "\n" + ouf.getChiusura().substring(0, 5));
                spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setText(spannable);
                text.setLayoutParams(params);
                layout.addView(text);
            }
        }catch (Exception e){

        }
    }

//ASYNC TASK --> CONTROLLA SE L'AULA è ATTUALMENTE APERTA
    private class check_aperta extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
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

                url = new URL(URL_CHECK_APERTA);
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
                if(jArray.length()==0) return "Chiusa";
                else return "Aperta";
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(String result) {
            LinearLayout layoutPulsanti=findViewById(R.id.infoAula_pulsanti);
            Button prenotaPosto=new Button(InfoAulaActivity.this);
            prenotaPosto.setText("Prenota Posto");
            Button prenotaGruppo=new Button(InfoAulaActivity.this);
            prenotaGruppo.setText("Prenota per gruppo");
            Button notificami=new Button(InfoAulaActivity.this);

            if(result==null) //non c'è connessione
                infoAula_posti.setText("Posti Totali: "+aula.getPosti_totali());


            else if(result.equals("Chiusa")){ //è chiusa
                infoAula_posti.setText("Posti Totali: "+aula.getPosti_totali());
                btnPrenotazionePosto.setVisibility(View.VISIBLE);
                if(aula.getGruppi()==0) btnPrenotazioneGruppo.setVisibility(View.VISIBLE);
            }
            else if(result.equals("Aperta")){ //è aperta
                new check_posti().execute();
            }
        }
    }


//MENU IN ALTO
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
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
            editor.putString("nome", null);
            editor.putString("cognome", null);
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
            startActivityForResult(i, 100);
        }

        return true;
    }



}
