package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Chunk;
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
    static final String URL_REGISTRAZIONE_CODICE ="http://pmsc9.altervista.org/progetto/registrazione_codice.php";
    private static final int STORAGE_CODE = 1000;
    private static final int READ_CODE = 2000;

    Random random = new Random();
    String[] numeriArray ={"0","1","2","3","4","5","6","7","8","9"};
    String[] caratteriArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "z", "y",
            "j", "k", "x", "w"};
    int lunghezzaCodice=10;
    String codice;
    String nome_docente, cognome_docente, universita, nomeUniversita, matricola_docente;
    String  gruppi, ore, partecipanti;
    EditText numeroPartecipanti, numeroOre, numeroGruppi, txtNomeGruppo;
    Spinner materieDocente;
    ArrayAdapter<Corso> adapter;
    Corso corso=null;
    Button creaCodici, btnDailogPdf, getBtnDailogPdfAnnulla;
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
    Dialog dialogPdf;

    ArrayList<Corso> corsoArrayList;
    //int current_year, current_month, current_day;

    private void initUI() {
        calendario= findViewById(R.id.calendario);
        txtNomeGruppo=findViewById(R.id.txtNomeGruppo);
        numeroGruppi = findViewById(R.id.numeroGruppi);
        numeroPartecipanti = findViewById(R.id.numeroPartecipanti);
        numeroOre = findViewById(R.id.numeroOre);
        materieDocente = findViewById(R.id.materieDocente);
        creaCodici= findViewById(R.id.btnCreaCodici);

        formatter= new SimpleDateFormat("yyyy-MM-dd");
        calendario.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                anno=year;
                mese=month+1;
                giorno=day;
                dataStringa=""+anno+"-"+mese+"-"+giorno;
                try{ date=formatter.parse(dataStringa); }
                catch(ParseException e) { return; }
            }
        });

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
                    MyToast.makeText(getApplicationContext(), "Per favore, seleziona una data", false).show();
                    return;
                }
                if(corso==null){
                    MyToast.makeText(getApplicationContext(), "Per favore, seleziona un corso", false).show();
                    return;
                }
                try{ gruppiIntero= Integer.parseInt(gruppi); }
                catch(NumberFormatException e){ return; }

                try{ partecipantiIntero= Integer.parseInt(partecipanti); }
                catch(NumberFormatException e){ return; }

                try{
                    double oreIntero1= Double.parseDouble(ore);
                    DecimalFormat twoDForm = new DecimalFormat("#.##");
                    oreIntero= Double.valueOf(twoDForm.format(oreIntero1));
                    ore=""+oreIntero;
                }
                catch(NumberFormatException e){ return; }
                //controllo che siano diversi da zero
                if(gruppiIntero<=0){
                    MyToast.makeText(getApplicationContext(), "Numero di gruppi errato!", false).show();
                    return;
                }
                if(partecipantiIntero<=0){
                    MyToast.makeText(getApplicationContext(), "Numero di partecipanti errato!", false).show();
                    return;
                }
                if(oreIntero<=0){
                    MyToast.makeText(getApplicationContext(), "Numero di ore errato!", false).show();
                    return;
                }

                int j=0;
                int k=0;
                codici=new String[gruppiIntero];
                for(j=0; j<gruppiIntero; j++){
                    String nuovoCodice=creaCodice();
                    codici[j]=  nuovoCodice;

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
            }
        };
        creaCodici.setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_codici);
        initUI();

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        nome_docente = settings.getString("nome", null);
        cognome_docente = settings.getString("cognome", null);
        matricola_docente = settings.getString("matricola", null);
        universita = settings.getString("universita", null);
        nomeUniversita= settings.getString("nome_universita", null);
        action_bar();

        Intent intent = getIntent();
        Bundle bundle=intent.getBundleExtra("bundle_corsi");
        corsoArrayList=bundle.getParcelableArrayList("corsi");

        adapter= new ArrayAdapter<Corso>(CreaCodici.this, android.R.layout.simple_spinner_item, corsoArrayList);
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

    @SuppressLint("WrongConstant")
    private void action_bar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar =view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText("Crea nuovi gruppi");
        final Dialog d = new Dialog(CreaCodici.this);
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_user);
        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        TextView txt_nome=d.findViewById(R.id.txt_dialog_user_nome);
        txt_nome.setText(nome_docente+" "+cognome_docente);
        TextView txt_matricola=d.findViewById(R.id.txt_dialog_user_matricola);
        txt_matricola.setText(matricola_docente);
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
                Intent i = new Intent(CreaCodici.this, MainActivity.class);
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
                    String parametri = "codice=" + URLEncoder.encode(codici[c], "UTF-8")+
                            "&matricolaDocente="+URLEncoder.encode(matricola_docente, "UTF-8")+
                            "&numeroOre="+URLEncoder.encode(ore, "UTF-8")+
                            "&numeroPartecipanti="+URLEncoder.encode(partecipanti,"UTF-8")+
                            "&codiceCorso="+URLEncoder.encode(corso.getCodiceCorso(),"UTF-8")+
                            "&nome_corso="+URLEncoder.encode(corso.getNomeCorso(),"UTF-8")+
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
                    if(result.equals("Codice gia presente")){
                        String nuovoCodice=creaCodice();
                        codici[c]=nuovoCodice;
                        c--;
                    }
                    else if(result.equals("ERROR: Could not connect.")) return null;
                }
                return "ok";
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            if(result.equals("ok")) dialogPdfCodici();
            else MyToast.makeText(getApplicationContext(),"Errore nella creazione dei codici",false).show();
        }
    }

    public void dialogPdfCodici(){
        dialogPdf= new Dialog(CreaCodici.this);
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
                    else savePdf();
                }
                else savePdf();
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
        String mFileName= new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        mFileName+="_"+corso.getCodiceCorso()+"_"+corso.getNomeCorso();
        //è deprecata
        String mFilePath = Environment.getExternalStorageDirectory()+"/"+mFileName+".pdf";
        try{

            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            //apri per scrivere
            mDoc.open();
            //mDoc.add(new Chunk(""));
            //openPdfFile(mDoc);
            //scegli il testo da inserire
            String mText="";
            mText+="Scadenza gruppi: "+dataStringa+"\n"+"Numero di partecipanti per ogni gruppo: "+partecipanti+
                    "\nOre assegnate a ciascun gruppo: "+ore+"\n";
            int i=1;
            for(String s:codici){
                mText+="Gruppo"+i+"-"+nomeGruppo+" codice: "+s+"\n";
                i++;
            }
            //String mText="cioa";
            //aggiungi paragrafo
            mDoc.add(new Paragraph(mText));
            // aggiungi autore opzionale e altre cose
            mDoc.addAuthor("App Aule Studio");

            //chiudi il doc
            mDoc.close();
            //mostra messaggio
            MyToast.makeText(this, mFileName+": operazione avvenuta con successo", true).show();

        }
        catch(Exception e){
            MyToast.makeText(this, "Impossibile salavare pdf", false).show();
        }
        dialogPdf.cancel();
        Intent i=new Intent(CreaCodici.this, HomeDocente.class);
        startActivity(i);
        finish();
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
                    MyToast.makeText(getApplicationContext(), "Hai negato il permesso quindi non posso procedere", false).show();
                }
            }
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