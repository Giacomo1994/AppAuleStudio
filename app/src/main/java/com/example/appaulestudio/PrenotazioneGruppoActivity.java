package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PrenotazioneGruppoActivity extends AppCompatActivity {
    String params;

    Spinner spinnerTavoli;
    CheckBox checkBoxFascia;
    ListView listaDisponibilitaActivity;
    ListAdapter adapterDisponibilita;
    LocalTime primaFascia, ultimaFascia;
    public DatePickerDialog.OnDateSetListener listener;
    TextView txtDataMostrata;
    int anno,mese,giorno;
    int giornoSelezionatoInt;
    int slotMin;
    String primoSlot;
    Button btnIscriviti, btnHome;
    Button btnCercaDisponibilita;
    String studente;
    ArrayAdapter adapterComponenti;
    ArrayAdapter adapterSpinner;
    String risultato,risultato2; int n;
    Button btnGruppo, btnComponenti, btncheckComponenti;
    ListView listacomponenti;
    TextView gruppoSelezionato;
    TextView txtNomeComponente, txtCognomeComponente;
    CheckBox checkComponente;
    Dialog d,dialogCheckComponenti;
    TextView nomeAula, output, componenti, txtStatoAula;
    Button btnIndietro,btnAvanti;
    TextView titoloDialogErrore;
    TextView txtOreResidue, txtOreResidueNumero;
    SubsamplingScaleImageView piantaAula;
    ArrayList<Orario_Ufficiale> orariUfficiali;
    String nomeStudente, matricolaStudente,codiceUniversita, cognomeStudente;
    Aula aula;
    Intent intent;
    ArrayList<Tavolo> tavoli;
    ArrayList<Tavolo> tavoliEffettivi;
    ArrayList<Prenotazione> prenotazioni;
    Button btnPrenota;
    static final String URL_TAVOLI="http://pmsc9.altervista.org/progetto/prenotazioni_gruppi_tavoli.php";
    static final String URL_GRUPPI_DA_MATRICOLA="http://pmsc9.altervista.org/progetto/gruppi_da_matricola.php";
    static final String URL_COMPONENTI_DA_GRUPPO="http://pmsc9.altervista.org/progetto/componenti_gruppo.php";
    static final String URL_PRENOTAZIONI_FUTURE="http://pmsc9.altervista.org/progetto/prenotazioni_gruppi_future.php";
    static final String URL_PRENOTAZIONE_GRUPPI="http://pmsc9.altervista.org/progetto/prenotazione_gruppi_prenota.php";
    GridView grigliaGruppi;
    Gruppo[] array_gruppo;
    User[] array_componenti;
    User[] array_dinamico, array_copia;
    Gruppo gruppo,provaGruppo;
    HashMap<Integer,Tavolo> tavoliMap;
    ArrayList<String> fasceDinamico;
    ArrayList<String> fasce;
    ArrayList<Tavolo> tavoliDinamico;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prenotazione_gruppo);

        initUI();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initUI(){
        btnPrenota=findViewById(R.id.btnPrenota);
        listaDisponibilitaActivity=findViewById(R.id.listaDisponibilitaActivity);
        fasceDinamico=new ArrayList<>();
        tavoliDinamico=new ArrayList<>();
        array_gruppo=null;
        tavoliMap= new HashMap<>();
        tavoli=new ArrayList<Tavolo>();
        tavoliEffettivi=new ArrayList<>();
        prenotazioni=new ArrayList<>();
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
        primaFascia=null;ultimaFascia=null;

        intent =getIntent();
        Bundle bundle=intent.getBundleExtra("dati");
        aula=bundle.getParcelable("aula");
        nomeAula.setText(aula.getNome());
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        nomeStudente=settings.getString("nome", null);
        cognomeStudente=settings.getString("cognome",null);
        matricolaStudente=settings.getString("matricola", null);
        codiceUniversita=settings.getString("universita", null);
        slotMin=Integer.parseInt(settings.getString("slot", null));
        primoSlot=settings.getString("first_slot", null);

        setTitle(nomeStudente+" "+cognomeStudente);
        orariUfficiali=bundle.getParcelableArrayList("orari");
        Collections.sort(orariUfficiali);

        new load_image().execute();
        new prendiGruppi().execute();
        btnComponenti=findViewById(R.id.btnComponenti);
        txtOreResidue=findViewById(R.id.txtOreResidue);
        txtOreResidueNumero=findViewById(R.id.txtOreResidueNumero);
        btnGruppo=findViewById(R.id.btnGruppo);

        btnGruppo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tavoliEffettivi.clear();
                riempiLista();
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


        giornoSelezionatoInt=1;
        txtDataMostrata=findViewById(R.id.txtDataMostrata);
        txtStatoAula=findViewById(R.id.txtStatoAula);
        btnAvanti=findViewById(R.id.btnAvanti);
        btnIndietro=findViewById(R.id.btnIndietro);


        Adapter adapter;
        Calendar c = Calendar.getInstance();
        anno=c.get(Calendar.YEAR);
        //il mese parte da zero
        mese=c.get(Calendar.MONTH)+1;
        giorno=c.get(Calendar.DAY_OF_MONTH);
        //output.setText(""+anno+""+mese+""+giorno);
        //txtDataMostrata.setText("Oggi");
        if(giornoSelezionatoInt==1){
            btnIndietro.setVisibility(View.GONE);
        }
        if(giornoSelezionatoInt==6){
            btnAvanti.setVisibility(View.GONE);
        }
        mostraData(giornoSelezionatoInt);
        btnAvanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnIndietro.setVisibility(View.VISIBLE);
                giornoSelezionatoInt++;
                mostraData(giornoSelezionatoInt);
                tavoliEffettivi.clear();
                riempiLista();
            }
        });
        btnIndietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAvanti.setVisibility(View.VISIBLE);
                giornoSelezionatoInt--;
                mostraData(giornoSelezionatoInt);
                tavoliEffettivi.clear();
                riempiLista();
            }
        });


        btnCercaDisponibilita=findViewById(R.id.btnCercaDisponibilita);
        btnCercaDisponibilita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(giornoSelezionatoInt>0) {
                    definiscoOrari();
                    //output.setText(primoSlot+" "+primaFascia.toString()+" "+ultimaFascia);

                    new prendiTavoli().execute();
                    //dopo aver preso tavoli prende prenotazioni e poi crea la mappa con tavoli e fasce orarie
                    //verifico che il numero di posti sia>= a quello che mi serve
                    //smistaPrenotazioni();
                    //riempiLista();


                }


            }
        });


        btnPrenota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tavoliEffettivi.isEmpty()==false && fasceDinamico.isEmpty()==false) {
                    //output.append("Sto prenotando");
                    ArrayList<Tavolo> tavoliPrenotare = new ArrayList<>();
                    for (String s : fasceDinamico) {
                        for (Tavolo t : tavoliDinamico) {
                            if (t.getFasciaOraria().compareTo(s.substring(0, 5)) == 0) {
                                tavoliPrenotare.add(t);

                            }
                            //output.append(" Sub "+s.substring(0,5)+" "+t.getFasciaOraria());
                        }
                    }
                    //output.setText(tavoliPrenotare.toString());
                    ArrayList<Prenotazione> prenotazioni = new ArrayList<>();
                    //creo il vettore di prenotazioni
                    ArrayList<Object> tavoliOrdinati;
                    tavoliOrdinati = new ArrayList<>(tavoliPrenotare.stream().sorted(new Comparator<Tavolo>() {
                        @Override
                        public int compare(Tavolo a, Tavolo b) {
                            return a.getFasciaOraria().compareTo(b.getFasciaOraria());
                        }
                    }).collect(Collectors.toList()));
                    //output.setText(tavoliOrdinati.toString());

                    for (Object t : tavoliOrdinati) {
                        LocalTime LOCALorario_prenotazione = LocalTime.parse(((Tavolo) t).getFasciaOraria());
                        LocalTime LOCALorario_fine_prenotazione = LocalTime.parse(((Tavolo) t).getFasciaOraria()).plusMinutes(slotMin);
                        String orario_prenotazione = orariUfficiali.get(giornoSelezionatoInt).getData() + " " + LOCALorario_prenotazione + ":00";
                        String orario_fine_prenotazione = orariUfficiali.get(giornoSelezionatoInt).getData() + " " + LOCALorario_fine_prenotazione + ":00";
                        Prenotazione p = new Prenotazione(
                                aula.getIdAula(), ((Tavolo) t).getNum_tavolo(), orario_prenotazione,
                                orario_fine_prenotazione, 1);
                        prenotazioni.add(p);
                    }
                    //se riesco si concatena
                    //ArrayList<Prenotazione> prenotazioniFinale = concatenaPrenotazioni(prenotazioni);

                    //output.append(fasceDinamico.toString());
                    //output.append(tavoliPrenotare.toString());
                    if(fasceDinamico.size()!=tavoliPrenotare.size()){
                        //errore
                    }

                    else{
                        //prenota
                        //devo controllare che ci siano ancora abbastanza posti liberi nei tavoli
                        //devo controllare che le persone non abbiano altre prenotazioni in quelle ore
                        //creo i json

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
                        //converto tutto a stringa e passo come unico parametro
                        params=parametri.toString();
                        //output.append(params);
                        new prenotaGruppi().execute();



                    }
                }
                else {
                    MyToast.makeText(getApplicationContext(), "Selezionare le fasce orarie per cui si desidera prenotare!", MyToast.LENGTH_SHORT).show();
                }
            }
        });
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
        btnIscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(PrenotazioneGruppoActivity.this,
                        IscrizioneActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void definiscoOrari(){
        LocalTime apertura=LocalTime.parse(orariUfficiali.get(giornoSelezionatoInt).getApertura());
        LocalTime chiusura=LocalTime.parse(orariUfficiali.get(giornoSelezionatoInt).getChiusura());
        primaFascia=LocalTime.parse(primoSlot);
        ultimaFascia=primaFascia;
        boolean trovata=false;
        while(trovata==false) {
            if (apertura.isAfter(primaFascia)) {
                primaFascia = primaFascia.plusMinutes(slotMin);
                trovata = false;
            } else
                trovata = true;
        }
        if(chiusura.isAfter(ultimaFascia.plusMinutes(slotMin))){
            //non ci sono ore prenotabili
        }
        boolean chiusuraTrovata=false;
        ultimaFascia=ultimaFascia.plusMinutes(slotMin);
        while(chiusuraTrovata==false) {
            if (!chiusura.isBefore(ultimaFascia)) {
                ultimaFascia = ultimaFascia.plusMinutes(slotMin);
                chiusuraTrovata = false;
            } else
                chiusuraTrovata = true;
        }
        ultimaFascia=ultimaFascia.minusMinutes(slotMin);
    }
    public void mostraData(int indiceGiorno){
        //ho array di date e orari
        orariUfficiali.get(indiceGiorno).getData();
        txtDataMostrata.setText(orariUfficiali.get(indiceGiorno).getData());
        if(orariUfficiali.get(indiceGiorno).getApertura()==null){
            txtStatoAula.setText("L'aula è chiusa");
            txtDataMostrata.setTextColor(getResources().getColor(R.color.rosso_scuro));

        }
        else{
            txtStatoAula.setText("L'aula apre alle "+orariUfficiali.get(indiceGiorno).getApertura()+
                    " e chiude alle "+orariUfficiali.get(indiceGiorno).getChiusura());
            txtDataMostrata.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        if(indiceGiorno==1){
            btnIndietro.setVisibility(View.GONE);
        }
        if(indiceGiorno==6){
            btnAvanti.setVisibility(View.GONE);
        }
    }

//COMPONENTI GRUPPO
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
                        tavoliEffettivi.clear();
                        riempiLista();

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
                txtOreResidue.setText("Ore residue:   ");
                txtOreResidueNumero.setText(""+gruppo.getOre_disponibili());
                d.cancel();
                new prendiUtenti().execute();



            }
        });
    }
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

//TAVOLI DISPONIBILI
    public void riempiLista(){
    if(tavoliEffettivi.isEmpty()){
        //MyToast.makeText(getApplicationContext(),"Non ci sono tavoli disponibili nel giorno selezionato con il numero di posti desiderato", MyToast.LENGTH_LONG).show();
    }
    tavoliDinamico.clear();
    fasceDinamico.clear();
    fasce= new ArrayList<>();
    for(Tavolo t: tavoliEffettivi){
        String fascia= t.getFasciaOraria();
        if(!fasce.contains(fascia)){
            fasce.add(fascia);
        }
    }
    //output.append(fasce.toString());
    //output.append("entro in riempi lista");
    adapterDisponibilita = new ArrayAdapter<String>(PrenotazioneGruppoActivity.this, R.layout.row_layout_fasce_disponibili, (List<String>) fasce) {

        //@SuppressLint("ResourceAsColor")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //return
            //super.getView(position, convertView, parent);
            String item = getItem(position);
            ArrayList<Tavolo> tavoliFasciaSelezionata= new ArrayList<>();
            tavoliFasciaSelezionata.clear();
            for(Tavolo t:tavoliEffettivi){
                if(t.getFasciaOraria().compareTo(item)==0){
                    tavoliFasciaSelezionata.add(t);
                    //tavoliDinamico.add(t);
                }
            }
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_fasce_disponibili
                    , parent, false);

            checkBoxFascia = convertView.findViewById(R.id.checkBoxFascia);
            spinnerTavoli = convertView.findViewById(R.id.spinnerTavoli);
            if(position==0){
                checkBoxFascia.setText(item+"-"+LocalTime.parse(item).plusMinutes(slotMin));
            }
            else if(position!=0 && item.compareTo(getItem(position-1))!=0){
                checkBoxFascia.setText(item+"-"+LocalTime.parse(item).plusMinutes(slotMin));
            }

            adapterSpinner = new ArrayAdapter(PrenotazioneGruppoActivity.this, android.R.layout.simple_list_item_1, tavoliFasciaSelezionata);
            spinnerTavoli.setAdapter(adapterSpinner);
            spinnerTavoli.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Tavolo t = (Tavolo) parent.getItemAtPosition(position);
                    String fascia = t.getFasciaOraria();
                        /*if(!tavoliDinamico.isEmpty()) {
                            for (Tavolo tavolo : tavoliDinamico) {
                                if (tavolo.getFasciaOraria() == fascia) {
                                    tavoliDinamico.remove(tavolo);
                                }
                            }
                            tavoliDinamico.add(t);
                        }*/
                    ArrayList<Tavolo> tavoliDaRimuovere=new ArrayList<>();
                    for(Tavolo tavolo:tavoliDinamico){
                        if(fascia.equals(tavolo.getFasciaOraria())){
                            tavoliDaRimuovere.add(tavolo);
                        }
                    }
                    for(Tavolo tavolo:tavoliDaRimuovere){
                        tavoliDinamico.remove(tavolo);
                    }
                    //if(presente==false) {
                    tavoliDinamico.add(t);
                    //}


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            checkBoxFascia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String f=compoundButton.getText().toString();
                    if(b==true){
                        //lo sta aggiungendo
                        if(!fasceDinamico.contains(f)){
                            if((gruppo.getOre_disponibili()*60)<(slotMin*(fasceDinamico.size()+1))){
                                //il gruppo non ha abbastanza ore
                                MyToast.makeText(getApplicationContext(),"Ore residue insufficienti per effettuale la prenotazione", Toast.LENGTH_LONG).show();
                                compoundButton.setChecked(false);
                            }
                            else {
                                fasceDinamico.add(f);
                            }

                        }
                    }
                    if(b==false){
                        //lo sta togliendo
                        if(fasceDinamico.contains(f)){
                            fasceDinamico.remove(f);

                        }
                    }
                    //output.setText(fasceDinamico.toString());
                    //output.append(tavoliDinamico.toString());
                    //output.append(tavoliDinamico.toString());
                }

            });


            return convertView;
        }
    };
    listaDisponibilitaActivity.setAdapter(adapterDisponibilita);


}
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void smistaPrenotazioni(){
        tavoliMap.clear();
        LocalTime fasciaAnalizzata; fasciaAnalizzata=primaFascia;
        int index=0;
        if(prenotazioni.isEmpty()){
            output.append("non ci sono prenotazioni");
            //non ci sono prenotazioni quindi ci sono tutti i posti liberi
            while(fasciaAnalizzata.isBefore(ultimaFascia)){

                for(Tavolo t: tavoli) {

                    Tavolo tAggiornato=new Tavolo(t); tAggiornato.setFasciaOraria(fasciaAnalizzata.toString());
                    tAggiornato.setPosti_liberi(t.getPosti_totali());
                    tavoliMap.put(index, tAggiornato);
                    index++;
                }
                fasciaAnalizzata=fasciaAnalizzata.plusMinutes(slotMin);
            }
        }
        else {
            //ci sono prenotazioni e devo smistarle per fasce orarie
            Tavolo tAggiornato;
            while (fasciaAnalizzata.isBefore(ultimaFascia)) {
                //output.append("\nanalizzo fascia" + fasciaAnalizzata + "\n");
                for (Tavolo t : tavoli) {
                    int occupati = 0;
                    for (Prenotazione p : prenotazioni) {
                        //output.append(p.getOrario_prenotazione()+" "+orariUfficiali.get(giornoSelezionatoInt).getData() + " " + fasciaAnalizzata + ":00"+"\n");
                        // if (p.getOrario_prenotazione().compareTo(orariUfficiali.get(giornoSelezionatoInt).getData()  + " " + fasciaAnalizzata + ":00")<=0
                        //         &&p.getOrario_fine_prenotazione().compareTo(orariUfficiali.get(giornoSelezionatoInt).getData()  + " " + fasciaAnalizzata.plusMinutes(slotMin) + ":00")>=0
                        if( p.getOrario_prenotazione().compareTo(orariUfficiali.get(giornoSelezionatoInt).getData()  + " " + fasciaAnalizzata + ":00")<=0
                                && p.getOrario_fine_prenotazione().compareTo(orariUfficiali.get(giornoSelezionatoInt).getData()  + " " + fasciaAnalizzata.plusMinutes(slotMin) + ":00")>=0
                                && p.getNum_tavolo() == t.getNum_tavolo()) {
                            occupati++;
                        }
                    }
                    tAggiornato = new Tavolo(t);
                    tAggiornato.setFasciaOraria(fasciaAnalizzata.toString());
                    tAggiornato.setPosti_liberi(t.getPosti_totali() - occupati);
                    //output.append("----"+tAggiornato.getNum_tavolo()+" "+tAggiornato.getFasciaOraria()+"\n");
                    tavoliMap.put(index,tAggiornato);
                    index++;
                    //output.append(index+" "+tAggiornato.toString());
                }
                fasciaAnalizzata = fasciaAnalizzata.plusMinutes(slotMin);

            }
            //output.append(tavoliMap.values().toString());
        }
        //output.append(tavoliMap.values().toString());
        tavoliEffettivi= new ArrayList<>(creaListaTavoliFinale(tavoliMap));
        if(tavoliEffettivi.isEmpty()){
            MyToast.makeText(getApplicationContext(), "Non ci sono tavoli disponibili", MyToast.LENGTH_SHORT).show();
        }
        //output.setText(tavoliEffettivi.toString());
        //ora devo stamparli
        riempiLista();
    }
    public ArrayList<Tavolo> creaListaTavoliFinale(HashMap<Integer,Tavolo> tavoliMap){
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
        if(result==null){
            Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Errore Immagine</b></font>"), Toast.LENGTH_LONG).show();
            return;
        }
        piantaAula.setImage(ImageSource.bitmap(result));
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
                MyToast.makeText(getApplicationContext(), "Errore nel caricamento", MyToast.LENGTH_LONG).show();
                return null;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String s) {

            //output.append(prenotazioni.toString());
            if(tavoli==null){
                Toast.makeText(getApplicationContext(), "Nessun tavolo disponibile", Toast.LENGTH_SHORT).show();
            }
            else {
                smistaPrenotazioni();
            }
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
            if (result == null) {//problema di connessione
                Toast.makeText(getApplicationContext(), "Sei offline! Impossibile prenotare!", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                //output.setText(tavoli.toString());
                new prendiPrenotazioni().execute();
            }
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
                riempiLista();
                return;
            }

        }
    }
}
