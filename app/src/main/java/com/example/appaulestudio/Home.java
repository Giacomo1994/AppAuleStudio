package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity {
    protected void initUI(){
        final FrameLayout fl= findViewById(R.id.fl);

        //doppio frame
        final LinearLayout frameLista = (LinearLayout)findViewById(R.id.frameLista);
        final LinearLayout frameMappa = (LinearLayout)findViewById(R.id.frameMappa);
        Button mappa= findViewById(R.id.mappa);
        Button lista = findViewById(R.id.lista);

        //passo da lista a mappa
        mappa.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                frameLista.setVisibility(fl.GONE);
                frameMappa.setVisibility(fl.VISIBLE);


            }

        });
        //passo da mappa a lista
        lista.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                frameMappa.setVisibility(fl.GONE);
                frameLista.setVisibility(fl.VISIBLE);
            }

        });
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initUI();
        //TextView txt=findViewById(R.id.txtHome);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        String strUniversita=settings.getString("universita", null);
        String strNomeUniversita=settings.getString("nome_universita", null);
        String strMatricola=settings.getString("matricola", null);
        String strPassword=settings.getString("password", null);
        String strStudente=""+settings.getBoolean("studente", false);
        String strLogged=""+settings.getBoolean("logged", false);

        Toast.makeText(getApplicationContext(),""+strNomeUniversita+" "+strMatricola+" "+strPassword+" "+strStudente+" "+strLogged,Toast.LENGTH_LONG).show();

    }



    /*@Override //creazione menu in alto
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
        return true;
    }*/

    @Override //se premo "Inserisci componente mi porta alla seconda activity"
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==1){
            SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("universita",null);
            editor.putString("nome_universita",null);
            editor.putString("matricola",null);
            editor.putString("password",null);
            editor.putBoolean("studente", true);
            editor.putBoolean("logged", false);
            editor.commit();
        }
        Intent i=new Intent(this,MainActivity.class);
        startActivityForResult(i,100);
        finish();
        return true;
    }
}
