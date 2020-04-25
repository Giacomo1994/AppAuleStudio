package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class InfoAulaActivity extends AppCompatActivity {
    TextView infoAula_id, txt;
    Intent intent;
    Bundle bundle;
    Aula aula;
    SQLiteDatabase db;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_aula);
        infoAula_id=findViewById(R.id.infoAula_nome);
        txt=findViewById(R.id.textView12);

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>"+isConnected+"</b></font>"), Toast.LENGTH_LONG).show();


        db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM info_aule_offline";
        cursor = db.rawQuery(sql, null);
        ArrayList<Aula> aule=new ArrayList<Aula>();
        if(cursor==null ||cursor.getCount()==0){
            Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile mostrare aule: connettiti ad internet per visualizzarle</b></font>"), Toast.LENGTH_LONG).show();
            return;
        }
        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            String id=cursor.getString(cursor.getColumnIndex("id"));
            String nome=cursor.getString(cursor.getColumnIndex("nome"));
            String luogo=cursor.getString(cursor.getColumnIndex("luogo"));
            double latitudine=cursor.getDouble(cursor.getColumnIndex("latitudine"));
            double longitudine=cursor.getDouble(cursor.getColumnIndex("longitudine"));
            int flag_gruppi=cursor.getInt(cursor.getColumnIndex("flag_gruppi"));
            int posti_totali=cursor.getInt(cursor.getColumnIndex("posti_totali"));
            int posti_liberi=-1;
            //int posti_liberi=cursor.getInt(cursor.getColumnIndex("posti_liberi"));
            String servizi=cursor.getString(cursor.getColumnIndex("servizi"));
            Aula a=new Aula(id,nome,luogo,latitudine,longitudine,flag_gruppi,posti_totali,posti_liberi,servizi);
            aule.add(a);
        }
        sql = "SELECT * FROM orari_offline";
        cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0){
            Toast.makeText(getApplicationContext(), Html.fromHtml("<font color='#eb4034' ><b>Impossibile mostrare aule: connettiti ad internet per visualizzarle</b></font>"), Toast.LENGTH_LONG).show();
            return;
        }
        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            String id=cursor.getString(cursor.getColumnIndex("id_aula"));
            int giorno=cursor.getInt(cursor.getColumnIndex("giorno"));
            String apertura=cursor.getString(cursor.getColumnIndex("apertura"));
            String chiusura=cursor.getString(cursor.getColumnIndex("chiusura"));
            for(Aula a: aule){
                if(a.idAula.equals(id)){
                    a.orari.put(giorno,new Orario(apertura,chiusura));
                }
            }
        }
        txt.setText("");
        for(Aula a:aule){
           txt.append(a.idAula+"\n");
           for(int i=1;i<=7;i++){
               txt.append(""+i+" "+a.orari.get(i).apertura+ " "+a.orari.get(i).chiusura+"\n");
           }
        }
        /*intent = getIntent();
        bundle=intent.getBundleExtra("bundle");
        aula=bundle.getParcelable("aula");
        Orario o=bundle.getParcelable("orario");
        txt.setText(aula.idAula+"\n"+o.apertura+"\n"+o.chiusura);*/
    }























    private final SQLiteOpenHelper dbHelper = new SQLiteOpenHelper(InfoAulaActivity.this, "info_aule_offline", null, 1) {
        @Override
        public void onCreate(SQLiteDatabase db) {

            String sql = "CREATE TABLE \"info_aule_offline\" (\n" +
                    "\t\"id\"\tTEXT,\n" +
                    "\t\"nome\"\tTEXT,\n" +
                    "\t\"luogo\"\tTEXT,\n" +
                    "\t\"latitudine\"\tREAL,\n" +
                    "\t\"longitudine\"\tREAL,\n" +
                    "\t\"posti_totali\"\tINTEGER,\n" +
                    "\t\"posti_liberi\"\tINTEGER,\n" +
                    "\t\"flag_gruppi\"\tINTEGER,\n" +
                    "\t\"servizi\"\tTEXT,\n" +
                    "\tPRIMARY KEY(\"id\")\n" +
                    ")";
            db.execSQL(sql);

            String sql1 = "CREATE TABLE \"orari_offline\" (\n" +
                    "\t\"id_aula\"\tTEXT,\n" +
                    "\t\"giorno\"\tINTEGER,\n" +
                    "\t\"apertura\"\tTEXT,\n" +
                    "\t\"chiusura\"\tTEXT,\n" +
                    "\tPRIMARY KEY(\"id_aula\",\"giorno\")\n" +
                    ");";
            db.execSQL(sql1);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    };
























    @Override //creazione menu in alto
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST, "Logout");
        menu.add(Menu.FIRST, 2, Menu.FIRST + 1, "Home");
        return true;
    }

    @Override //se premo "Inserisci componente mi porta alla seconda activity"
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
            editor.commit();
            Intent i = new Intent(this, MainActivity.class);
            startActivityForResult(i, 100);
            finish();
        }
        if (item.getItemId() == 2) {
            Intent i = new Intent(this, Home.class);
            startActivityForResult(i, 100);
        }

        return true;
    }



}
