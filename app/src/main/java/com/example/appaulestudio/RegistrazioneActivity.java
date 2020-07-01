package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrazioneActivity extends AppCompatActivity {
    static final String URL_UNIVERSITA="http://pmsc9.altervista.org/progetto/login_listaUniversita.php";
    static final String URL_REGISTRAZIONE="http://pmsc9.altervista.org/registrazione.php";

    Spinner spinner;
    ArrayAdapter<Universita> adapter;
    Button btn_registrazione;
    EditText txt_matricola,txt_nome,txt_cognome,txt_email,txt_password, txt_password2;
    RadioButton radioStudente,radioDocente;
    ImageView img_reg;
    Dialog dialogLoading;

    Intent intent;
    Universita universita=null;
    String matricola,nome,cognome, email, password, password2;
    boolean isStudente=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        spinner=findViewById(R.id.reg_universita);
        btn_registrazione=findViewById(R.id.btn_registrazione);
        txt_matricola=this.findViewById(R.id.reg_matricola);
        txt_nome=this.findViewById(R.id.reg_nome);
        txt_cognome=this.findViewById(R.id.reg_cognome);
        txt_email=this.findViewById(R.id.reg_email);
        txt_password=this.findViewById(R.id.reg_password);
        txt_password2=this.findViewById(R.id.reg_password2);
        radioStudente=findViewById(R.id.radioButton5);
        radioDocente=findViewById(R.id.radioButton6);
        img_reg=findViewById(R.id.img_registrazione);
        action_bar();
        dialogLoading();

        radioStudente.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(radioStudente.isChecked()){
                    img_reg.setImageDrawable(getResources().getDrawable(R.drawable.studente));
                    isStudente=true;
                }else {
                    img_reg.setImageDrawable(getResources().getDrawable(R.drawable.docente));
                    isStudente=false;
                }
            }
        });

        btn_registrazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matricola=txt_matricola.getText().toString().trim();
                nome=txt_nome.getText().toString().trim();
                cognome=txt_cognome.getText().toString().trim();
                email=txt_email.getText().toString().trim();
                password=txt_password.getText().toString().trim();
                password2=txt_password2.getText().toString().trim();

                //controllo campi non vuoti
                if(matricola.equals("")||nome.equals("")||cognome.equals("")||email.equals("")||password.equals("")||password2.equals("")){
                    MyToast.makeText(getApplicationContext(),"Per favore, inserisci tutti i campi!", false).show();
                    return;
                }

                //controllo formato mail
                String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                        "[a-zA-Z0-9_+&*-]+)*@" +
                        "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                        "A-Z]{2,7}$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(email);
                boolean isValidMail=matcher.matches();
                if(isValidMail==false){
                    MyToast.makeText(getApplicationContext(),"Formato e-mail non valido!", false).show();
                    return;
                }

                //controllo password uguali
                if(password.equals(password2)==false){
                    MyToast.makeText(getApplicationContext(),"Le password devono essere uguali!", false).show();
                    return;
                }

                //controllo lunghezza password
                if(password.length()<8){
                    MyToast.makeText(getApplicationContext(),"La password deve contenere almeno 8 caratteri!", false).show();

                    return;
                }
                dialogLoading.show();
                new registraUtente().execute();
                //new checkUtenteFromUniversita();
            }
        });

        intent=getIntent();
        new riempiUniversita().execute();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new riempiUniversita().execute();
    }

    @SuppressLint("WrongConstant")
    private void action_bar(){

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar=view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText(getString(R.string.header_registrazione));
        image_actionbar.setImageDrawable(getResources().getDrawable(R.drawable.logo_size));
    }

    //TASK ASINCRONO --> riempie lo spinner delle univeristà
    private class riempiUniversita extends AsyncTask<Void, Void, Universita[]> {
        @Override
        protected Universita[] doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_UNIVERSITA);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
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
                    array_universita[i] = new Universita(json_data.getString("codice"), json_data.getString("nome"),
                            json_data.getDouble("latitudine"), json_data.getDouble("longitudine"),
                            json_data.getInt("ingresso"), json_data.getInt("pausa"),
                            json_data.getInt("slot"), json_data.getString("first_slot"),
                            json_data.getString("url_registrazione"),json_data.getString("url_corsi"));
                }
                return array_universita;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Universita[] array_universita) {
            if(array_universita==null){
                spinner.setEnabled(false);
                MyToast.makeText(getApplicationContext(),"Sei offline! Connettititi ad una rete per effettuare registrarti",false).show();
                return;
            }
            spinner.setEnabled(true);
            adapter = new ArrayAdapter(RegistrazioneActivity.this, R.layout.simple_custom_list_item, array_universita);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    universita = (Universita) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    //registra nella tabella utente il nuovo utente
    private class registraUtente extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_REGISTRAZIONE);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(7000);
                urlConnection.setConnectTimeout(7000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "universita=" + URLEncoder.encode(universita.getCodice(), "UTF-8")
                        + "&url_registrazione=" + URLEncoder.encode(universita.getUrl_registrazione(), "UTF-8")
                        + "&matricola=" + URLEncoder.encode(matricola, "UTF-8")
                        + "&nome=" + URLEncoder.encode(nome, "UTF-8")
                        + "&cognome=" + URLEncoder.encode(cognome, "UTF-8")
                        + "&mail=" + URLEncoder.encode(email, "UTF-8")
                        + "&password=" + URLEncoder.encode(password, "UTF-8")
                        + "&flag_studente=" + URLEncoder.encode(""+isStudente, "UTF-8"); //imposto parametri da passare
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
                Log.e("SimpleHttpURLConnection", e.getMessage());
                return "Impossibile connettersi";
            } finally {
            }
        }

        @Override
        protected void onPostExecute(String result) {
            dialogLoading.dismiss();
            if(!result.equals("Utente registrato")) MyToast.makeText(getApplicationContext(), "Errore: "+result, false).show();
            else{
                intent.putExtra("matricola", matricola);
                intent.putExtra("password", password);
                intent.putExtra("isStudente", isStudente);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }

    private void dialogLoading(){
        dialogLoading= new Dialog(RegistrazioneActivity.this);
        dialogLoading.setCancelable(true);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogLoading.getWindow().setDimAmount(0);

    }


    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, getString(R.string.options_aggiorna));
        menu.add(Menu.FIRST, 2, Menu.FIRST+1, getString(R.string.header_login));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            new riempiUniversita().execute();
        }
        if (item.getItemId() == 2) {
            finish();
        }
        return true;
    }
}
