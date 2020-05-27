package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.FormatException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
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
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

public class CreaCodici extends AppCompatActivity {
    private static final int STORAGE_CODE = 1000;
    private static final int READ_CODE = 2000;
    Random random = new Random();
    String[] numeriArray ={"0","1","2","3","4","5","6","7","8","9"};
    String[] caratteriArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "z", "y",
            "j", "k", "x", "w"};
    int lunghezzaCodice=10;
    String codice;
    TextView textnome;
    String nome_docente, cognome_docente, universita, matricola_docente;
    String materia, gruppi, ore, partecipanti;
    EditText numeroPartecipanti, numeroOre, numeroGruppi, txtNomeGruppo;
    Spinner materieDocente;
    ArrayAdapter<Corso> adapter;
    Corso corso=null;
    Button creaCodici, salvaPdf, btnDailogPdf, getBtnDailogPdfAnnulla;
    CalendarView calendario;
    int anno;
    int mese;
    int giorno;
    String nomeGruppo;
    //int year, month, day;
    String dataStringa=null; //formato dd/mm/YYYY bisogna risolvere inizializzazione
    String[] codici;
    Date date=null;
    SimpleDateFormat formatter;
    View.OnClickListener listener;
    //int current_year, current_month, current_day;
    static final String URL_CORSI= "http://pmsc9.altervista.org/progetto/richiedi_corsi_from_docente.php";
    //static final String URL_CONTROLLO_CODICI ="http://pmsc9.altervista.org/progetto/controllo_codici_gruppi.php";
    static final String URL_REGISTRAZIONE_CODICE ="http://pmsc9.altervista.org/progetto/registrazione_codice.php";
    private void initUI() {
        /*salvaPdf= findViewById(R.id.salvaPDF);
        salvaPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //permission ho gia messo nel manifest la dichiarazione
                if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                            PackageManager.PERMISSION_DENIED){
                        //il permesso va richiesto
                        String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, STORAGE_CODE);
                    }
                    else{//permesso già accordato salviamo il pdf
                        savePdf();

                    }

                }
                else{//sistema non richiedere runtime permission
                    savePdf();

                }


            }
        });*/
        calendario= findViewById(R.id.calendario);
        textnome = findViewById(R.id.textnome);
        textnome.setText("");
        //btnScadenza= findViewById(R.id.btnScadenza);
       /* btnScadenza.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DatePickerDialog datePicker=new DatePickerDialog(CreaCodici.this, (DatePickerDialog.OnDateSetListener) CreaCodici.this,
                        current_year, current_month, current_day);
                datePicker.show();
            }
        });*/


        formatter= new SimpleDateFormat("yyyy-MM-dd");
        calendario.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                //textnome.setText("");
                anno=year;
                mese=month+1;
                giorno=day;
                //textnome.append(anno+" "+mese+" "+giorno);
                dataStringa=""+anno+"-"+mese+"-"+giorno;
                try{
                    date=formatter.parse(dataStringa);
                    //textnome.append(anno+" "+mese+" "+giorno);
                }
                catch(ParseException e) {
                    return;
                }
            }
        });


        setTitle(nome_docente + " " + cognome_docente);
        txtNomeGruppo=findViewById(R.id.txtNomeGruppo);
        numeroGruppi = findViewById(R.id.numeroGruppi);
        numeroPartecipanti = findViewById(R.id.numeroPartecipanti);
        numeroOre = findViewById(R.id.numeroOre);
        materieDocente = findViewById(R.id.materieDocente);
        creaCodici= findViewById(R.id.btnCreaCodici);
        listener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int gruppiIntero=0;
                int partecipantiIntero=0;
                double oreIntero=0;

                nomeGruppo=txtNomeGruppo.getText().toString().trim();
                gruppi=numeroGruppi.getText().toString().trim();
                partecipanti=numeroPartecipanti.getText().toString().trim();
                ore=numeroOre.getText().toString().trim();
                if(dataStringa==null){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font>Per favore seleziona una data</b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                if(corso==null){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font>Per favore seleziona un corso</b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }

                try{
                    gruppiIntero= Integer.parseInt(gruppi);

                }
                catch(NumberFormatException e){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<<b><font><p>Numero di gruppi errato!</p><p>Per favore inserisci un numero intero<p></b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    partecipantiIntero= Integer.parseInt(partecipanti);
                }
                catch(NumberFormatException e){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font><p>Numero di partecipanti errato!</p><p>Per favore inserisci un numero intero</p></b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    double oreIntero1= Double.parseDouble(ore);
                    //oreIntero= Math.floor(oreIntero1 * 100.0) / 100.0;
                    DecimalFormat twoDForm = new DecimalFormat("#.##");
                    oreIntero= Double.valueOf(twoDForm.format(oreIntero1));
                    ore=""+oreIntero;

                }
                catch(NumberFormatException e){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font><p>Numero di ore errato!</p><p>Per favore inserisci un numero intero</p></b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                //controllo che siano diversi da zero
                if(gruppiIntero==0){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font><p>Numero di gruppi errato!</p><p>Per favore inserisci un numero maggiore di zero</p></b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                if(partecipantiIntero==0){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font><p>Numero di partecipanti errato!</p><p>Per favore inserisci un numero maggiore di zero</p></b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                if(oreIntero==0){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font><p>Numero di ore errato!</p><p>Per favore inserisci un numero maggiore di zero</p></b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                //si può togliere
                if(nomeGruppo.length()>=30){
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<b><font><p>Nome eccessivamente lungo!</p><p>Per favore inserisci un numero maggiore di zero</p></b></font>"), Toast.LENGTH_LONG).show();
                    return;
                }
                //se arrivo qui ho corso, numero di partecipanti ore e gruppi diversi da zero
                //devo chiamare la funzione che genera i codici
                int j=0;
                int k=0;



                codici=new String[gruppiIntero];
                for(j=0; j<gruppiIntero; j++){
                    String nuovoCodice=creaCodice();
                    codici[j]=  nuovoCodice;
                    //textnome.append("codice "+j+" "+codici[j]+ " ");

                    if(j>0) { //controllo che non ci siano duecodici uguali
                        for (k = 0; k < j; k++) {
                            if (codici[j].compareTo(codici[k]) == 0) {
                                codici[j] = null;
                                j--;
                            }
                        }
                    }
                }
                new iscriviCodici().execute();
                dialogPdfCodici();
            }
        };

        creaCodici.setOnClickListener(listener);

    }

    public void dialogPdfCodici(){
        final Dialog dialogPdf= new Dialog(CreaCodici.this);
        dialogPdf.setTitle("Crea Pdf");
        dialogPdf.setCancelable(true);
        dialogPdf.setContentView(R.layout.dialog_crea_pdf);
        getBtnDailogPdfAnnulla=dialogPdf.findViewById(R.id.btnDialogPdfAnnulla);
        btnDailogPdf=dialogPdf.findViewById(R.id.btnDialogPdf);
        dialogPdf.show();
        btnDailogPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                            PackageManager.PERMISSION_DENIED){
                        //il permesso va richiesto
                        String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, STORAGE_CODE);
                    }
                    else{//permesso già accordato salviamo il pdf
                        savePdf();

                    }

                }
                else{//sistema non richiedere runtime permission
                    savePdf();

                }
                dialogPdf.cancel();
                dialogPdf.cancel();
                Intent i=new Intent(CreaCodici.this, HomeDocente.class);
                startActivity(i);
                finish();

            }
        });
        getBtnDailogPdfAnnulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPdf.cancel();
                Intent i=new Intent(CreaCodici.this, HomeDocente.class);
                startActivity(i);
                finish();
            }
        });



    }
    private void savePdf() {
        //creo oggetto di classe pdf
        Document mDoc= new Document();
        //nome del file
        String mFileName= new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()).format(System.currentTimeMillis());
        //è deprecata
        String mFilePath = Environment.getExternalStorageDirectory()+"/"+mFileName+".pdf";
        try{

            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            //apri per scrivere
            mDoc.open();
            //openPdfFile(mDoc);
            //scegli il testo da inserire
            String mText="";
            mText+="Scadenza gruppi: "+dataStringa+"\n"+"Numero di partecipanti per ogni gruppo: "+partecipanti+
                    "\nOre assegnate a ciascun gruppo: "+ore+"\n";
            //mText="cioa";
            int i=1;
            for(String s:codici){
                mText+="Gruppo"+i+"-"+nomeGruppo+" codice: "+s+"\n";
                i++;
            }
            // aggiungi autore opzionale e altre cose
            mDoc.addAuthor("App Aule Studio");
            //aggiungi paragrafo
            mDoc.add(new Paragraph(mText));
            //chiudi il doc
            mDoc.close();
            //mostra messaggio
            Toast.makeText(this, mFileName+" operazione avvenuta con successo "+ mFilePath, Toast.LENGTH_SHORT).show();

        }
        catch(Exception e){
            //we qualcosa va storto
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case STORAGE_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    savePdf();
                }
                else{
                    //permesso non accordato dalla finestra di pop un e mostro errore
                    Toast.makeText(this, "Hai negato il permesso quindi non posso procedere", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    /*public void onDateSet(DatePicker view, int y, int m, int d){
        year=y; month=m; day=d;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LocalDate date= LocalDate.of(year,month,day);
            DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.ITALY);
            String s=date.format(formatter);

        }
    }*/
    //passiamo i parametri all tabella
    //public void iscriviCodici(String[] codici, Corso c, int numeroPartecipanti,int numeroOre, String dataScadenza){

    //}

    //creo menu in alto
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
            editor.putString("password", null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.putString("last_update", null);
            editor.commit();
            Intent i = new Intent(CreaCodici.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, 100);
            finish();
        }
        if (item.getItemId() == 2) {
            Intent i = new Intent(CreaCodici.this, HomeDocente.class);
            startActivityForResult(i, 150);
            finish();
        }
        return true;
    }

    private void openPdfFile(File pdfFile) {

        if(Build.VERSION.SDK_INT>=24){

            try{

                //For API's > 24, runtime exception occurs when a URI is exposed BEYOND this particular app that you are writing (AKA when user attempts to open in device/emulator

                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");

                m.invoke(null);

            }catch(Exception e){

                e.printStackTrace();

            }

        }

        Intent target = new Intent(Intent.ACTION_VIEW);

        target.setDataAndType(Uri.fromFile(pdfFile),"application/pdf");

        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);



        Intent intent = Intent.createChooser(target, "Open File");

        try {

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            // Instruct the user to install a PDF reader here, or something

        }

    }
    private class iscriviCodici extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(URL_REGISTRAZIONE_CODICE);
                if(nomeGruppo.equals("")){
                    nomeGruppo=corso.getNomeCorso();
                }

                for (int c=0; c<codici.length; c++) {
                    int g=c+1;

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(3000);
                    urlConnection.setConnectTimeout(3000);
                    urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    String parametri = "codice=" + URLEncoder.encode(codici[c], "UTF-8")+ "&matricolaDocente="+URLEncoder.encode(matricola_docente, "UTF-8")+
                    "&numeroOre="+URLEncoder.encode(ore, "UTF-8")+
                "&numeroPartecipanti="+URLEncoder.encode(partecipanti,"UTF-8")+
                "&codiceCorso="+URLEncoder.encode(corso.getCodiceCorso(),"UTF-8")+
                "&dataScadenza="+URLEncoder.encode(dataStringa,"UTF-8")+
                            "&codiceUniversita="+URLEncoder.encode(universita,"UTF-8")+
                            "&nomeGruppo="+URLEncoder.encode("Gruppo"+g+"-"+nomeGruppo, "UTF-8");

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
                    if(result.compareTo("Codice gia presente")==0){
                        String nuovoCodice=creaCodice();
                        codici[c]=nuovoCodice;
                        c--;
                    }

                }
                return null;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }



    //algoritmo per calcolare i codici
    public String creaCodice(){
        codice="";

        for(int i=0; i<lunghezzaCodice; i++){
            boolean scelgoArray = random.nextBoolean();
            if(scelgoArray==true){
                //scelgo casualmente tra l'array dei numeri
                int numeroRandom = random.nextInt(numeriArray.length-1);
                codice += numeriArray[numeroRandom];

            }
            else{
                //scelgo casualmente tra l'array delle lettere
                int letteraRandom =random.nextInt(caratteriArray.length-1);
                codice += caratteriArray[letteraRandom];
            }
        }
        return codice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_codici);
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        nome_docente = settings.getString("nome", null);
        cognome_docente = settings.getString("cognome", null);
        matricola_docente = settings.getString("matricola", null);
        universita = settings.getString("universita", null);
        if (nome_docente.compareTo("") == 0 || cognome_docente.compareTo("") == 0 ||
                matricola_docente.compareTo("") == 0 || universita.compareTo("") == 0) {
            //fai qualcosa non so cosa
        }
        initUI();
        new riempiCorsi().execute();

    }

    //riempire lo spinner delle materie del docente loggato
    private class riempiCorsi extends AsyncTask<Void, Void, Corso[]> {

        @Override
        protected Corso[] doInBackground(Void... voids) {
            try{
                URL url = new URL(URL_CORSI);
                //URL url = new URL("http://10.0.2.2/progetto/listaUniversita.php");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                String parametri = "matricola_docente="+URLEncoder.encode(matricola_docente, "UTF-8")
                        +"&codice_universita="+URLEncoder.encode(universita, "UTF-8");

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

                Corso[] array_corso = new Corso[jArrayCorsi.length()];

                for(int i = 0; i<jArrayCorsi.length(); i++){
                    JSONObject json_data = jArrayCorsi.getJSONObject(i);
                    array_corso[i] = new Corso(json_data.getString("codice_corso"),
                            json_data.getString("nome_corso"),
                            universita,
                            matricola_docente);
                }

                return array_corso;


            }
            catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Corso[] array_corso) {
            super.onPostExecute(array_corso);
            if(array_corso==null){
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Nessun corso disponibile</b></font>"), Toast.LENGTH_LONG).show();
                materieDocente.setEnabled(false);

                return;
            }
            String s="";
            for(Corso c: array_corso){
                s +=" "+ c.getCodiceCorso()+" "+c.getNomeCorso();
            }
            //textnome.setText(s);


            /*adapter=new ArrayAdapter<String>(CreaCodici.this, R.layout.spinner_materie);
            adapter.add(array_corso[0].getCodiceCorso());
            materieDocente.setAdapter(adapter);

            materieDocente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View arg1,
                                           int position, long arg3) {
                    corso = (Corso) parent.getItemAtPosition(position);
                }*/







            adapter= new ArrayAdapter<Corso>(CreaCodici.this, android.R.layout.simple_spinner_item, array_corso);

            materieDocente.setAdapter(adapter);

            materieDocente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    corso = (Corso) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }








}