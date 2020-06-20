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
import android.graphics.Color;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import android.widget.LinearLayout;
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
    static final String URL_CREA_GRUPPI ="http://pmsc9.altervista.org/progetto/random_codice.php";
    private static final int STORAGE_CODE = 1000;

    View.OnClickListener listener;
    Dialog dialogPdf;
    ArrayList<Corso> corsoArrayList;
    Button creaCodici, btnDailogPdf, getBtnDailogPdfAnnulla;
    EditText numeroPartecipanti, numeroOre, numeroGruppi, txtNomeGruppo;
    Spinner materieDocente;
    ArrayAdapter<Corso> adapter;
    CalendarView calendario;
    ImageView img_info;

    Gruppo[] array_gruppi=null;
    String nome_docente, cognome_docente, universita, nomeUniversita, matricola_docente, gruppi, ore, partecipanti, nomeGruppo, dataStringa=null;
    Corso corso=null;
    int anno,mese,giorno;
    Date date=null;
    SimpleDateFormat formatter;

    private void initUI() {
        calendario= findViewById(R.id.calendario);
        txtNomeGruppo=findViewById(R.id.txtNomeGruppo);
        numeroGruppi = findViewById(R.id.numeroGruppi);
        numeroPartecipanti = findViewById(R.id.numeroPartecipanti);
        numeroOre = findViewById(R.id.numeroOre);
        materieDocente = findViewById(R.id.materieDocente);
        creaCodici= findViewById(R.id.btnCreaCodici);
        img_info=findViewById(R.id.img_why);

        //informazioni
        img_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(CreaCodici.this);
                d.setCancelable(true);
                d.setContentView(R.layout.dialog_warning);
                d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                ImageView img=d.findViewById(R.id.img_dialog_warning);
                LinearLayout ll_bottoni=d.findViewById(R.id.ll_dialog_warning_bottoni);
                TextView txt= d.findViewById(R.id.txt_dialog_warning);

                img.setVisibility(View.GONE);
                ll_bottoni.setVisibility(View.GONE);
                String s="Se viene compilata la casella, il nome dei gruppi creati sarà 'Gruppo#-testoInserito' con # numero pregressivo del gruppo. Se non viene compilata, il suffisso viene sostituito dal nome della materia";
                SpannableString ss=new SpannableString(s);
                ss.setSpan(new ForegroundColorSpan(Color.argb(255,47, 163, 78)),62,85, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                txt.setText(ss, TextView.BufferType.SPANNABLE);
                d.show();
            }
        });

        //calendario
        formatter= new SimpleDateFormat("yyyy-MM-dd");
        calendario.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                anno=year;
                mese=month+1;
                giorno=day;
                dataStringa=""+anno+"-"+mese+"-"+giorno;
                try{
                    date=formatter.parse(dataStringa);
                    if(date.before(Calendar.getInstance().getTime())){
                        MyToast.makeText(getApplicationContext(), "Data errata! Seleziona un'altra data.", false).show();
                        dataStringa=null;
                    }
                }
                catch(ParseException e) { return; }
            }
        });

        //pulsante
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

                // ho selezionato data e corso?
                if(dataStringa==null){
                    MyToast.makeText(getApplicationContext(), "Per favore, seleziona una data", false).show();
                    return;
                }
                else if(corso==null){
                    MyToast.makeText(getApplicationContext(), "Per favore, seleziona un corso", false).show();
                    return;
                }

                //il formato dei dati è corretto?
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

                //i dati sono giusti?
                if(gruppiIntero<=0){
                    MyToast.makeText(getApplicationContext(), "Numero di gruppi errato!", false).show();
                    return;
                }
                else if(partecipantiIntero<=0){
                    MyToast.makeText(getApplicationContext(), "Numero di partecipanti errato!", false).show();
                    return;
                }
                else if(oreIntero<=0){
                    MyToast.makeText(getApplicationContext(), "Numero di ore errato!", false).show();
                    return;
                }
                else new creaGruppi().execute();
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

        adapter= new ArrayAdapter<Corso>(CreaCodici.this, R.layout.simple_custom_list_item, corsoArrayList);
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
        d.setCancelable(true);
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

//TASK ASINCRONO
    private class creaGruppi extends AsyncTask<Void, Void, Gruppo[]> {
        @Override
        protected Gruppo[] doInBackground(Void... voids) {
            try {
                String params;
                URL url;
                HttpURLConnection urlConnection; //serve per aprire connessione
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArrayGruppi;
                url = new URL(URL_CREA_GRUPPI); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                params = "matricolaDocente="+URLEncoder.encode(matricola_docente, "UTF-8")+
                        "&numeroGruppi="+URLEncoder.encode(gruppi, "UTF-8")+
                        "&numeroOre="+URLEncoder.encode(ore, "UTF-8")+
                        "&numeroPartecipanti="+URLEncoder.encode(partecipanti,"UTF-8")+
                        "&codiceCorso="+URLEncoder.encode(corso.getCodiceCorso(),"UTF-8")+
                        "&nome_corso="+URLEncoder.encode(corso.getNomeCorso(),"UTF-8")+
                        "&dataScadenza="+URLEncoder.encode(dataStringa,"UTF-8")+
                        "&codiceUniversita="+URLEncoder.encode(universita,"UTF-8")+
                        "&nomeGruppo="+URLEncoder.encode(nomeGruppo, "UTF-8");
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(params);
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
                jArrayGruppi = new JSONArray(result);
                Gruppo[] array_gruppo = new Gruppo[jArrayGruppi.length()];
                for(int i = 0; i<jArrayGruppi.length(); i++){
                    JSONObject json_data = jArrayGruppi.getJSONObject(i);
                    array_gruppo[i] = new Gruppo(json_data.getString("codice"), json_data.getString("nome"),
                            corso.getCodiceCorso(), matricola_docente, Integer.parseInt(partecipanti), Double.parseDouble(ore), dataStringa);
                }
                return array_gruppo;
            } catch (Exception e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(Gruppo[] array_gruppo) {
            if(array_gruppo==null) MyToast.makeText(getApplicationContext(),"Errore nella creazione dei codici",false).show();
            else{
                array_gruppi=array_gruppo;
                dialogPdfCodici();
            }
        }
    }

// PDF
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) savePdf();
                else MyToast.makeText(getApplicationContext(), "Impossibile salvare il file: hai negato il permesso!", false).show();
            }
        }
    }

    public void dialogPdfCodici(){
        dialogPdf= new Dialog(CreaCodici.this);
        dialogPdf.setTitle("Crea Pdf");
        dialogPdf.setCancelable(true);
        dialogPdf.setContentView(R.layout.dialog_crea_pdf);
        dialogPdf.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        getBtnDailogPdfAnnulla=dialogPdf.findViewById(R.id.btnDialogPdfAnnulla);
        btnDailogPdf=dialogPdf.findViewById(R.id.btnDialogPdf);
        dialogPdf.show();
        btnDailogPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
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
        String mFilePath = Environment.getExternalStorageDirectory()+"/"+mFileName+".pdf";
        try{
            //aggiungo testo
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            mDoc.open();
            String mText=""+corso.getNomeCorso()+"\n";
            mText+="Scadenza gruppi: "+dataStringa+"\n"+"Numero di partecipanti per ogni gruppo: "+partecipanti+
                    "\nOre assegnate a ciascun gruppo: "+ore+"\n";
            for(Gruppo g: array_gruppi){
                mText+=g.getNome_gruppo()+", "+g.getCodice_gruppo()+"\n";
            }
            //salvo pdf
            mDoc.add(new Paragraph(mText));
            mDoc.addAuthor("StudyAround");
            mDoc.close();
            MyToast.makeText(this, mFileName+": operazione avvenuta con successo", true).show();

        }
        catch(Exception e){
            MyToast.makeText(this, "Impossibile salavare pdf", false).show();
        }
        dialogPdf.cancel();
        finish();
    }

//OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Home");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            finish();
        }

        return true;
    }
}