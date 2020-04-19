package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class InfoAula extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_aula);

        Intent intent=getIntent();
        Bundle bundle=intent.getBundleExtra("bundle_aula");
        Aula aula=bundle.getParcelable("aula");
        Toast.makeText(getApplicationContext(), aula.nome, Toast.LENGTH_SHORT).show();
    }
}
