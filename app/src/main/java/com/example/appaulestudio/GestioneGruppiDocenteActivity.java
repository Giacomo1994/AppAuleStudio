package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;

public class GestioneGruppiDocenteActivity extends AppCompatActivity {
    static final String URL_GRUPPI_DA_CORSO="http://pmsc9.altervista.org/progetto/gruppi_da_corso.php";
    static final String URL_COMPONENTI_DA_GRUPPO="http://pmsc9.altervista.org/progetto/componenti_gruppo.php";
    static final String URL_AGGIORNA_GRUPPO="http://pmsc9.altervista.org/progetto/aggiorna_gruppo.php";
    static final String URL_ELIMINA_COMPONENTI="http://pmsc9.altervista.org/progetto/elimina_componenti_da_gruppo.php";
    static final String URL_ELIMINA_GRUPPO="http://pmsc9.altervista.org/progetto/elimina_gruppo.php";
    TextView output;
    TextView titoloAttivi,titoloInScad,titoloScaduti;
    Boolean booleanAttivi, booleanInScadenza, booleanScaduti;
    String mes2; String gio2;
    String mes, gio;
    FrameLayout frameLayout;
    LinearLayout layoutAttivi, layoutInscadenza,layoutScaduti;
    ListView listaAttivi, listaInScadenza,listaScaduti;
    Button btnInScadenza, btnAttivi, btnScaduti;
    //dialog gestione gruppo
    TextView nomeGruppoDialog;
    ListView listastudenti;
    TextView txtNomeComponente, txtCognomeComponente; CheckBox checkComponente;
    TextView nuovaScadenza;
    EditText oreDaAggiungere;
    TextView oreResidueDailog,dataScadenzaDialog;
    Button btnOk, btnAnnulla, btnNuovaScadenza;
    int anno,mese,giorno;
    double oreUpdateNumerico; String dataUpdate, oreUpdate;
    ArrayAdapter adapterComponenti;
    TextView nomeGruppo, dataScadenza, oreRimanenti, codiceGruppo;
    String codiceUniversita, nomeUniversita, nomeDocente, matricolaDocente, cognomeDocente, password;
    LinearLayout rowGruppiDocente;
    Intent intent;
    Bundle bundle;
    Corso corso;
    Gruppo[] gruppi;
    Gruppo[] gruppiAttivi, gruppiInScadenza,gruppiScaduti;
    ArrayAdapter adapter, adapterInScadenza, adapterScaduti;
    Dialog gestisciGruppoDialog;
    User[] componenti, array_copia, array_dinamico,array_eliminati;
    Gruppo gruppoSelezionato;
    String studente;

    private void initUI(){
        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        codiceUniversita=settings.getString("universita", null);
        nomeUniversita=settings.getString("nome_universita", null);
        matricolaDocente=settings.getString("matricola", null);
        password=settings.getString("password", null);
        nomeDocente=settings.getString("nome", null);
        cognomeDocente=settings.getString("cognome", null);
        titoloAttivi=findViewById(R.id.txtTitoloAttivi);
        titoloInScad=findViewById(R.id.txtTitoloInScad);
        titoloScaduti=findViewById(R.id.txtTitoloScaduti);
        titoloAttivi.setText(corso.getNomeCorso());
        titoloInScad.setText(corso.getNomeCorso());
        titoloScaduti.setText(corso.getNomeCorso());
        output=findViewById(R.id.output);

        new prendiGruppi().execute();
        frameLayout=findViewById(R.id.frameLayout);
        layoutAttivi=findViewById(R.id.layoutAttivi);
        layoutInscadenza=findViewById(R.id.layoutInScadenza);
        layoutScaduti=findViewById(R.id.layoutScaduti);
        listaAttivi=findViewById(R.id.listaAttivi);
        listaInScadenza=findViewById(R.id.listaInScadenza);
        listaScaduti=findViewById(R.id.listaScaduti);

        layoutAttivi.setVisibility(frameLayout.VISIBLE);
        layoutInscadenza.setVisibility(frameLayout.GONE);
        layoutScaduti.setVisibility(frameLayout.GONE);
        btnInScadenza=findViewById(R.id.btnInScadenza);
        btnAttivi=findViewById(R.id.btnAttivi);
        btnScaduti=findViewById(R.id.btnScaduti);
        btnAttivi.setBackgroundResource(R.color.grigio_chiaro);
        booleanAttivi=true;
        registerForContextMenu(listaAttivi);


        //passo da lista a mappa
        btnInScadenza.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnInScadenza.setBackgroundResource(R.color.grigio_chiaro);
                btnAttivi.setBackgroundResource(R.color.background_liste);
                btnScaduti.setBackgroundResource(R.color.background_liste);
                layoutAttivi.setVisibility(frameLayout.GONE);
                layoutScaduti.setVisibility(frameLayout.GONE);
                layoutInscadenza.setVisibility(frameLayout.VISIBLE);
                booleanInScadenza=true;
                booleanAttivi=false; booleanScaduti=false;
                registerForContextMenu(listaInScadenza);
            }

        });
        btnAttivi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnAttivi.setBackgroundResource(R.color.grigio_chiaro);
                btnInScadenza.setBackgroundResource(R.color.background_liste);
                btnScaduti.setBackgroundResource(R.color.background_liste);
                layoutAttivi.setVisibility(frameLayout.VISIBLE);
                layoutScaduti.setVisibility(frameLayout.GONE);
                layoutInscadenza.setVisibility(frameLayout.GONE);
                booleanInScadenza=false;
                booleanAttivi=true; booleanScaduti=false;
                registerForContextMenu(listaAttivi);
            }

        });
        btnScaduti.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                btnScaduti.setBackgroundResource(R.color.grigio_chiaro);
                btnInScadenza.setBackgroundResource(R.color.background_liste);
                btnAttivi.setBackgroundResource(R.color.background_liste);
                layoutAttivi.setVisibility(frameLayout.GONE);
                layoutScaduti.setVisibility(frameLayout.VISIBLE);
                layoutInscadenza.setVisibility(frameLayout.GONE);
                booleanInScadenza=false;
                booleanAttivi=false; booleanScaduti=true;
                registerForContextMenu(listaScaduti);
            }

        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestione_gruppi_dicente_frame_layout);
        intent=getIntent();
        bundle=intent.getBundleExtra("bundle");
        corso=bundle.getParcelable("corso");

        initUI();
        action_bar();

        new prendiGruppi().execute();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new prendiGruppi().execute();
    }

    @SuppressLint("WrongConstant")
    private void action_bar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar =view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText("I miei gruppi");
        final Dialog d = new Dialog(GestioneGruppiDocenteActivity.this);
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_user);
        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        TextView txt_nome=d.findViewById(R.id.txt_dialog_user_nome);
        txt_nome.setText(nomeDocente+" "+cognomeDocente);
        TextView txt_matricola=d.findViewById(R.id.txt_dialog_user_matricola);
        txt_matricola.setText(matricolaDocente);
        TextView txt_universita=d.findViewById(R.id.txt_dialog_user_università);
        txt_universita.setText(nomeUniversita);
        ImageView img_user=d.findViewById(R.id.img_dialog_user);
        img_user.setImageResource(R.drawable.docente);
        Button btn_logout=d.findViewById(R.id.btn_logout);
        Button btn_continue=d.findViewById(R.id.btn_continue);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("logged", false);
                editor.commit();
                Intent i = new Intent(GestioneGruppiDocenteActivity.this, MainActivity.class);
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

//METODI GESTIONE GRUPPI
    public void smistaGruppi(Gruppo[] gruppi){
        int scaduti=0, inScadenza=0, attivi=0;
        Calendar c= Calendar.getInstance();
        Calendar c2=Calendar.getInstance();
        c2.add(Calendar.DAY_OF_MONTH, 14);
        int an,me,gi;
        int an2,me2,gi2;

        an= c.get(Calendar.YEAR);
        me= c.get(Calendar.MONTH)+1;
        gi= c.get(Calendar.DAY_OF_MONTH);

        an2= c2.get(Calendar.YEAR);
        me2= c2.get(Calendar.MONTH)+1;
        gi2= c2.get(Calendar.DAY_OF_MONTH);

        if(me<=9) mes="0"+me;
        else mes = me + "";
        if(gi<=9)gio="0"+gi;
        else  gio = gi + "";
        String dataOggi=an+"-"+mes+"-"+gio;

        if(me2<=9)mes2="0"+me2;
        else mes2 = me2 + "";
        if(gi2<=9) gio2="0"+gi2;
        else gio2 = gi2 + "";
        String dataFra2sett=an2+"-"+mes2+"-"+gio2;

        //smisto i gruppi
        for(Gruppo g:gruppi) {
            if (g.getData_scadenza().compareTo(dataOggi) < 0) scaduti++;
            else if (g.getData_scadenza().compareTo(dataOggi) == 0 || (g.getData_scadenza().compareTo(dataOggi) > 0 && g.getData_scadenza().compareTo(dataFra2sett) <= 0)) inScadenza++;
            else attivi++;
        }
        gruppiAttivi = new Gruppo[attivi];
        gruppiScaduti = new Gruppo[scaduti];
        gruppiInScadenza = new Gruppo[inScadenza];

        int scadutiAggiunti=0, attiviAggiunti=0, inScadenzaAggiunti=0;
        for(Gruppo g:gruppi) {
            if (g.getData_scadenza().compareTo(dataOggi) < 0) {
                gruppiScaduti[scadutiAggiunti]=g;
                scadutiAggiunti++;
            }
            else if (g.getData_scadenza().compareTo(dataOggi) == 0 || (g.getData_scadenza().compareTo(dataOggi) > 0 && g.getData_scadenza().compareTo(dataFra2sett) <= 0)) {
                gruppiInScadenza[inScadenzaAggiunti]=g;
                inScadenzaAggiunti++;
            }
            else {
                gruppiAttivi[attiviAggiunti]=g;
                attiviAggiunti++;
            }
        }
        mostraGruppi();
    }

    public void mostraGruppi(){
        if(gruppiAttivi!=null) {
            adapter = new ArrayAdapter<Gruppo>(GestioneGruppiDocenteActivity.this, R.layout.row_layout_lista_gruppi_docente, gruppiAttivi) {

                //@SuppressLint("ResourceAsColor")
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    //return super.getView(position, convertView, parent);
                    Gruppo item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_lista_gruppi_docente
                            , parent, false);
                    nomeGruppo = convertView.findViewById(R.id.nomeGruppo);
                    dataScadenza = convertView.findViewById(R.id.dataScadenza);
                    oreRimanenti = convertView.findViewById(R.id.oreRimanenti);
                    codiceGruppo=convertView.findViewById(R.id.codiceGruppo);
                    rowGruppiDocente = convertView.findViewById(R.id.rowGruppiDocente);
                    nomeGruppo.setText(item.getNome_gruppo());
                    dataScadenza.setText("Scadenza: " + item.getData_scadenza());
                    oreRimanenti.setText("Ore disponibili residue: " + item.getOre_disponibili());
                    codiceGruppo.setText("Codice gruppo: "+item.getCodice_gruppo());
                    //rowGruppiDocente.setBackgroundResource(R.drawable.forma_lista_gruppi_docente);
                    return convertView;
                }
            };
            listaAttivi.setAdapter(adapter);
        }
        if(gruppiInScadenza!=null) {
            adapterInScadenza = new ArrayAdapter<Gruppo>(GestioneGruppiDocenteActivity.this, R.layout.row_layout_lista_gruppi_docente, gruppiInScadenza) {

                //@SuppressLint("ResourceAsColor")
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    //return super.getView(position, convertView, parent);
                    Gruppo item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_lista_gruppi_docente
                            , parent, false);
                    nomeGruppo = convertView.findViewById(R.id.nomeGruppo);
                    dataScadenza = convertView.findViewById(R.id.dataScadenza);
                    oreRimanenti = convertView.findViewById(R.id.oreRimanenti);
                    codiceGruppo=convertView.findViewById(R.id.codiceGruppo);
                    rowGruppiDocente = convertView.findViewById(R.id.rowGruppiDocente);
                    nomeGruppo.setText(item.getNome_gruppo());
                    dataScadenza.setText("Scadenza: " + item.getData_scadenza());
                    oreRimanenti.setText("Ore disponibili residue: " + item.getOre_disponibili());
                    codiceGruppo.setText("Codice gruppo: "+item.getCodice_gruppo());
                    return convertView;
                }
            };
            listaInScadenza.setAdapter(adapterInScadenza);
        }
        if(gruppiScaduti!=null) {
            adapterScaduti = new ArrayAdapter<Gruppo>(GestioneGruppiDocenteActivity.this, R.layout.row_layout_lista_gruppi_docente, gruppiScaduti) {

                //@SuppressLint("ResourceAsColor")
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    //return super.getView(position, convertView, parent);
                    Gruppo item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_lista_gruppi_docente
                            , parent, false);
                    nomeGruppo = convertView.findViewById(R.id.nomeGruppo);
                    dataScadenza = convertView.findViewById(R.id.dataScadenza);
                    oreRimanenti = convertView.findViewById(R.id.oreRimanenti);
                    rowGruppiDocente = convertView.findViewById(R.id.rowGruppiDocente);
                    codiceGruppo=convertView.findViewById(R.id.codiceGruppo);
                    nomeGruppo.setText(item.getNome_gruppo());
                    dataScadenza.setText("Scadenza: " + item.getData_scadenza());
                    oreRimanenti.setText("Ore disponibili residue: " + item.getOre_disponibili());
                    codiceGruppo.setText("Codice gruppo: "+item.getCodice_gruppo());
                    return convertView;
                }
            };
            listaScaduti.setAdapter(adapterScaduti);
        }

        listaAttivi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gruppoSelezionato=gruppiAttivi[i];
                new prendiUtenti().execute();

            }
        });
        listaInScadenza.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gruppoSelezionato=gruppiInScadenza[i];
                new prendiUtenti().execute();

            }
        });
        listaScaduti.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gruppoSelezionato=gruppiScaduti[i];
                new prendiUtenti().execute();

            }
        });

    }

    public void creaDialogGruppo(){
        gestisciGruppoDialog = new Dialog(GestioneGruppiDocenteActivity.this);
        gestisciGruppoDialog.setTitle("Gestione Gruppo");
        gestisciGruppoDialog.setCancelable(true);
        gestisciGruppoDialog.setContentView(R.layout.dialog_gestione_gruppo);
        gestisciGruppoDialog.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        nomeGruppoDialog=gestisciGruppoDialog.findViewById(R.id.nomeGruppo);
        TextView txt_componenti=gestisciGruppoDialog.findViewById(R.id.txt_elimina_componente);
        listastudenti=gestisciGruppoDialog.findViewById(R.id.listaStudenti);
        oreDaAggiungere=gestisciGruppoDialog.findViewById(R.id.oreDaAggiungere);
        nuovaScadenza=gestisciGruppoDialog.findViewById(R.id.nuovaScadenza);
        oreResidueDailog=gestisciGruppoDialog.findViewById(R.id.oreResidueDialog);
        dataScadenzaDialog=gestisciGruppoDialog.findViewById(R.id.dataScadenzaDialog);
        oreResidueDailog.setText("Ore disponibili residue: "+gruppoSelezionato.getOre_disponibili());
        dataScadenzaDialog.setText("Scadenza: "+ gruppoSelezionato.getData_scadenza());
        nuovaScadenza.setText("");
        btnOk=gestisciGruppoDialog.findViewById(R.id.btnOK);
        btnAnnulla=gestisciGruppoDialog.findViewById(R.id.btnAnnulla);
        btnNuovaScadenza=gestisciGruppoDialog.findViewById(R.id.btnNuovaScadenza);
        nomeGruppoDialog.setText(gruppoSelezionato.getNome_gruppo());
        if(componenti.length==0 || componenti==null) txt_componenti.setVisibility(View.GONE);
        if(componenti!=null) {
            adapterComponenti = new ArrayAdapter<User>(GestioneGruppiDocenteActivity.this, R.layout.row_layout_componenti, componenti) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    //return super.getView(position, convertView, parent);
                    User item = getItem(position);
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_componenti, parent, false);
                    checkComponente = convertView.findViewById(R.id.checkComponente);
                    txtNomeComponente = convertView.findViewById(R.id.txtNomeComponente);
                    txtCognomeComponente = convertView.findViewById(R.id.txtCognomeComponente);

                    if (contiene(array_dinamico, item) == true) {
                        checkComponente.setChecked(true);
                    } else {
                        checkComponente.setChecked(false);
                    }
                    checkComponente.setText(item.getMatricola());
                    txtNomeComponente.setText(item.getNome());
                    txtCognomeComponente.setText(item.getCognome());

                    checkComponente.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            array_copia = array_dinamico;
                            studente = compoundButton.getText().toString();
                            //se lo toglie
                            if (b == false) {
                                array_dinamico = rimuoviUser(array_copia, studente);
                                if (array_dinamico.length == 0) {
                                    Toast.makeText(getApplicationContext(), "Non ci sono piu componenti nel gruppo",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                            else array_dinamico = aggiungiUser(array_copia, studente);

                            User[] prova = null;
                            prova = creaArrayEliminati(componenti, array_dinamico);
                            if (prova != null) {
                                for (int c = 0; c < prova.length; c++) {
                                    //output.append(prova[c].getMatricola());
                                }
                            }
                        }
                    });
                    return convertView;
                }
            };
            listastudenti.setAdapter(adapterComponenti);
        }



        gestisciGruppoDialog.show();
        btnNuovaScadenza.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                scegliData();

            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n=oreDaAggiungere.getText().toString().trim();
                double numero;
                double numeroTroncato;
                if(n.equals("")){
                  numero=0;
                  numeroTroncato=0;
                }
                else {
                    try {
                        numero = Double.parseDouble(n);
                        DecimalFormat twoDForm = new DecimalFormat("#.##");
                        numeroTroncato = Double.valueOf(twoDForm.format(numero));
                        //output.setText(numero+" "+numeroTroncato);
                    }
                    catch(NumberFormatException e){
                        Toast.makeText(getApplicationContext(),"Errore formato numero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                oreUpdateNumerico=gruppoSelezionato.getOre_disponibili()+numeroTroncato;
                oreUpdate=""+oreUpdateNumerico;
                dataUpdate=nuovaScadenza.getText().toString().trim();

                if((!dataUpdate.equals("") && !dataUpdate.equals(gruppoSelezionato.getData_scadenza())) || !n.equals("")) {
                    new aggiornaGruppi().execute();
                }
                if(componenti!=null) {
                    array_eliminati = creaArrayEliminati(componenti, array_dinamico);
                    if (array_eliminati != null) {
                        new eliminaComponenti().execute();
                    }
                }
                new prendiGruppi().execute();
                gestisciGruppoDialog.cancel();

            }
        });
        btnAnnulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gestisciGruppoDialog.cancel();
            }
        });
    }

    public void scegliData(){
        DatePickerDialog StartTime;
        try {
            Date newDate= new SimpleDateFormat("yyyy-MM-dd").parse(gruppoSelezionato.getData_scadenza());
            final Calendar newCalendar = Calendar.getInstance();
            newCalendar.setTime(newDate);
            StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int m, int d) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(i, m, d);

                    anno=newDate.get(Calendar.YEAR);
                    mese=newDate.get(Calendar.MONTH)+1;
                    giorno=newDate.get(Calendar.DAY_OF_MONTH);

                    String meseStringa=""+mese;
                    String giornoStringa=""+giorno;
                    if(mese<=9){
                        meseStringa="0"+mese;
                    }
                    if(giorno<=9){
                        giornoStringa="0"+giorno;
                    }
                    nuovaScadenza.setText(anno+"-"+meseStringa+"-"+giornoStringa);
                }
            },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

            StartTime.show();

        } catch (ParseException e) { }

    }

    public User[] creaArrayEliminati(User[]componenti, User[] array_dinamico){
        int eliminati=componenti.length-array_dinamico.length;
        array_eliminati= new User[eliminati];
        if(eliminati==0){
            return null;
        }
        else{
            boolean presente=false;
            int x=0;
            for(int j=0; j<componenti.length; j++){
                presente=false;
                for(int i=0; i<array_dinamico.length; i++){
                    if(array_dinamico[i].getMatricola().equals(componenti[j].getMatricola())){
                        presente=true;
                    }
                }
                if(presente==false){
                    array_eliminati[x]=componenti[j];
                    x++;
                }
            }
            return array_eliminati;
        }
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
        for(User s:componenti){
            if(s.getMatricola()==matricolaAggiungere){
                array_presenti[array_presenti.length-1]=s;
                return array_presenti;
            }
        }

        return null;

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

//TASK ASINCRONI
    private class prendiGruppi extends AsyncTask<Void, Void, Gruppo[]> {

        @Override
        protected Gruppo[] doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_GRUPPI_DA_CORSO);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "codice_universita="+ URLEncoder.encode(codiceUniversita, "UTF-8")+
                        "&matricola_docente="+URLEncoder.encode(matricolaDocente, "UTF-8")+
                        "&codice_corso="+URLEncoder.encode(corso.getCodiceCorso(), "UTF-8");

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
                Gruppo[] array_gruppi = new Gruppo[jArrayCorsi.length()];
                for (int i = 0; i < jArrayCorsi.length(); i++) {
                    JSONObject json_data = jArrayCorsi.getJSONObject(i);
                    array_gruppi[i] = new Gruppo(json_data.getString("codice_gruppo"),
                            json_data.getString("nome_gruppo"),
                            json_data.getString("codice_corso"),
                            json_data.getString("matricola_docente"),
                            json_data.getInt("componenti_max"),
                            json_data.getDouble("ore_disponibili"),
                            json_data.getString("data_scadenza"));
                }
                return array_gruppi;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Gruppo[] array_gruppi) {
            if (array_gruppi==null) {
                MyToast.makeText(getApplicationContext(), "Impossibile contattare il server", false).show();
                return;
            }
            if (array_gruppi.length==0) {
                //MyToast.makeText(getApplicationContext(), "Non ci sono gruppi disponibili", false).show();
                return;
            }
            gruppi=array_gruppi;
            smistaGruppi(gruppi);
        }
    }

    private class prendiUtenti extends AsyncTask<Void, Void, User[]> {
        @Override
        protected User[] doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_COMPONENTI_DA_GRUPPO);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "codice_gruppo="+URLEncoder.encode(gruppoSelezionato.getCodice_gruppo(), "UTF-8");
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
                User[] array_componenti = new User[jArrayCorsi.length()];
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
                componenti=array_componenti;
                MyToast.makeText(getApplicationContext(), "Sei offline! Non puoi aggiornare il gruppo.",false).show();
                //creaDialogGruppo();
                return;
            }
            componenti=array_componenti;
            array_dinamico=componenti;
            if(componenti!=null) creaDialogGruppo();
        }
    }

    private class aggiornaGruppi extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... strings) {
            try {
                URL url;
                url = new URL(URL_AGGIORNA_GRUPPO);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(1000);
                urlConnection.setConnectTimeout(1500);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                if(dataUpdate.equals("")) dataUpdate=gruppoSelezionato.getData_scadenza();
                String parametri = "data_scadenza=" + URLEncoder.encode(dataUpdate, "UTF-8") +
                        "&ore_disponibili=" + URLEncoder.encode(oreUpdate, "UTF-8")+
                        "&codice_gruppo="+ URLEncoder.encode(gruppoSelezionato.getCodice_gruppo(), "UTF-8") +
                        "&nome_gruppo="+ URLEncoder.encode(gruppoSelezionato.getNome_gruppo(), "UTF-8"); //imposto parametri da passare
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
                return null;
            } finally {
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result==null || !result.equals("Gruppo aggiornato")){
                MyToast.makeText(getApplicationContext(),"Errore nell'aggiornamento del gruppo",false).show();
                return;
            }
            else MyToast.makeText(getApplicationContext(), "Gruppo aggiornato",true).show();
        }
    }

    private class eliminaComponenti extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_ELIMINA_COMPONENTI);
                for (int c=0; c<array_eliminati.length; c++) {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(3000);
                    urlConnection.setConnectTimeout(3000);
                    urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    String parametri = "codice_gruppo=" +URLEncoder.encode(gruppoSelezionato.getCodice_gruppo(), "UTF-8")+
                            "&nome_gruppo=" +URLEncoder.encode(gruppoSelezionato.getNome_gruppo(), "UTF-8")+
                            "&matricola_studente="+URLEncoder.encode(array_eliminati[c].getMatricola(), "UTF-8") +
                            "&nome_studente="+URLEncoder.encode(array_eliminati[c].getNome(), "UTF-8") +
                            "&cognome_studente="+URLEncoder.encode(array_eliminati[c].getCognome(), "UTF-8");
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
                    if(result==null || !result.equals("Studente eliminato")) return null;
                }
                return "ok";

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result==null) MyToast.makeText(getApplicationContext(),"Errore nell'aggiornamento del gruppo",false).show();
            else MyToast.makeText(getApplicationContext(), "Studenti eliminati",true).show();
        }
    }

    private class eliminaGruppoIntero extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_ELIMINA_GRUPPO);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                String parametri = "codice_gruppo=" +URLEncoder.encode(gruppoSelezionato.getCodice_gruppo(), "UTF-8");
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
            if(result==null || !result.equals("Gruppo eliminato")) MyToast.makeText(getApplicationContext(),"Impossibile eliminare il gruppo",false).show();
            else MyToast.makeText(getApplicationContext(),"Gruppo eliminato definitvamente",true).show();

        }
    }


//MENU CONTESTUALE
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Gruppo g;
        if(booleanAttivi==true) {
            g = (Gruppo) listaAttivi.getItemAtPosition(info.position);
        }
        else if(booleanInScadenza==true){
            g = (Gruppo) listaInScadenza.getItemAtPosition(info.position);
        }
        else{
            g=(Gruppo) listaScaduti.getItemAtPosition(info.position);
        }
        gruppoSelezionato=g;
        //if(a.getPosti_liberi()<0) return; //se  non c'è connessione non posso fare nulla

        menu.add(Menu.FIRST, 1, Menu.FIRST,"Elimina gruppo");

    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getItemId()==1){
            new eliminaGruppoIntero().execute();
            new prendiGruppi().execute();

        }
        return true;
    }

//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Aggiorna");
        menu.add(Menu.FIRST, 2, Menu.FIRST+1, "Home");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            new prendiGruppi().execute();
        }
        if (item.getItemId() == 2) {
            finish();
        }

        return true;
    }
}
