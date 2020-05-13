package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/*new User(json_data.getString("matricola"),
        json_data.getString("nome"),
        json_data.getString("cognome"),
        json_data.getString("codice_universita"),
        json_data.getString("mail"),
        json_data.getString("password"),
        json_data.getBoolean("studente"));*/
public class PrenotazioneGruppoActivity extends AppCompatActivity {
    Button btnIscriviti, btnHome;
    String studente;
    ArrayAdapter adapterComponenti;
    String risultato; int n;
    Button btnGruppo, btnComponenti, btncheckComponenti;
    ListView listacomponenti;
    TextView gruppoSelezionato;
    TextView txtNomeComponente, txtCognomeComponente, txtUniversitaComponente;
    //TextView txtComponenti;
    CheckBox checkComponente;
    Dialog d,dialogCheckComponenti;
    TextView nomeAula, output, componenti;
    TextView titoloDialogErrore;
    SubsamplingScaleImageView piantaAula;
    ArrayList<Orario_Ufficiale> orariUfficiali;
    String nomeStudente, matricolaStudente,codiceUniversita;
    Aula aula;
    Intent intent;
    static final String URL_GRUPPI_DA_MATRICOLA="http://pmsc9.altervista.org/progetto/gruppi_da_matricola.php";
    static final String URL_COMPONENTI_DA_GRUPPO="http://pmsc9.altervista.org/progetto/componenti_gruppo.php";
    GridView grigliaGruppi;
    Gruppo[] array_gruppo;
    User[] array_componenti;
    User[] array_dinamico, array_copia;
    Gruppo gruppo,provaGruppo;

    public User[] rimuoviUser(User[] array_passato, String matricola){
        String matricolaUtente="";
        //dovrebbe essere sempre uno perchè la matricola è univoca
        int elementiDaRimuovere=0;
        for(int i=0; i<array_passato.length; i++){
            matricolaUtente=array_passato[i].getMatricola();
            if(matricola.equals(matricolaUtente)){
                elementiDaRimuovere++;
            }
        }
        if(elementiDaRimuovere==0){
            return array_passato;
        }
        else{
            int elementiRimasti=array_passato.length-elementiDaRimuovere;
            User[] array_presenti= new User[elementiRimasti];
            int i=0;
            int j=0;
            for (i = 0; i < array_passato.length; i++) {
                matricolaUtente = array_passato[i].getMatricola();
                if (matricola.equals(matricolaUtente)) {
                    j--;
                } else {
                    array_presenti[j] = array_passato[i];
                }
                j++;
            }

            return array_presenti;
        }

    }
    public User[] aggiungiUser(User[] array_passato, String matricola){
        String matricolaAggiungere=matricola;
            //controllo che non ci sia(dovrebbe essere impossibile)
            for(int i=0; i<array_passato.length;i++){
                String matricolaUtente=array_passato[i].getMatricola();
                if(matricolaAggiungere.equals(matricolaUtente)){
                    return array_passato;

                }
            }
            User[] array_presenti= new User[array_passato.length+1];
            for(int i=0; i<array_passato.length;i++){
                array_presenti[i]=array_passato[i];
            }
            for(User s:array_componenti){
               if(s.getMatricola()==matricolaAggiungere){
                   array_presenti[array_presenti.length-1]=s;
                   return array_presenti;
               }
            }

        return null;

    }
    //serve per far checkare le caselle selezionate quando riapro lo scegli gruppo
    public boolean contiene(User[] array_partecipanti, User s){
        //boolean contiene=false;
        String matricola=s.getMatricola();
        for(int i=0; i<array_partecipanti.length;i++){
            if(matricola.equals(array_partecipanti[i].getMatricola())){
                return true;
            }
        }
        return false;
    }

    public void dialogErrore(){
        Dialog dErrore = new Dialog(PrenotazioneGruppoActivity.this);
        dErrore.setTitle("Errore ricerca Gruppi");
        dErrore.setCancelable(false);
        dErrore.setContentView(R.layout.dialog_errore_gruppi);
        btnIscriviti=dErrore.findViewById(R.id.btnIscriviti);
        titoloDialogErrore=dErrore.findViewById(R.id.txtTitoloDialogErrore);
        btnHome=dErrore.findViewById(R.id.btnHome);
        dErrore.show();
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(PrenotazioneGruppoActivity.this,
                        Home.class);
                startActivity(i);
                finish();
            }
        });
    }
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

    private class load_image extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                String uri="http://pmsc9.altervista.org/progetto/immagini/plant_"+aula.getIdAula()+".png";
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                return image;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(Bitmap result) {
            if(result==null){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Errore Immagine</b></font>"), Toast.LENGTH_LONG).show();
                return;
            }
            piantaAula.setImage(ImageSource.bitmap(result));
        }
    }

    private void initUI(){
        array_gruppo=null;
        piantaAula=findViewById(R.id.piantaAula);

        provaGruppo=new Gruppo("h37cg76f0a",
                "gruppo prova4", "3",
                "b", 3,
        10, "2020-08-09");
        componenti=findViewById(R.id.componenti);
        gruppoSelezionato=findViewById(R.id.gruppoSelezionato);
        nomeAula=findViewById(R.id.nomeAula);
        output=findViewById(R.id.output);
        piantaAula=findViewById(R.id.piantaAula);

        intent =getIntent();
        Bundle bundle=intent.getBundleExtra("dati");
        aula=bundle.getParcelable("aula");
        nomeAula.setText(aula.getNome());
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        nomeStudente=settings.getString("nome", null);
        matricolaStudente=settings.getString("matricola", null);
        codiceUniversita=settings.getString("universita", null);
        setTitle(nomeStudente);
        orariUfficiali=bundle.getParcelableArrayList("orari");
        Collections.sort(orariUfficiali);

        new load_image().execute();
        new prendiGruppi().execute();
        btnComponenti=findViewById(R.id.btnComponenti);

        btnGruppo=findViewById(R.id.btnGruppo);

        btnGruppo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(array_gruppo!=null)
                    scegliGruppo();
                //else{
                    //dialogErrore();
                //}
            }
        });

        btnComponenti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ho selezionato il gruppo nel dialog e ho dei componenti
                if(gruppo!=null) {
                    scegliComponenti();
                }
                else{
                    //se non seleziono prima il gruppo o il gruppo non ha utenti o
                    //non andava la connessione quando ho chiesto i partecipanti
                    Toast.makeText(getApplicationContext(), "Seleziona prima un gruppo o" +
                                    " riprova più tardi: connessione lenta",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //new prendiUtenti().execute();

    }

    public void scegliComponenti(){
        //ora uso solo la matricola per la verifica
        dialogCheckComponenti = new Dialog(PrenotazioneGruppoActivity.this);
        dialogCheckComponenti.setTitle("Seleziona il gruppo con cui vuoi studiare");
        dialogCheckComponenti.setCancelable(false);
        dialogCheckComponenti.setContentView(R.layout.dialog_check_componenti);
        btncheckComponenti=dialogCheckComponenti.findViewById(R.id.btnCheckComponenti);
        listacomponenti=dialogCheckComponenti.findViewById(R.id.listaComponenti);
        adapterComponenti= new ArrayAdapter<User>(PrenotazioneGruppoActivity.this,
                R.layout.row_layout_componenti, array_componenti){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                //return super.getView(position, convertView, parent);
                User item = getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_componenti
                        , parent, false);
                //txtComponenti = convertView.findViewById(R.id.txtComponente);
                checkComponente=convertView.findViewById(R.id.checkComponente);
                txtNomeComponente=convertView.findViewById(R.id.txtNomeComponente);
                txtCognomeComponente=convertView.findViewById(R.id.txtCognomeComponente);
                //txtUniversitaComponente=convertView.findViewById(R.id.txtUniversitaComponente);
                //txtComponenti.setText(item.getNome()+" "+item.getCognome());
                if(contiene(array_dinamico, item)==true){
                    checkComponente.setChecked(true);
                    }
                else{
                    checkComponente.setChecked(false);
                }
                checkComponente.setText(item.getMatricola());
                txtNomeComponente.setText(item.getNome());
                txtCognomeComponente.setText(item.getCognome());
                //txtUniversitaComponente.setText(item.getUniversita());
                //all inizio l array dinamico li contiene tutti e sono tutti checked

               checkComponente.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        output.setText("");
                        output.append(array_componenti.length+"COMPONENTI");
                        array_copia=array_dinamico;
                        studente = compoundButton.getText().toString();


                        //se lo toglie
                        if(b==false){
                            //controllo che almeno uno studente rimanga nel gruppo
                            if(array_dinamico.length==1 && array_componenti.length==1){
                                compoundButton.setChecked(true);
                                output.append(array_dinamico.length+"SELEZIONATI");
                                componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
                                Toast.makeText(getApplicationContext(), "Almeno un componente",
                                        Toast.LENGTH_LONG).show();

                            }
                            else if(array_dinamico.length==2){
                                compoundButton.setChecked(true);
                                output.append(array_dinamico.length+"SELEZIONATI");
                                componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
                                Toast.makeText(getApplicationContext(), "Almeno due componenti",
                                        Toast.LENGTH_LONG).show();
                            }
                            //tolgo lo studente
                            else {

                                //output.setText(studente);
                                array_dinamico = rimuoviUser(array_copia, studente);
                                output.append(array_dinamico.length+"SELEZIONATI");
                                componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
                                if (array_dinamico.length == 0) {

                                    Toast.makeText(getApplicationContext(), "Seleziona almeno un componente",
                                            Toast.LENGTH_LONG).show();
                                }
                                else {
                                    /*output.setText("");
                                    for (User s : array_dinamico) {
                                        output.setText(s.getMatricola());
                                    }*/
                                }


                            }
                        }
                        //se lo inserisce
                        else{
                            array_dinamico=aggiungiUser(array_copia,studente);
                            output.append("\ninserisco "+array_dinamico.length+"SELE\n");
                            componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
                            for (User s : array_dinamico) {
                                output.append(s.getMatricola());
                            }
                        }
                    }
                });
                return convertView;

            }

        };
        listacomponenti.setAdapter(adapterComponenti);
        dialogCheckComponenti.show();
        //bottone di OK del dialog
        btncheckComponenti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCheckComponenti.cancel();
            }
        });
    }


    public void scegliGruppo(){
        d = new Dialog(PrenotazioneGruppoActivity.this);
        d.setTitle("Seleziona il gruppo con cui vuoi studiare");
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_scegli_gruppo);
        grigliaGruppi=d.findViewById(R.id.grigliaGruppi);

        GridViewAdapter booksAdapter = new GridViewAdapter(PrenotazioneGruppoActivity.this, array_gruppo);
        grigliaGruppi.setAdapter(booksAdapter);


        d.show();
        grigliaGruppi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //prendo il gruppo che lo studente seleziona
                gruppo= new Gruppo(array_gruppo[i]);
                gruppoSelezionato.setText(gruppo.getNome_gruppo());
                d.cancel();
                new prendiUtenti().execute();



            }
        });
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
                risultato=result;
                if(risultato==null){
                    array_gruppo=null;
                    return array_gruppo;
                }

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
            if (array_gruppo.length==0) {
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Nessun gruppo disponibile</b></font>"), Toast.LENGTH_LONG).show();
                dialogErrore();
                //componenti.setText(risultato+array_gruppo.length);
                return;
            }
            //ci sono dei gruppi quindi facciamo subito selezionare uno
            scegliGruppo();
            //componenti.setText(risultato+array_gruppo.length);
            return;
            //componenti.setText(array_gruppo.length);
            /*Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Gruppi disponibili</b></font>"), Toast.LENGTH_LONG).show();

            for (Gruppo g:array_gruppo){
                output.append(g.getCodice_gruppo());
            }*/


        }
    }

    private class prendiUtenti extends AsyncTask<Void, Void, User[]> {

        @Override
        protected User[] doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_COMPONENTI_DA_GRUPPO);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "codice_gruppo="+URLEncoder.encode(gruppo.getCodice_gruppo(), "UTF-8");

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

                array_componenti = new User[jArrayCorsi.length()];

                n=array_componenti.length;
                for (int i = 0; i < jArrayCorsi.length(); i++) {
                    JSONObject json_data = jArrayCorsi.getJSONObject(i);
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
        protected void onPostExecute(User[] array_componenti) {
            super.onPostExecute(array_componenti);
            if (array_componenti == null) {
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Nessun utente disponibile</b></font>"), Toast.LENGTH_LONG).show();

                return;
            }
            //iniazializzo l'arrau degli effettivi partecipanti
            array_dinamico=array_componenti;
            componenti.setText(+array_componenti.length+"/"+array_componenti.length);
            //componenti.setText(risultato+" "+n);
            for(User s: array_componenti) {
                //componenti.append(s.getNome()+"\n");
            }
        }
    }


    /*if(array_gruppo==null){
            Dialog dErrore = new Dialog(PrenotazioneGruppoActivity.this);
            dErrore.setTitle("Errore ricerca Gruppi");
            dErrore.setCancelable(false);
            dErrore.setContentView(R.layout.dialog_errore_gruppi);
            btnIscriviti=dErrore.findViewById(R.id.btnIscrviti);
            btnHome=dErrore.findViewById(R.id.btnHome);
            dErrore.show();
            btnHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i= new Intent(PrenotazioneGruppoActivity.this,
                            Home.class);
                    startActivity(i);
                    finish();
                }
            });
        }*/



}









