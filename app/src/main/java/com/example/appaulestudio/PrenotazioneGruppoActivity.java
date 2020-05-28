package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.sip.SipSession;
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
import android.widget.DatePicker;
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
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/*new User(json_data.getString("matricola"),
        json_data.getString("nome"),
        json_data.getString("cognome"),
        json_data.getString("codice_universita"),
        json_data.getString("mail"),
        json_data.getString("password"),
        json_data.getBoolean("studente"));*/
public class PrenotazioneGruppoActivity extends AppCompatActivity {
    LocalTime fascia830, fascia10, fascia1130,fascia13, fascia1430,fascia16,fascia1730,fascia19;
    LocalTime primaFascia, ultimaFascia,fasciaQuery;
    LocalTime fascia8, inizioGiorno;
    public DatePickerDialog.OnDateSetListener listener;
    String[] dateDisponibili;
    TextView txtDataMostrata;
    int anno,mese,giorno;
    int giornoSelezionatoInt;
    Button btnIscriviti, btnHome;
    Button btnCercaDisponibilita;
    String studente;
    ArrayAdapter adapterComponenti;
    String risultato,risultato2; int n;
    Button btnGruppo, btnComponenti, btncheckComponenti;
    ListView listacomponenti;
    TextView gruppoSelezionato;
    TextView txtNomeComponente, txtCognomeComponente, txtUniversitaComponente;
    //TextView txtComponenti;
    CheckBox checkComponente;
    Dialog d,dialogCheckComponenti;
    TextView nomeAula, output, componenti, txtStatoAula;
    Button btnIndietro,btnAvanti;
    TextView titoloDialogErrore;
    TextView txtOreResidue;
    SubsamplingScaleImageView piantaAula;
    ArrayList<Orario_Ufficiale> orariUfficiali;
    String nomeStudente, matricolaStudente,codiceUniversita, cognomeStudente;
    Aula aula;
    Intent intent;
    ArrayList<Tavolo> tavoli;
    static final String URL_GRUPPI_DA_MATRICOLA="http://pmsc9.altervista.org/progetto/gruppi_da_matricola.php";
    static final String URL_COMPONENTI_DA_GRUPPO="http://pmsc9.altervista.org/progetto/componenti_gruppo.php";
    static final String URL_PRENOTAZIONI_FUTURE="http://pmsc9.altervista.org/progetto/prenotazioni_gruppi_future.php";
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
    @RequiresApi(api = Build.VERSION_CODES.O)
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
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initUI(){
        array_gruppo=null;
        tavoli=new ArrayList<Tavolo>();
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
        fascia830=LocalTime.parse("08:30:00");
        fascia10=LocalTime.parse("10:00:00");fascia1130=LocalTime.parse("11:30:00");
        fascia13=LocalTime.parse("13:00:00");fascia1430=LocalTime.parse("14:30:00");
        fascia16=LocalTime.parse("16:00:00");fascia1730=LocalTime.parse("17:30:00");
        fascia19=LocalTime.parse("19:00:00");
        //fascia8 = LocalTime.of(5, 30);

        intent =getIntent();
        Bundle bundle=intent.getBundleExtra("dati");
        aula=bundle.getParcelable("aula");
        nomeAula.setText(aula.getNome());
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        nomeStudente=settings.getString("nome", null);
        cognomeStudente=settings.getString("cognome",null);
        matricolaStudente=settings.getString("matricola", null);
        codiceUniversita=settings.getString("universita", null);
        setTitle(nomeStudente+" "+cognomeStudente);
        orariUfficiali=bundle.getParcelableArrayList("orari");
        Collections.sort(orariUfficiali);

        new load_image().execute();
        new prendiGruppi().execute();
        btnComponenti=findViewById(R.id.btnComponenti);
        txtOreResidue=findViewById(R.id.txtOreResidue);
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

        /*final DatePickerDialog  StartTime;
        View.OnClickListener lister=new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
        final Calendar newCalendar = Calendar.getInstance();
        StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                output.setText(newDate.toString());
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        btnScegliData.setOnClickListener(new View.OnClickListener() {
            @Override   public void onClick(View v) {
                StartTime.show();
                }
            });*/


        /*implements DatePickerDialog.OnDateSetListener {

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getApplicationContext(), MainActivity.this, anno, mese, giorno);
        }

        btnScegliData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dataPicker=new DatePickerDialog(getApplicationContext(),
                        PrenotazioneGruppoActivity.this,anno, mese,giorno);
                dataPicker.show();
            }
        });*/

        giornoSelezionatoInt=0;
        txtDataMostrata=findViewById(R.id.txtDataMostrata);
        txtStatoAula=findViewById(R.id.txtStatoAula);
        btnAvanti=findViewById(R.id.btnAvanti);
        btnIndietro=findViewById(R.id.btnIndietro);
        Calendar c = Calendar.getInstance();
        anno=c.get(Calendar.YEAR);
        //il mese parte da zero
        mese=c.get(Calendar.MONTH)+1;
        giorno=c.get(Calendar.DAY_OF_MONTH);
        //output.setText(""+anno+""+mese+""+giorno);
        //txtDataMostrata.setText("Oggi");
        if(giornoSelezionatoInt==0){
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
            }
        });
        btnIndietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAvanti.setVisibility(View.VISIBLE);
                giornoSelezionatoInt--;
                mostraData(giornoSelezionatoInt);
            }
        });


        btnCercaDisponibilita=findViewById(R.id.btnCercaDisponibilita);
        btnCercaDisponibilita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(giornoSelezionatoInt>0) {
                    fasceOrarie(giornoSelezionatoInt);
                    //while(!fasciaQuery.isAfter(ultimaFascia)) {
                        new prendiPrenotazioni().execute();
                     //   fasciaQuery.plusMinutes(90);
                   // }
                }
                output.setText(primaFascia+" "+ultimaFascia+" "+fascia830.toString());
                //ora ho gruppo, componenti, data e fascie orarie
                //devo fare un task asincrono che conta i posti liberi e li confronta con quelli richiesti
                //per ogni fascia oraria

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fasceOrarie(int indice){
        LocalTime oraApertura, oraChiusura;
        oraApertura=LocalTime.parse(orariUfficiali.get(indice).getApertura());
        oraChiusura=LocalTime.parse(orariUfficiali.get(indice).getChiusura());
        //primaFascia indica orario di inizio delle fascie, ultimaFascia induca l orari odi fine dell ultimafascia
        //ci sono 7 fasce orarie
        if(!oraApertura.isAfter(fascia830)){
            primaFascia=fascia830;
        }
        else if(!oraApertura.isAfter(fascia10)){
            primaFascia=fascia10;
        }
        else if(!oraApertura.isAfter(fascia1130)){
            primaFascia=fascia1130;
        }
        else if(!oraApertura.isAfter(fascia13)){
            primaFascia=fascia13;
        }
        else if(!oraApertura.isAfter(fascia1430)){
            primaFascia=fascia1430;
        }
        else if(!oraApertura.isAfter(fascia16)){
            primaFascia=fascia16;
        }
        else if(!oraApertura.isAfter(fascia1730)){
            primaFascia=fascia1730;
        }

        if(!oraChiusura.isBefore(fascia19)) {
            ultimaFascia=fascia19;
        }
        else if(!oraChiusura.isBefore(fascia1730)){
            ultimaFascia=fascia1730;
        }
        else if(!oraChiusura.isBefore(fascia16)){
            ultimaFascia=fascia16;
        }
        else if(!oraChiusura.isBefore(fascia1430)){
            ultimaFascia=fascia1430;
        }
        else if(!oraChiusura.isBefore(fascia13)){
            ultimaFascia=fascia13;
        }
        else if(!oraChiusura.isBefore(fascia1130)){
            ultimaFascia=fascia1130;
        }
        else if(!oraChiusura.isBefore(fascia10)){
            ultimaFascia=fascia10;
        }
        fasciaQuery=primaFascia;
    }
    public void mostraData(int indiceGiorno){
        //ho array di date e orari
        orariUfficiali.get(indiceGiorno).getData();
        txtDataMostrata.setText(orariUfficiali.get(indiceGiorno).getData());
        if(orariUfficiali.get(indiceGiorno).getApertura()==null){
            txtStatoAula.setText("L'aula è chiusa");
        }
        else{
            txtStatoAula.setText("L'aula apre alle "+orariUfficiali.get(indiceGiorno).getApertura()+
                    " e chiude alle "+orariUfficiali.get(indiceGiorno).getChiusura());
        }
        if(indiceGiorno==0){
            btnIndietro.setVisibility(View.GONE);
        }
        if(indiceGiorno==6){
            btnAvanti.setVisibility(View.GONE);
        }
    }



    /*public void onDateSet(DatePicker view, int y, int m, int d){
        giorno=d;
        mese=m;
        anno=y;
        if(android.os.Build.VERSION.SDK_INT>= android.os.Build.VERSION_CODES.O){
            LocalDate date=LocalDate.of(anno,mese,giorno);
            DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.ITALY);
            String s=date.format((formatter));
            output.append(s);
        }
    }*/

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
                txtOreResidue.setText("Ore residue: "+gruppo.getOre_disponibili()+"");
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


    /*int year=0,month=0,day=0,hour=0,minute=0;
                if(android.os.Build.VERSION.SDK_INT>= android.os.Build.VERSION_CODES.O){
        LocalDateTime dateTime=LocalDateTime.of(year,month,day,hour,minute);
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String s=dateTime.format((formatter));
        output.append(s);

    }*/



    private class prendiPrenotazioni extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_PRENOTAZIONI_FUTURE);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "id_aula="+URLEncoder.encode(aula.getIdAula(), "UTF-8")+
                        "&data_orario="+URLEncoder.encode(orariUfficiali.get(giornoSelezionatoInt).getData()+
                        " "+fasciaQuery.toString()+":00", "UTF-8");

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



                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    int posti_liberi=json_data.getInt("posti_totali")-json_data.getInt("posti_occupati");
                    Tavolo t= new Tavolo(aula.getIdAula(),json_data.getInt("tavolo"),json_data.getInt("posti_totali"),
                            posti_liberi);
                    t.setFasciaOraria(fasciaQuery.toString());
                    //if(t.getPosti_liberi()>=array_dinamico.length) tavoli.add(t);
                    tavoli.add(t);
                }
                return result;

            } catch (Exception e) {
                MyToast.makeText(getApplicationContext(), "Errore nel caricamento", MyToast.LENGTH_LONG).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(tavoli!=null){
                output.append(tavoli.toString());
            }
            output.append("\n"+risultato2+"\n"+orariUfficiali.get(giornoSelezionatoInt).getData()+" "+fasciaQuery.toString()+":00");

        }

    }
}









