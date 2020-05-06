package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;




public class GroupActivity extends AppCompatActivity {

/*
new AlertDialog.Builder(this)
.setTitle("Title")
.setMessage("Do you really want to whatever?")
.setIcon(android.R.drawable.ic_dialog_alert)
.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int whichButton) {
        Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
    }})
 .setNegativeButton(android.R.string.no, null).show();


 */

    String nomeStudente;
    String matricolaStudente;
    String strUniversita, strMatricola, strPassword, strNome, strCognome;

    ListView corsiPerStudente;


    private void initUI(){
        corsiPerStudente = findViewById(R.id.listaGruppi);
        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strPassword=settings.getString("password", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        this.initUI();
    }
}
