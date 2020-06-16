package com.example.appaulestudio;

import androidx.appcompat.app.*;

import android.annotation.SuppressLint;
import android.app.*;
import android.app.ActionBar;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import android.graphics.*;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {
    static final String URL_UNIVERSITA="http://pmsc9.altervista.org/progetto/login_listaUniversita.php";
    static final String URL_LOGIN="http://pmsc9.altervista.org/progetto/login.php";
    static final String URL_RECUPERA_PASSWORD="http://pmsc9.altervista.org/progetto/recupero_password.php";

    ImageView studente_docente;
    TextView txt_toRegistrazione, txt_toPassword;
    Spinner spinner;
    ArrayAdapter<Universita> adapter=null;
    EditText txtMatricola, txtPassword;
    Button btn_login;
    RadioButton radioStudente,radioDocente;

    Universita universita=null, universita_recupero=null;
    String matricola, password, token=null;
    boolean isStudente;
    boolean studentePassato;
    boolean is_logged=false, is_studente=false;
    String rec_matricola=null, rec_mail=null;


    private void initUI(){
        txt_toRegistrazione=findViewById(R.id.log_toRegistrazione);
        txt_toPassword=findViewById(R.id.log_toPassword);
        spinner=findViewById(R.id.log_spinner);
        txtMatricola=findViewById(R.id.log_matricola);
        txtPassword=findViewById(R.id.log_password);
        btn_login=findViewById(R.id.btn_login);
        radioStudente=findViewById(R.id.radioButton);
        radioDocente=findViewById(R.id.radioDocente);
        studente_docente = findViewById(R.id.imageView9);

        action_bar();

        //radio button
        if(radioStudente.isChecked()){
            studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.studente));
        }else {
            studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.docente));
        }
        radioStudente.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(radioStudente.isChecked()){
                    studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.studente));
                }else {
                    studente_docente.setImageDrawable(getResources().getDrawable(R.drawable.docente));
                }

            }
        });

        //link a registrazione
        String stringa="Oppure registrati";
        SpannableString ss=new SpannableString(stringa);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,RegistrazioneActivity.class);
                startActivityForResult(i,1);
            }
        };

        ss.setSpan(clickableSpan1, 7, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txt_toRegistrazione.setText(ss);
        txt_toRegistrazione.setMovementMethod(LinkMovementMethod.getInstance());

        //link a recupero password
        String stringa_topw="Password dimenticata?";
        SpannableString ss_topw=new SpannableString(stringa_topw);

        ClickableSpan clickableSpan_topw = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                final Dialog d = new Dialog(MainActivity.this);
                d.setContentView(R.layout.dialog_recupero_password);
                d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                Spinner dialog_spinner=d.findViewById(R.id.dialog_spinner_uni);
                final EditText dialog_matricola=d.findViewById(R.id.dialog_input_matricola);
                final EditText dialog_mail=d.findViewById(R.id.dialog_input_mail);
                Button btn_recupera=d.findViewById(R.id.btn_recupera_pw);

                dialog_spinner.setAdapter(adapter);
                dialog_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        universita_recupero = (Universita) parent.getItemAtPosition(position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                btn_recupera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rec_matricola=dialog_matricola.getText().toString().trim();
                        rec_mail=dialog_mail.getText().toString().trim();
                        if(universita_recupero==null){
                            MyToast.makeText(getApplicationContext(),"Per favore, seleziona un'università", false).show();
                            return;
                        }
                        if(universita_recupero==null){
                            MyToast.makeText(getApplicationContext(),"Per favore, seleziona un'università", false).show();
                            return;
                        }
                        if(rec_matricola==null || rec_matricola.equals("") || rec_mail==null || rec_mail.equals("")){
                            MyToast.makeText(getApplicationContext(),"Per favore, inserisci tutti i campi", false).show();
                            return;
                        }
                        else {
                            new recuperoPassword().execute();
                            d.dismiss();
                        }
                    }
                });
                d.show();
            }
        };
        ss_topw.setSpan(clickableSpan_topw, 0, stringa_topw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt_toPassword.setText(ss_topw);
        txt_toPassword.setMovementMethod(LinkMovementMethod.getInstance());

        //funzione bottone
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matricola=txtMatricola.getText().toString().trim();
                password=txtPassword.getText().toString().trim();

                //controllo campi vuoti
                if(matricola.equals("")||password.equals("")){
                    MyToast.makeText(getApplicationContext(),"Devi inserire tutti i campi!",false).show();
                    return;
                }
                if(radioStudente.isChecked()){
                    isStudente=true;
                }
                else isStudente=false;

                new loginUtente().execute();
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        //ottengo token da Firebase
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this,new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken(); //salvo token in variabile globale
            }
        });

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        is_logged=settings.getBoolean("logged", false);
        is_studente = settings.getBoolean("studente", false);

        //se non ho fatto logout reindirizzo a Home, altrimenti riempio spinner universita
        if(is_logged==true&&is_studente==true){
            Intent i=new Intent(MainActivity.this, Home.class);
            i.putExtra("start_from_login",false);
            startActivityForResult(i,-1);
            return;
        }
        else if(is_logged==true&&is_studente==false){
            Intent i=new Intent(MainActivity.this, HomeDocente.class);
            i.putExtra("start_from_login",false);
            startActivityForResult(i,23);
            return;
        }
        else new riempiUniversita().execute();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restart();
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        is_logged=settings.getBoolean("logged", false);
        if(is_logged==true) finish();
    }

    private void restart(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this,new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken(); //salvo token in variabile globale
            }
        });
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
        txt_actionbar.setText("LOGIN");
        image_actionbar.setImageDrawable(getResources().getDrawable(R.drawable.logo_size));
    }


    //TASK ASINCRONO PER RIEMPIRE SPINNER UNIVERSITA
    private class riempiUniversita extends AsyncTask<Void, Void, Universita[]> {
        @Override
        protected Universita[] doInBackground(Void... strings) {
            try {
                URL url = new URL(URL_UNIVERSITA);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
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
                    array_universita[i].setLast_update(json_data.getString("last_update"));
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
                MyToast.makeText(getApplicationContext(),"Sei offline! Connettititi ad una rete per effettuare il login",false).show();
                return;
            }
            adapter = new ArrayAdapter(MainActivity.this, R.layout.simple_custom_list_item, array_universita);
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
    //TASK ASINCRONO PER LOGIN UTENTE
    private class loginUtente extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... strings) {
            try {
                URL url=new URL(URL_LOGIN);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "universita=" + URLEncoder.encode(universita.getCodice(), "UTF-8")
                        + "&matricola=" + URLEncoder.encode(matricola, "UTF-8")
                        + "&password=" + URLEncoder.encode(password, "UTF-8")
                        + "&token=" + URLEncoder.encode(token, "UTF-8")
                        + "&flag_studente=" + URLEncoder.encode(""+isStudente, "UTF-8");
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
                User user=null;
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    if(isStudente==true) {
                        user = new User(json_data.getString("matricola"),
                                json_data.getString("nome"),
                                json_data.getString("cognome"),
                                json_data.getString("codice_universita"), json_data.getString("mail"),
                                json_data.getString("password"), true);
                    }
                    else{
                        user = new User(json_data.getString("matricola"),
                                json_data.getString("nome"),
                                json_data.getString("cognome"),
                                json_data.getString("codice_universita"), json_data.getString("mail"),
                                json_data.getString("password"), false);
                    }
                }
                return user;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if(user==null) {
                MyToast.makeText(getApplicationContext(),"Errore: impossibile effettuare il login!",false).show();
                return;
            }
            else{
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("universita",universita.getCodice());
                editor.putString("nome_universita",universita.getNome());
                editor.putString("matricola",user.getMatricola());
                editor.putString("nome", user.getNome());
                editor.putString("cognome", user.getCognome());
                editor.putString("token", token);
                editor.putBoolean("logged", true);
                if(user.isStudente()==true) {
                    editor.putString("latitudine", ""+universita.getLatitudine());
                    editor.putString("longitudine", ""+universita.getLongitudine());
                    editor.putString("ingresso", ""+universita.getIngresso());
                    editor.putString("pausa", ""+universita.getPausa());
                    editor.putString("slot", ""+universita.getSlot());
                    editor.putString("first_slot", universita.getFirst_slot());
                    editor.putString("last_update", universita.getLast_update());
                    editor.putBoolean("studente", true);
                    editor.commit();
                    Intent i=new Intent(MainActivity.this, Home.class);
                    i.putExtra("start_from_login",true);
                    startActivityForResult(i,2);
                    finish();
                }
                else{
                    editor.putBoolean("studente", false);
                    editor.commit();
                    Intent i=new Intent(MainActivity.this, HomeDocente.class);
                    i.putExtra("start_from_login",true);
                    startActivityForResult(i,3);
                    finish();
                }
            }
        }
    }

    private class recuperoPassword extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... strings) {
            try {
                URL url=new URL(URL_RECUPERA_PASSWORD);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "matricola=" + URLEncoder.encode(rec_matricola, "UTF-8")
                        + "&universita=" + URLEncoder.encode(universita_recupero.getCodice(), "UTF-8")
                        + "&mail=" + URLEncoder.encode(rec_mail, "UTF-8");
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
                Log.e("SimpleHttpURLConnection", e.getMessage());
                return "Impossibile connettersi";
            } finally {
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Impossibile connettersi") || result.equals("ERROR: Could not connect.")) MyToast.makeText(getApplicationContext(), "Sei offline! Connettiti ad una rete per recuperare la password!", false).show();
            else if(result.equals("Utente inesistente: impossibile inviare email")) MyToast.makeText(getApplicationContext(), result, false).show();
            else MyToast.makeText(getApplicationContext(), "E-mail inviata: controlla la tua casella di posta", true).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode== Activity.RESULT_OK){
                studentePassato= (data.getBooleanExtra("isStudente", true));
                txtMatricola.setText(data.getStringExtra("matricola"));
                txtPassword.setText(data.getStringExtra("password"));

                if(studentePassato==true){
                    radioStudente.setChecked(true);
                    radioDocente.setChecked(false);
                }
                else{
                    radioDocente.setChecked(true);
                    radioStudente.setChecked(false);
                }
                MyToast.makeText(getApplicationContext(),"Registrazione avvenuta con successo! Effettua Login per accedere alla pagina personale!",true).show();
            }
        }
    }


    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Aggiorna");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            restart();
        }
        return true;
    }

}