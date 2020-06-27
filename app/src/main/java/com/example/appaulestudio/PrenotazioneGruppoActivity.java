package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
public class PrenotazioneGruppoActivity extends AppCompatActivity {
    static final String URL_TAVOLI="http://pmsc9.altervista.org/progetto/prenotazioni_gruppi_tavoli.php";
    static final String URL_GRUPPI_DA_MATRICOLA="http://pmsc9.altervista.org/progetto/gruppi_da_matricola.php";
    static final String URL_COMPONENTI_DA_GRUPPO="http://pmsc9.altervista.org/progetto/componenti_gruppo.php";
    static final String URL_PRENOTAZIONI_FUTURE="http://pmsc9.altervista.org/progetto/prenotazioni_gruppi_future.php";
    static final String URL_PRENOTAZIONE_GRUPPI="http://pmsc9.altervista.org/progetto/prenotazione_gruppi_prenota.php";

    boolean mostra_dialog_data=false;
    TableLayout ll_form;
    LinearLayout ll_btn;
    SubsamplingScaleImageView piantaAula;
    Calendar cal_primaFascia, cal_ultimaFascia;
    Spinner spinnerTavoli;
    CheckBox checkBoxFascia;
    ListView listacomponenti;
    ListAdapter adapterDisponibilita;
    TextView nomeAula, output, componenti, txtOreResidueNumero,txtDataMostrata, gruppoSelezionato,txtNomeComponente, txtCognomeComponente;
    int giornoSelezionatoInt,slotMin,n;
    boolean img_mostrata=false;
    Button btnCercaDisponibilita, btnData;
    ArrayAdapter adapterComponenti,adapterSpinner;
    Button btnGruppo, btnComponenti, btncheckComponenti;
    CheckBox checkComponente;
    Dialog dialogCheckComponenti;
    ArrayList<Orario_Ufficiale> orariUfficiali;
    String studente,risultato,risultato2,primoSlot,nomeStudente, matricolaStudente,codiceUniversita, nomeUniversita, cognomeStudente, params;
    Aula aula;
    Intent intent;
    Gruppo[] array_gruppo;
    User[] array_componenti,array_dinamico, array_copia;
    Gruppo gruppo=null;
    HashMap<Integer,Tavolo> tavoliMap;
    ArrayList<String> fasceDinamico,fasce, fasceOrarie;
    ArrayList<Tavolo> tavoliDinamico,tavoli,tavoliEffettivi;
    ArrayList<Prenotazione> prenotazioni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazione_gruppo);
        initUI();
    }

    private void initUI(){
        mostra_dialog_data=true;
        btnData=findViewById(R.id.btnData);
        btnComponenti=findViewById(R.id.btnComponenti);
        txtOreResidueNumero=findViewById(R.id.txtOreResidueNumero);
        btnGruppo=findViewById(R.id.btnGruppo);
        componenti=findViewById(R.id.componenti);
        gruppoSelezionato=findViewById(R.id.gruppoSelezionato);
        nomeAula=findViewById(R.id.nomeAula);
        output=findViewById(R.id.output);
        piantaAula=findViewById(R.id.piantaAula);
        ll_form=findViewById(R.id.tl_form_pren_gruppo);
        ll_btn=findViewById(R.id.ll_btn_pren_gruppo);

        fasceDinamico=new ArrayList<>();
        tavoliDinamico=new ArrayList<>();
        array_gruppo=null;
        tavoliMap= new HashMap<>();
        tavoli=new ArrayList<Tavolo>();
        tavoliEffettivi=new ArrayList<>();
        prenotazioni=new ArrayList<>();

        //intent
        intent =getIntent();
        Bundle bundle=intent.getBundleExtra("dati");
        aula=bundle.getParcelable("aula");
        nomeAula.setText(aula.getNome());
        orariUfficiali=bundle.getParcelableArrayList("orari");
        Collections.sort(orariUfficiali);

        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        nomeStudente=settings.getString("nome", null);
        cognomeStudente=settings.getString("cognome",null);
        matricolaStudente=settings.getString("matricola", null);
        codiceUniversita=settings.getString("universita", null);
        nomeUniversita=settings.getString("nome_universita", null);
        slotMin=Integer.parseInt(settings.getString("slot", null));
        primoSlot=settings.getString("first_slot", null);
        action_bar();

        //scegli gruppo
        btnGruppo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(array_gruppo!=null) dialogGruppo();
                else new prendiGruppi().execute();
            }
        });

        //scegli componenti
        btnComponenti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gruppo==null) MyToast.makeText(getApplicationContext(), "Seleziona prima un gruppo!", false).show();
                else if(array_componenti==null) new prendiUtenti().execute();
                else dialogComponenti();
            }
        });


        //scegli data
        Calendar time_now=Calendar.getInstance();
        String string_now=new SimpleDateFormat("HH:mm:ss", Locale.ITALY).format(time_now.getTime());
        if(orariUfficiali.get(0).getApertura()==null || string_now.compareTo(orariUfficiali.get(0).getApertura())>=0) orariUfficiali.remove(0);
        else orariUfficiali.remove(orariUfficiali.size()-1);
        giornoSelezionatoInt=-1;
        txtDataMostrata=findViewById(R.id.txtDataMostrata);
        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogData();
            }
        });

        //cerca disponibilità e prenota
        btnCercaDisponibilita=findViewById(R.id.btnCercaDisponibilita);
        btnCercaDisponibilita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gruppo==null) MyToast.makeText(getApplicationContext(),"Per favore, seleziona un gruppo!", false).show();
                else if(array_componenti==null) MyToast.makeText(getApplicationContext(),"Per favore, seleziona dei partecipanti!", false).show();
                else if(giornoSelezionatoInt==-1) MyToast.makeText(getApplicationContext(),"Per favore, seleziona una data!", false).show();
                else{
                    Log.i("myLog", "\n"+gruppo.getNome_gruppo()+"\n"+ Arrays.toString(array_dinamico)+"\n"+orariUfficiali.get(giornoSelezionatoInt).getData());
                    definiscoOrari();
                    new prendiTavoli().execute();
                }

            }
        });

        //task iniziali
        if(chiusa_tutta_la_settimana()==true) dialogWarning("L'aula è chiusa tutta la settimana\nNon ci sono date disponibili per la prenotazione");
        start();
    }

    private void start(){ //metodo per aggiornare la pagina
        mostra_dialog_data=true;
        if(!img_mostrata) new load_image().execute(); //se non è riuscito a scaricare l'iimmagine allora la scarica
        if(array_gruppo==null ) new prendiGruppi().execute(); //se non è riuscito a scaricare i gruppi allora li scarica e mostra dialog
        else if(gruppo==null) dialogGruppo(); //se è riuscito a scaricarli ma non ne ho selezionato uno allora mostro solo dialog senza scaricare
        else if(array_dinamico==null) new prendiUtenti().execute(); //se non è riuscito a scaricare componenti allora li scarica e mostra dialog
        else if(giornoSelezionatoInt==-1) dialogData();
    }

    @SuppressLint("WrongConstant")
    public void action_bar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar =view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText("Prenotazione gruppo");
        final Dialog d = new Dialog(PrenotazioneGruppoActivity.this);
        d.setCancelable(true);
        d.setContentView(R.layout.dialog_user);
        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        TextView txt_nome=d.findViewById(R.id.txt_dialog_user_nome);
        txt_nome.setText(nomeStudente+" "+cognomeStudente);
        TextView txt_matricola=d.findViewById(R.id.txt_dialog_user_matricola);
        txt_matricola.setText(matricolaStudente);
        TextView txt_universita=d.findViewById(R.id.txt_dialog_user_università);
        txt_universita.setText(nomeUniversita);
        Button btn_logout=d.findViewById(R.id.btn_logout);
        Button btn_continue=d.findViewById(R.id.btn_continue);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("logged", false);
                editor.commit();
                Intent i = new Intent(PrenotazioneGruppoActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        image_actionbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.show();
            }
        });
    }


    //GRUPPO
    private boolean contiene(User[] array_partecipanti, User s){
        //boolean contiene=false;
        String matricola=s.getMatricola();
        for(int i=0; i<array_partecipanti.length;i++){
            if(matricola.equals(array_partecipanti[i].getMatricola())){
                return true;
            }
        }
        return false;
    }
    private User[] rimuoviUser(User[] array_passato, String matricola){
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
    private User[] aggiungiUser(User[] array_passato, String matricola){
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
    private boolean oreDisponibili(){
        int ore_selezionate=0;
        for(String f:fasceDinamico){
            ore_selezionate+=slotDifference(f.substring(0,5),f.substring(6,11));
        }
        if(ore_selezionate>gruppo.getOre_disponibili()*60) return false;
        return true;
    }

    //TAVOLI DISPONIBILI
    private void smistaPrenotazioni(){
        tavoliMap.clear();
        int index=0;
        //se non ci sono prenotazioni allora tutti i tavoli sono liberi
        if(prenotazioni.isEmpty()){
            for(int i=0;i<=fasceOrarie.size()-2;i++){
                String fascia=fasceOrarie.get(i);
                for(Tavolo t: tavoli) {
                    Tavolo tAggiornato=new Tavolo(t);
                    tAggiornato.setFasciaOraria(fascia);
                    tAggiornato.setPosti_liberi(t.getPosti_totali());
                    tavoliMap.put(index, tAggiornato);
                    index++;
                }
            }
        }
        else {
            Tavolo tAggiornato;
            //guardo per ogni fascia oraria tranne l'ultima quante prenotazioni ci sono per ogni tavolo
            for(int i=0;i<=fasceOrarie.size()-2;i++){
                String fascia=fasceOrarie.get(i);
                for (Tavolo t : tavoli) {
                    int occupati = 0;
                    for (Prenotazione p : prenotazioni) {
                        if(p.getOrario_prenotazione().compareTo(orariUfficiali.get(giornoSelezionatoInt).getData()  + " " + fascia)<=0
                           && p.getOrario_fine_prenotazione().compareTo(orariUfficiali.get(giornoSelezionatoInt).getData()  + " " + fascia)>0
                           && p.getNum_tavolo() == t.getNum_tavolo()) occupati++;
                    }
                    tAggiornato = new Tavolo(t);
                    tAggiornato.setFasciaOraria(fascia);
                    tAggiornato.setPosti_liberi(t.getPosti_totali() - occupati);
                    tavoliMap.put(index,tAggiornato);
                    index++;
                }
            }
        }
        tavoliEffettivi= new ArrayList<>(creaListaTavoliFinale(tavoliMap));
        if(tavoliEffettivi.isEmpty()){
            MyToast.makeText(getApplicationContext(), "Non ci sono tavoli disponibili", false).show();
        }
        else dialogLista();
    }
    private ArrayList<Tavolo> creaListaTavoliFinale(HashMap<Integer,Tavolo> tavoliMap){
        ArrayList<Tavolo> tavoli= new ArrayList<>();
        for(Tavolo t: tavoliMap.values()){
            if(t.getPosti_liberi()>=array_dinamico.length){
                tavoli.add(t);
            }
        }
        return tavoli;
    }

    //TASK ASINCRONI
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
            if(result!=null){
                piantaAula.setImage(ImageSource.bitmap(result));
                img_mostrata=true;
                piantaAula.setVisibility(View.VISIBLE);
            }

        }
    }

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
                risultato= result;
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
                            json_data.getDouble("ore_disponibili"),
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
            if(array_gruppo==null) dialogWarning("Sei offline!\nImpossibile procedere!");
            else if (array_gruppo.length==0) dialogWarning("Impossibile procedere!\nNon sei iscritto ad alcun gruppo");
            else{
                ll_form.setVisibility(View.VISIBLE);
                ll_btn.setVisibility(View.VISIBLE);
                dialogGruppo();
            }
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
                MyToast.makeText(getApplicationContext(),"Errore nel caricamento dei partecipanti: riprova!", false).show();
                componenti.setText("");
                PrenotazioneGruppoActivity.this.array_componenti=null;
                array_dinamico=null;
                return;
            }
            array_dinamico=array_componenti;
            componenti.setText(+array_componenti.length+"/"+array_componenti.length);
            dialogComponenti();
        }
    }

    private class prendiPrenotazioni extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_PRENOTAZIONI_FUTURE);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(4000);
                urlConnection.setConnectTimeout(4000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "id_aula="+URLEncoder.encode(aula.getIdAula(), "UTF-8")+
                        "&data="+URLEncoder.encode(orariUfficiali.get(giornoSelezionatoInt).getData(), "UTF-8");

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
                risultato2=result;


                JSONArray jArray = new JSONArray(result);


                prenotazioni.clear();
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Prenotazione p= new Prenotazione(aula.getIdAula(),json_data.getInt("tavolo"),
                            json_data.getString("orario_prenotazione"), json_data.getString("orario_fine_prenotazione"),
                            json_data.getInt("stato"));
                    //if(t.getPosti_liberi()>=array_dinamico.length) tavoli.add(t);
                    prenotazioni.add(p);
                }
                return result;

            } catch (Exception e) {
                MyToast.makeText(getApplicationContext(), "Errore nel caricamento: riprova!", false).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(tavoli==null) MyToast.makeText(getApplicationContext(), "Nessun tavolo disponibile", false).show();
            else smistaPrenotazioni();
        }
    }

    private class prendiTavoli extends AsyncTask<Void, Void, ArrayList<Tavolo>> {
        @Override
        protected ArrayList<Tavolo> doInBackground(Void... voids) {
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

                url = new URL(URL_TAVOLI);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
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
                tavoli.clear();
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Tavolo t = new Tavolo(aula.getIdAula(), json_data.getInt("num_tavolo"), json_data.getInt("posti_totali"), json_data.getInt("posti_totali"),"");
                    tavoli.add(t);
                }
                return tavoli;
            } catch (Exception e) {
                return null;
            }
        }
        protected void onPostExecute(ArrayList<Tavolo> result) {
            if (result == null) MyToast.makeText(getApplicationContext(), "Sei offline! Riprova!", false).show();
            else new prendiPrenotazioni().execute();
        }
    }

    private class prenotaGruppi extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_PRENOTAZIONE_GRUPPI);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(7000);
                urlConnection.setConnectTimeout(7000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "parametri="+URLEncoder.encode(params, "UTF-8");
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(parametri); //passo i parametri
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
                String result = new String(baos.toByteArray());
                return result;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            if(result==null){
                MyToast.makeText(getApplicationContext(), "Impossibile connettersi, riprovare più tardi.", false).show();
                return;
            }
            else if(result.equals("Prenotazione avvenuta con successo")){
                MyToast.makeText(getApplicationContext(), "Prenotazione avvenuta con successo!", true).show();
                Intent i= new Intent(PrenotazioneGruppoActivity.this, PrenotazioniAttiveActivity.class);
                startActivity(i);
                finish();
                return;
            }
            else{
                MyToast.makeText(getApplicationContext(), result+"", false).show();
                tavoliEffettivi.clear();
                return;
            }

        }
    }

    //date e orari
    public void definiscoOrari(){
        fasceOrarie=fasceOrarie();
    }
    private Calendar stringToCalendar(String dateString){
        try{
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format_time=new SimpleDateFormat("HH:mm:ss", Locale.ITALY);
        calendar.setTime(format_time.parse(dateString));
        return calendar;
        } catch (Exception e) {
            return null;
        }
    }
    private String calendarToString(Calendar calendar) {
        SimpleDateFormat format_time=new SimpleDateFormat("HH:mm:ss", Locale.ITALY);
        return format_time.format(calendar.getTime());
    }
    private class Comparatore implements Comparator<Tavolo>{
        @Override
        public int compare(Tavolo a, Tavolo b) {
            return a.getFasciaOraria().compareTo(b.getFasciaOraria());
        }
    }
    private String dataItaliana(String dataStraniera){
        try {
            Date d= new SimpleDateFormat("yyyy-MM-dd").parse(dataStraniera);
            return new SimpleDateFormat("E", Locale.ITALY).format(d).toUpperCase()+" "+dataStraniera.substring(8,10)+"/"+dataStraniera.substring(5,7)+"/"+dataStraniera.substring(0,4);
        } catch (ParseException e) {
           return null;
        }

    }
    private String oreToMinuti(double oreDisponibili){
        int ore_int= (int) oreDisponibili;
        int ore_round= (int) Math.ceil(oreDisponibili);
        if(ore_int==ore_round) return ore_int+"h";
        else{
            int min=(int)((oreDisponibili-(double)ore_int)*60);
            return ore_int+"h "+min+"min";
        }
    }
    private boolean chiusa_tutta_la_settimana(){
        for(Orario_Ufficiale uf:orariUfficiali){
            if(uf.getApertura()==null) continue;
            else return false;
        }
        return true;
    }
    private ArrayList<String> fasceOrarie(){
        ArrayList<String> fasce=new ArrayList<String>();
        Calendar cal_apertura=stringToCalendar(orariUfficiali.get(giornoSelezionatoInt).getApertura());
        Calendar cal_chiusura=stringToCalendar(orariUfficiali.get(giornoSelezionatoInt).getChiusura());
        cal_primaFascia=stringToCalendar(primoSlot);
        cal_ultimaFascia= (Calendar) cal_primaFascia.clone();
        boolean trovata=false;
        while(trovata==false) {
            if (cal_apertura.after(cal_primaFascia)) {
                cal_primaFascia.add(Calendar.MINUTE,slotMin);
                trovata = false;
            } else trovata = true;
        }
        boolean chiusuraTrovata=false;
        cal_ultimaFascia.add(Calendar.MINUTE,slotMin);
        while(chiusuraTrovata==false) {
            if (!cal_chiusura.before(cal_ultimaFascia)) {
                cal_ultimaFascia.add(Calendar.MINUTE,slotMin);
                chiusuraTrovata = false;
            } else chiusuraTrovata = true;
        }
        cal_ultimaFascia.add(Calendar.MINUTE,-slotMin);

        if(!orariUfficiali.get(giornoSelezionatoInt).getApertura().equals(calendarToString(cal_primaFascia))) fasce.add(orariUfficiali.get(giornoSelezionatoInt).getApertura());
        if(!orariUfficiali.get(giornoSelezionatoInt).getChiusura().equals(calendarToString(cal_ultimaFascia))) fasce.add(orariUfficiali.get(giornoSelezionatoInt).getChiusura());

        boolean trovato=false;
        Calendar cal= (Calendar) cal_primaFascia.clone();
        while(trovato==false){
            if(cal.after(cal_ultimaFascia)) trovato=true;
            else{
                fasce.add(calendarToString(cal));
                cal.add(Calendar.MINUTE,slotMin);
            }
        }
        Collections.sort(fasce);
        return fasce;
    }
    private String slotSuccessivo(String slot){
        for(int i=0;i<fasceOrarie.size();i++){
            if(fasceOrarie.get(i).equals(slot)) return fasceOrarie.get(i+1);
        }
        return null;
    }
    private int slotDifference(String slot1, String slot2){
        Calendar cal1= stringToCalendar(slot1+":00");
        Calendar cal2= stringToCalendar(slot2+":00");
        long difference=cal2.getTimeInMillis()-cal1.getTimeInMillis();
        difference=difference/60000;
        return (int)difference;

    }

    //dialog
    private void dialogData(){
        mostra_dialog_data=false;
        final Dialog dialog_data = new Dialog(PrenotazioneGruppoActivity.this);
        dialog_data.setCancelable(true);
        dialog_data.setContentView(R.layout.dialog_scegli_gruppo);
        dialog_data.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        TextView textView=dialog_data.findViewById(R.id.et_dialog_gruppi);
        textView.setText("Seleziona una data");
        ListView list_data=dialog_data.findViewById(R.id.lv_pren_gruppi);
        ArrayAdapter<Orario_Ufficiale> adpter_gruppi=new ArrayAdapter<Orario_Ufficiale>(PrenotazioneGruppoActivity.this, R.layout.row_layout_scegli_gruppo,orariUfficiali){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Orario_Ufficiale item=getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_scegli_gruppo, parent, false);
                TextView txt_data=convertView.findViewById(R.id.nomeGruppo);
                ImageView img_data=convertView.findViewById(R.id.imgGroup);
                txt_data.setText(dataItaliana(item.getData()));
                img_data.setImageDrawable(getResources().getDrawable(R.drawable.calendario));
                return convertView;
            }
        };
        list_data.setAdapter(adpter_gruppi);
        list_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(orariUfficiali.get(position).getApertura()==null)
                    MyToast.makeText(getApplicationContext(),"L'aula è chiusa nella data selezionata!\n Selezionare un'altra data",false).show();
                else {
                    giornoSelezionatoInt = position;
                    txtDataMostrata.setText(dataItaliana(orariUfficiali.get(position).getData()));
                    dialog_data.dismiss();
                }
            }
        });
        dialog_data.show();
    }
    private void dialogGruppo(){
        final Dialog dialog_gruppi = new Dialog(PrenotazioneGruppoActivity.this);
        dialog_gruppi.setCancelable(true);
        dialog_gruppi.setContentView(R.layout.dialog_scegli_gruppo);
        dialog_gruppi.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        ListView list_gruppi=dialog_gruppi.findViewById(R.id.lv_pren_gruppi);
        ArrayAdapter<Gruppo> adpter_gruppi=new ArrayAdapter<Gruppo>(PrenotazioneGruppoActivity.this, R.layout.row_layout_scegli_gruppo,array_gruppo){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Gruppo item=getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_scegli_gruppo, parent, false);
                TextView txt_nome_gruppi=convertView.findViewById(R.id.nomeGruppo);
                txt_nome_gruppi.setText(item.getNome_gruppo());
                return convertView;
            }
        };
        list_gruppi.setAdapter(adpter_gruppi);
        list_gruppi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gruppo= new Gruppo(array_gruppo[i]);
                gruppoSelezionato.setText(gruppo.getNome_gruppo());
                txtOreResidueNumero.setText(oreToMinuti(gruppo.getOre_disponibili()));
                dialog_gruppi.dismiss();
                new prendiUtenti().execute();
            }
        });
        dialog_gruppi.show();
    }
    private void dialogComponenti(){
        dialogCheckComponenti = new Dialog(PrenotazioneGruppoActivity.this);
        dialogCheckComponenti.setCancelable(true);
        dialogCheckComponenti.setContentView(R.layout.dialog_check_componenti);
        dialogCheckComponenti.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        btncheckComponenti=dialogCheckComponenti.findViewById(R.id.btnCheckComponenti);
        listacomponenti=dialogCheckComponenti.findViewById(R.id.listaComponenti);
        adapterComponenti= new ArrayAdapter<User>(PrenotazioneGruppoActivity.this, R.layout.row_layout_componenti, array_componenti){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                User item = getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_componenti, parent, false);
                checkComponente=convertView.findViewById(R.id.checkComponente);
                txtNomeComponente=convertView.findViewById(R.id.txtNomeComponente);
                txtCognomeComponente=convertView.findViewById(R.id.txtCognomeComponente);
                if(contiene(array_dinamico, item)==true) checkComponente.setChecked(true);
                else checkComponente.setChecked(false);

                checkComponente.setText(item.getMatricola());
                txtNomeComponente.setText(item.getNome());
                txtCognomeComponente.setText(item.getCognome());

                checkComponente.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        array_copia=array_dinamico;
                        studente = compoundButton.getText().toString();

                        //se lo toglie
                        if(b==false){
                            //controllo che almeno uno studente rimanga nel gruppo
                            if(array_dinamico.length==1 && array_componenti.length==1){
                                compoundButton.setChecked(true);
                                componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
                                MyToast.makeText(getApplicationContext(), "Deve essere presente almeno un componente", false).show();

                            }
                            else if(array_dinamico.length==2){
                                compoundButton.setChecked(true);
                                componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
                                MyToast.makeText(getApplicationContext(), "Devono essere presenti almeno due componenti", false).show();
                            }
                            //tolgo lo studente
                            else {
                                array_dinamico = rimuoviUser(array_copia, studente);
                                componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
                                if (array_dinamico.length == 0) MyToast.makeText(getApplicationContext(), "Seleziona almeno un componente", false).show();
                            }
                        }
                        //se lo inserisce
                        else{
                            array_dinamico=aggiungiUser(array_copia,studente);
                            componenti.setText(+array_dinamico.length+"/"+array_componenti.length);
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
                dialogCheckComponenti.dismiss();
                if(mostra_dialog_data==true) dialogData();
            }
        });
    }
    private void dialogLista(){
        tavoliDinamico.clear();
        fasceDinamico.clear();
        fasce= new ArrayList<>();
        for(Tavolo t: tavoliEffettivi){
            String fascia= t.getFasciaOraria();
            if(!fasce.contains(fascia)) fasce.add(fascia);
        }
        final Dialog dialog_prenotazione = new Dialog(PrenotazioneGruppoActivity.this);
        dialog_prenotazione.setCancelable(true);
        dialog_prenotazione.setContentView(R.layout.dialog_prenotazione_gruppo);
        dialog_prenotazione.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        ListView lista_disponibilita=dialog_prenotazione.findViewById(R.id.dialog_lista_disponibilita);
        Button btn_prenota_dialog=dialog_prenotazione.findViewById(R.id.dialog_btn_prenota);
        btn_prenota_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tavoliEffettivi.isEmpty()==false && fasceDinamico.isEmpty()==false) {
                    ArrayList<Tavolo> tavoliPrenotare = new ArrayList<>();
                    for (String s : fasceDinamico) {
                        for (Tavolo t : tavoliDinamico) {
                            if (t.getFasciaOraria().substring(0,5).compareTo(s.substring(0,5)) == 0) tavoliPrenotare.add(t);
                        }
                    }

                    ArrayList<Prenotazione> prenotazioni = new ArrayList<>();
                    ArrayList<Tavolo> tavoliOrdinati=tavoliPrenotare;
                    Collections.sort(tavoliOrdinati,new Comparatore());

                    for (Tavolo t : tavoliOrdinati) {
                        String orario_prenotazione = orariUfficiali.get(giornoSelezionatoInt).getData() + " " + t.getFasciaOraria();
                        String orario_fine_prenotazione = orariUfficiali.get(giornoSelezionatoInt).getData() + " " + slotSuccessivo(t.getFasciaOraria());
                        Prenotazione p = new Prenotazione(aula.getIdAula(), t.getNum_tavolo(), orario_prenotazione, orario_fine_prenotazione, 1);
                        prenotazioni.add(p);
                    }
                    if(fasceDinamico.size()!=tavoliPrenotare.size()){ }
                    else{
                        JSONObject parametri = new JSONObject();
                        //dati generali
                        try {
                            parametri.put("codice_universita", codiceUniversita);
                            parametri.put("id_aula", aula.getIdAula());
                            parametri.put("data", orariUfficiali.get(giornoSelezionatoInt).getData());
                            parametri.put("creatore", matricolaStudente);
                            parametri.put("gruppo", gruppo.getCodice_gruppo());
                            parametri.put("nome_gruppo", gruppo.getNome_gruppo());
                            parametri.put("numero_partecipanti", array_dinamico.length);
                        }
                        catch (JSONException e) {}
                        //dati studenti
                        JSONArray jsonArrayStudenti = new JSONArray();
                        try {
                            for (User u : array_dinamico) {
                                JSONObject studente = new JSONObject();
                                studente.put("matricola", u.getMatricola());
                                jsonArrayStudenti.put(studente);
                            }
                            parametri.put("partecipanti", jsonArrayStudenti);
                        }
                        catch (JSONException e) {}

                        //inserisco slots
                        JSONArray jsonArraySlots = new JSONArray();
                        try {
                            for (Prenotazione p : prenotazioni) {
                                JSONObject slot = new JSONObject();
                                slot.put("tavolo", p.getNum_tavolo());
                                slot.put("inizio", p.getOrario_prenotazione());
                                slot.put("fine", p.getOrario_fine_prenotazione());
                                jsonArraySlots.put(slot);
                            }
                            parametri.put("slots", jsonArraySlots);
                        }
                        catch (JSONException e) {}
                        params=parametri.toString();
                        //output.setText(params);
                        new prenotaGruppi().execute();
                    }
                    dialog_prenotazione.dismiss();
                }
                else MyToast.makeText(getApplicationContext(), "Non hai selezionato nessuna fascia oraria!", false).show();
            }
        });
        adapterDisponibilita = new ArrayAdapter<String>(PrenotazioneGruppoActivity.this, R.layout.row_layout_fasce_disponibili, (List<String>) fasce) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String item = getItem(position);
                ArrayList<Tavolo> tavoliFasciaSelezionata= new ArrayList<>();
                tavoliFasciaSelezionata.clear();
                for(Tavolo t:tavoliEffettivi){
                    if(t.getFasciaOraria().compareTo(item)==0){
                        tavoliFasciaSelezionata.add(t);
                    }
                }
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_fasce_disponibili, parent, false);
                checkBoxFascia = convertView.findViewById(R.id.checkBoxFascia);
                spinnerTavoli = convertView.findViewById(R.id.spinnerTavoli);
                checkBoxFascia.setText(item.substring(0,item.length()-3)+"-"+slotSuccessivo(item).substring(0,item.length()-3));

                adapterSpinner = new ArrayAdapter(PrenotazioneGruppoActivity.this, android.R.layout.simple_list_item_1, tavoliFasciaSelezionata);
                spinnerTavoli.setAdapter(adapterSpinner);
                //aggiungo rimuovo/tavolo
                spinnerTavoli.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Tavolo t = (Tavolo) parent.getItemAtPosition(position);
                        String fascia = t.getFasciaOraria();

                        ArrayList<Tavolo> tavoliDaRimuovere=new ArrayList<>();
                        for(Tavolo tavolo:tavoliDinamico){
                            if(fascia.equals(tavolo.getFasciaOraria())){
                                tavoliDaRimuovere.add(tavolo);
                            }
                        }
                        for(Tavolo tavolo:tavoliDaRimuovere){
                            tavoliDinamico.remove(tavolo);
                        }
                        tavoliDinamico.add(t);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                //aggiungo/rimuovo fascia
                checkBoxFascia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        String f=compoundButton.getText().toString();
                        if(b==true){
                            if(!fasceDinamico.contains(f)){
                                fasceDinamico.add(f);
                                if(oreDisponibili()==false){
                                    MyToast.makeText(getApplicationContext(), "Ore disponibili esaurite", false).show();
                                    compoundButton.setChecked(false);
                                    fasceDinamico.remove(f);
                                }
                            }
                        }
                        if(b==false && fasceDinamico.contains(f) )fasceDinamico.remove(f);
                    }
                });
                return convertView;
            }
        };
        lista_disponibilita.setAdapter(adapterDisponibilita);
        dialog_prenotazione.show();
    }
    private void dialogWarning(final String message){
        final Dialog d = new Dialog(PrenotazioneGruppoActivity.this);
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_warning);
        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        Button btn=d.findViewById(R.id.btn_dialog_warning);
        Button btn_aggiorna=d.findViewById(R.id.btn_dialog_aggiorna);
        TextView txt_warning=d.findViewById(R.id.txt_dialog_warning);
        txt_warning.setText(message);
        if(message.equals("Sei offline!\nImpossibile procedere!")) btn_aggiorna.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PrenotazioneGruppoActivity.this, Home.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                d.dismiss();
            }
        });
        btn_aggiorna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
                d.dismiss();
            }
        });
        d.show();
        return;
    }

    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST+1, "Home");
        menu.add(Menu.FIRST, 2, Menu.FIRST, "Aggiorna");
        menu.add(Menu.FIRST, 3, Menu.FIRST+3, "Gestione Gruppi");
        menu.add(Menu.FIRST, 4, Menu.FIRST+2, "Prenotazioni");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent i = new Intent(this, Home.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        if (item.getItemId() == 2) {
            start();
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
