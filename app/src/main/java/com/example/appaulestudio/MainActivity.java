package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView txt_toRegistrazione;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_toRegistrazione=findViewById(R.id.log_toRegistrazione);
        String stringa="Oppure registrati";
        SpannableString ss=new SpannableString(stringa);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,RegistrazioneActivity.class);
                startActivity(i);
            }
        };

        ss.setSpan(clickableSpan1, 7, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt_toRegistrazione.setText(ss);
        txt_toRegistrazione.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
