package com.example.appaulestudio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class GestioneGruppiDocenteActivity extends AppCompatActivity {
    static final String URL_GRUPPI_DA_CORSO="http://pmsc9.altervista.org/progetto/gruppi_da_corso.php";
    static final String URL_COMPONENTI_DA_GRUPPO="http://pmsc9.altervista.org/progetto/componenti_gruppo.php";
    TextView output;
    //dialog gestione gruppo
    TextView nomeGruppoDialog; ListView listastudenti;
    TextView txtNomeComponente, txtCognomeComponente; CheckBox checkComponente;
    TextView oreDaAggiungere, nuovaScadenza;

    ArrayAdapter adapterComponenti;
    TextView nomeGruppo, dataScadenza, oreRimanenti, numeroPartecipanti;
    String codiceUniversita, nomeDocente, matricolaDocente, cognomeDocente, password;
    Intent intent;
    Bundle bundle;
    Corso corso;
    Gruppo[] gruppi;
    ListView listaGruppi;
    ArrayAdapter adapter;
    Dialog gestisciGruppoDialog;
    User[] componenti, array_copia, array_dinamico, array_partecipanti;
    Gruppo gruppoSelezionato;
    String studente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestione_gruppi_docente);
        intent=getIntent();
        bundle=intent.getBundleExtra("bundle");
        corso=bundle.getParcelable("corso");

        initUI();

    }

    private void initUI(){
        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        codiceUniversita=settings.getString("universita", null);
        matricolaDocente=settings.getString("matricola", null);
        password=settings.getString("password", null);
        nomeDocente=settings.getString("nome", null);
        cognomeDocente=settings.getString("cognome", null);
        setTitle(nomeDocente+" "+cognomeDocente);

        listaGruppi=findViewById(R.id.listaGruppi);
        output=findViewById(R.id.output);
        new prendiGruppi().execute();


            //mostraGruppi();


    }
    public void mostraGruppi(){
        adapter= new ArrayAdapter<Gruppo>(GestioneGruppiDocenteActivity.this,R.layout.row_layout_lista_gruppi_docente, gruppi){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                //return super.getView(position, convertView, parent);
                Gruppo item= getItem(position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_layout_lista_gruppi_docente
                        , parent, false);
                nomeGruppo=convertView.findViewById(R.id.nomeGruppo);
                dataScadenza=convertView.findViewById(R.id.dataScadenza);
                oreRimanenti=convertView.findViewById(R.id.oreRimanenti);
                nomeGruppo.setText(item.getNome_gruppo());
                dataScadenza.setText("Scadenza: "+ item.getData_scadenza());
                oreRimanenti.setText("Ore disponibili residue: "+item.getOre_disponibili());

                return convertView;
            }
        };
        listaGruppi.setAdapter(adapter);
        listaGruppi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gruppoSelezionato=gruppi[i];
                //output.setText(gruppoSelezionato.getNome_gruppo());
                //creaDialogGruppo();
                new prendiUtenti().execute();

            }
        });
    }

    public void creaDialogGruppo(){
        gestisciGruppoDialog = new Dialog(GestioneGruppiDocenteActivity.this);
        gestisciGruppoDialog.setTitle("Gestione Gruppo");
        gestisciGruppoDialog.setCancelable(true);
        gestisciGruppoDialog.setContentView(R.layout.dialog_gestione_gruppo);
        nomeGruppoDialog=gestisciGruppoDialog.findViewById(R.id.nomeGruppo);
        listastudenti=gestisciGruppoDialog.findViewById(R.id.listaStudenti);
        oreDaAggiungere=gestisciGruppoDialog.findViewById(R.id.oreDaAggiungere);
        nomeGruppoDialog.setText(gruppoSelezionato.getNome_gruppo());
        adapterComponenti= new ArrayAdapter<User>(GestioneGruppiDocenteActivity.this,
        R.layout.row_layout_componenti, componenti){
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
                        output.append(componenti.length+"COMPONENTI");
                        array_copia=array_dinamico;
                        studente = compoundButton.getText().toString();


                        //se lo toglie
                        if(b==false){
                            //tolgo lo studente

                            array_dinamico = rimuoviUser(array_copia, studente);
                            output.append(array_dinamico.length+"SELEZIONATI");
                            output.append(+array_dinamico.length+"/"+componenti.length);
                            if (array_dinamico.length == 0) {

                                Toast.makeText(getApplicationContext(), "Non ci sono piu componenti nel gruppo",
                                        Toast.LENGTH_LONG).show();
                            }
                            /*else {
                                //output.setText("");
                                for (User s : array_dinamico) {
                                    output.append(s.getMatricola());
                                }
                            }*/



                        }
                        //se lo inserisce
                        else{
                            array_dinamico=aggiungiUser(array_copia,studente);
                            output.append("\ninserisco "+array_dinamico.length+"SELE\n");
                            output.append(+array_dinamico.length+"/"+componenti.length);
                            for (User s : array_dinamico) {
                                output.append(s.getMatricola());
                            }
                        }
                    }
                });
                return convertView;

            }

        };
        listastudenti.setAdapter(adapterComponenti);



        gestisciGruppoDialog.show();
    }







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

                int n=array_gruppi.length;
                for (int i = 0; i < jArrayCorsi.length(); i++) {
                    JSONObject json_data = jArrayCorsi.getJSONObject(i);
                    array_gruppi[i] = new Gruppo(json_data.getString("codice_gruppo"),
                            json_data.getString("nome_gruppo"),
                            json_data.getString("codice_corso"),
                            json_data.getString("matricola_docente"),
                            json_data.getInt("componenti_max"),
                            json_data.getInt("ore_disponibili"),
                            json_data.getString("data_scadenza"));
                }

                return array_gruppi;


            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Gruppo[] array_gruppi) {

            if (array_gruppi.length==0) {
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Nessun gruppo disponibile</b></font>"), Toast.LENGTH_LONG).show();
                output.setText("");
                return;
            }
            gruppi=array_gruppi;
            //iniazializzo l'arrau degli effettivi partecipanti
            mostraGruppi();

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
                Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Gruppo senza iscritti</b></font>"), Toast.LENGTH_LONG).show();

                return;
            }
            componenti=array_componenti;
            array_dinamico=componenti;
            if(componenti!=null) {
                output.setText(componenti[0].getNome());
                creaDialogGruppo();
            }
            //iniazializzo l'arrau degli effettivi partecipanti

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

}
