package com.example.appaulestudio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SqliteManager {
    private SqliteHelper dbHelper;
    public SqliteManager(Context ctx) {
        dbHelper=new SqliteHelper(ctx);
    }


    public void writeAuleOrari(Aula[] array_aula){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql1 = "DELETE FROM info_aule_offline";
        String sql2="DELETE FROM orari_offline";
        db.execSQL(sql2);
        db.execSQL(sql1);
        for (Aula a : array_aula) {
            String sql =
                    "INSERT INTO info_aule_offline (id, nome, luogo, latitudine, longitudine,posti_totali,posti_liberi, flag_gruppi, servizi) " +
                            "VALUES ('" + a.getIdAula() + "', '" + a.getNome() + "', '" + a.getLuogo() + "', " + a.getLatitudine() + "," + a.getLongitudine() + "," + a.getPosti_totali() + "," + a.getPosti_liberi() + "," + a.getGruppi() + ",'" + a.getServizi() + "')";
            db.execSQL(sql);
        }
        for(Aula a : array_aula){
            for(int i = 1; i<=7; i++) {
                String sql = "INSERT INTO orari_offline (id_aula, giorno, apertura, chiusura)" +
                        "VALUES('" + a.getIdAula() + "', " + i + ", '" + a.getOrari().get(i).getApertura() + "','" + a.getOrari().get(i).getChiusura() + "')";
                db.execSQL(sql);
            }
        }
    }

    public ArrayList<Aula> readListaAule(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Aula> aule=new ArrayList<Aula>();
        Cursor cursor=null;
        String sql="";

        sql = "SELECT * FROM info_aule_offline";
        cursor = db.rawQuery(sql, null);
        if(cursor==null ||cursor.getCount()==0) return null;
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
            String servizi=cursor.getString(cursor.getColumnIndex("servizi"));
            Aula a=new Aula(id,nome,luogo,latitudine,longitudine,flag_gruppi,posti_totali,posti_liberi,servizi);
            aule.add(a);
        }

        sql = "SELECT * FROM orari_offline";
        cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;
        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            String id=cursor.getString(cursor.getColumnIndex("id_aula"));
            int giorno=cursor.getInt(cursor.getColumnIndex("giorno"));
            String apertura=cursor.getString(cursor.getColumnIndex("apertura"));
            String chiusura=cursor.getString(cursor.getColumnIndex("chiusura"));
            for(Aula a: aule){
                if(a.getIdAula().equals(id)){
                    a.getOrari().put(giorno,new Orario(apertura,chiusura));
                }
            }
        }
        db.close();
        return aule;
    }

    public HashMap<Integer,Orario> readOrariAula(String id_aula){
        HashMap<Integer,Orario> mappa_orari=new HashMap<Integer, Orario>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM orari_offline where id_aula='"+id_aula+"'";
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;

        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            //String id=cursor.getString(cursor.getColumnIndex("id_aula"));
            int giorno=cursor.getInt(cursor.getColumnIndex("giorno"));
            String apertura=cursor.getString(cursor.getColumnIndex("apertura"));
            String chiusura=cursor.getString(cursor.getColumnIndex("chiusura"));
            mappa_orari.put(giorno, new Orario(apertura,chiusura));
        }
        db.close();
        return mappa_orari;
    }

    public String getNomeAula(String id_aula){
       String aula="";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM info_aule_offline where id='"+id_aula+"'";
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;

        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            aula=cursor.getString(cursor.getColumnIndex("nome"));
        }
        db.close();
        return aula;
    }










}
