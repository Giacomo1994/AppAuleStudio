package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        Double latitudine=Double.parseDouble(settings.getString("latitudine", null));
        Double longitudine=Double.parseDouble(settings.getString("longitudine", null));
        int ingresso=Integer.parseInt(settings.getString("ingresso", null));
        int pausa=Integer.parseInt(settings.getString("pausa", null));
        int slot=Integer.parseInt(settings.getString("slot", null));
        String first_slot=settings.getString("first_slot", null);
        MyToast.makeText(getApplicationContext(),""+latitudine+" "+longitudine+" "+ingresso+" "+pausa+" "+slot+" "+first_slot,true).show();
    }
}
