package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView txt=findViewById(R.id.txtHome);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        String strUniversita=settings.getString("universita", null);
        String strNomeUniversita=settings.getString("nome_universita", null);
        String strMatricola=settings.getString("matricola", null);
        String strPassword=settings.getString("password", null);
        String strStudente=""+settings.getBoolean("studente", false);
        String strLogged=""+settings.getBoolean("logged", false);

        Toast.makeText(getApplicationContext(),""+strNomeUniversita+" "+strMatricola+" "+strPassword+" "+strStudente+" "+strLogged,Toast.LENGTH_LONG).show();

    }

    @Override //creazione menu in alto
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
        return true;
    }

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
