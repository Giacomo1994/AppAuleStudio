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

    public void insertEventoCalendario(Prenotazione prenotazione, int id_calendar, int id_evento){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="INSERT OR IGNORE INTO eventi_calendario (id_prenotazione, id_calendar, id_evento) "+
                "VALUES (" +prenotazione.getId_prenotazione() + ", " + id_calendar +", " + id_evento +  ")";
        db.execSQL(sql);
    }
    public void deleteEventoCalendario(Prenotazione prenotazione){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="DELETE FROM eventi_calendario where id_prenotazione="+prenotazione.getId_prenotazione();
        db.execSQL(sql);
    }

    public ArrayList<Integer> getEventiFromPrenotazione(Prenotazione p){
        ArrayList<Integer> eventi=new ArrayList<Integer>();
        //TODO
        return eventi;
    }

    public void insertGruppi(Gruppo[] gruppi){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        if(gruppi.length==0){
            String sql="DELETE FROM gruppi_offline";
            db.execSQL(sql);
            return;
        }
        for(Gruppo g:gruppi){
            String sql="INSERT OR REPLACE INTO gruppi_offline "+
                    "VALUES ('" + g.getCodice_gruppo() + "', '" + g.getNome_gruppo() +"', '" + g.getNome_corso() +"', '"
                    + g.getNome_docente() +"', '" + g.getCognome_docente() +"', " + g.getOre_disponibili() +", '" + g.getData_scadenza() +"')";
            db.execSQL(sql);
        }
    }

    public void deleteGruppo(Gruppo g){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="DELETE FROM gruppi_offline where codice_gruppo = '" + g.getCodice_gruppo() + "'";
        db.execSQL(sql);
    }

    public ArrayList<Gruppo> selectGruppi(){
        ArrayList<Gruppo> gruppi=new ArrayList<Gruppo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM gruppi_offline";
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;

        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            String codice_gruppo=cursor.getString(cursor.getColumnIndex("codice_gruppo"));
            String nome_gruppo=cursor.getString(cursor.getColumnIndex("nome_gruppo"));
            String nome_corso=cursor.getString(cursor.getColumnIndex("nome_corso"));
            String nome_docente=cursor.getString(cursor.getColumnIndex("nome_docente"));
            String cognome_docente=cursor.getString(cursor.getColumnIndex("cognome_docente"));
            double ore_disponibili=cursor.getDouble(cursor.getColumnIndex("ore_disponibili"));
            String data_scadenza=cursor.getString(cursor.getColumnIndex("data_scadenza"));
            String date_now=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

            if(date_now.compareTo(data_scadenza)<=0){
                Gruppo g=new Gruppo(""+codice_gruppo,""+nome_gruppo,"","",100,ore_disponibili,""+data_scadenza);
                g.setNome_corso(nome_corso);
                g.setNome_docente(nome_docente);
                g.setCognome_docente(cognome_docente);
                gruppi.add(g);
            }
        }
        db.close();
        return gruppi;
    }

    public String selectNomegruppo(String codice_gruppo){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql="SELECT nome_gruppo FROM gruppi_offline where codice_gruppo = '" + codice_gruppo + "'";
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;
        String nome="";
        for(int i=0; i<cursor.getCount();i++) {
            cursor.moveToPosition(i);
            nome = cursor.getString(cursor.getColumnIndex("nome_gruppo"));
        }
        db.close();
        return nome;
    }


    public void insertPrenotazione(int id_prenotazione, String orario_prenotazione, String nome_aula, int tavolo, String gruppo){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="INSERT INTO prenotazioni_offline (id_prenotazione, orario_prenotazione, nome_aula, tavolo, gruppo) "+
                "VALUES (" +id_prenotazione + ", '" + orario_prenotazione + "', '" + nome_aula + "'," + tavolo + ",'" + gruppo + "')";
        db.execSQL(sql);
    }

    public void deletePrenotazione(int id_prenotazione){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="DELETE FROM prenotazioni_offline where id_prenotazione="+id_prenotazione;
        db.execSQL(sql);
    }

    public void insertPrenotazioniGruppi(ArrayList<Prenotazione> prenotazioni){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ArrayList<Prenotazione> pren_gruppi=new ArrayList<Prenotazione>();
        for(Prenotazione pren:prenotazioni){
            if(!pren.getGruppo().equals("null")) pren_gruppi.add(pren);
        }
        String sql0="DELETE FROM prenotazioni_offline where gruppo!='null' ";
        for(Prenotazione p: pren_gruppi){
            String sql="INSERT OR IGNORE INTO prenotazioni_offline (id_prenotazione, orario_prenotazione, nome_aula, tavolo, gruppo) "+
                    "VALUES (" +p.getId_prenotazione() + ", '" + p.getOrario_prenotazione() + "', '" + p.getAula() + "'," + p.getNum_tavolo() + ",'" + p.getGruppo() + "')";
            db.execSQL(sql);
            sql0+="AND gruppo!='" + p.getGruppo() + "' ";
        }
        db.execSQL(sql0);

    }

    public ArrayList<Prenotazione> selectPrenotazioni(){
        ArrayList<Prenotazione> prenotazioni=new ArrayList<Prenotazione>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM prenotazioni_offline";
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;

        for(int i=0; i<cursor.getCount();i++){
            cursor.moveToPosition(i);
            int id=cursor.getInt(cursor.getColumnIndex("id_prenotazione"));
            String orario_prenotazione=cursor.getString(cursor.getColumnIndex("orario_prenotazione"));
            String nome_aula=cursor.getString(cursor.getColumnIndex("nome_aula"));
            int tavolo=cursor.getInt(cursor.getColumnIndex("tavolo"));
            String gruppo=cursor.getString(cursor.getColumnIndex("gruppo"));

            String date_now=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            String date_prenotazione=orario_prenotazione.substring(0,10);

            if(date_prenotazione.compareTo(date_now)>=0)
                prenotazioni.add(new Prenotazione(id,"null","null",""+nome_aula,tavolo,""+orario_prenotazione,"null","null",-1, ""+gruppo, "null"));
        }
        db.close();
        return prenotazioni;
    }

    /*public void deletePrenotazioneGruppo(String codice_universita, String matricola, String orario_prenotazione, String gruppo){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql = "DELETE FROM prenotazioni_offline WHERE codice_universita='"+ codice_universita +"' AND matricola='"+ matricola +"' AND orario_prenotazione='"+ orario_prenotazione +"' AND gruppo='"+ gruppo +"'";
        db.execSQL(sql);
    }*/


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
