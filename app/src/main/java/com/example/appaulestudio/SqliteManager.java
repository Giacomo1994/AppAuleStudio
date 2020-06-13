package com.example.appaulestudio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class SqliteManager {
    private SqliteHelper dbHelper;
    public SqliteManager(Context ctx) {
        dbHelper=new SqliteHelper(ctx);
    }

//ALARM_TRIGGER
    public void insertAlarm(int id_prenotazione, String orario_alarm){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="INSERT OR REPLACE INTO alarm_trigger (id_prenotazione, orario_alarm) "+
                "VALUES (" +id_prenotazione + ", '" + orario_alarm + "')";
        db.execSQL(sql);
    }

    public void deleteAlarm(int id_prenotazione){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="DELETE FROM alarm_trigger WHERE id_prenotazione="+id_prenotazione;
        db.execSQL(sql);
    }

    public LinkedList<AlarmClass> getAlarms(){
        LinkedList<AlarmClass> allarmi=new LinkedList<AlarmClass>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM alarm_trigger";
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;
        for(int i=0; i<cursor.getCount();i++) {
            cursor.moveToPosition(i);
            int id_prenotazione = cursor.getInt(cursor.getColumnIndex("id_prenotazione"));
            String orario_alarm = cursor.getString(cursor.getColumnIndex("orario_alarm"));
            allarmi.add(new AlarmClass(id_prenotazione,orario_alarm));
        }
        db.close();
        return allarmi;
    }

    public boolean isAllarmeGiaInserito(int id_prenotazione){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM alarm_trigger where id_prenotazione="+id_prenotazione;
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return false;
        return true;
    }


//EVENTI_CALENDARIO
    public boolean is_prenotazione_sincronizzata(int id_prenotazione){
        ArrayList<CalendarEvent> eventi=new ArrayList<CalendarEvent>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM eventi_calendario WHERE id_prenotazione="+id_prenotazione;
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return false;
        else return true;
    }

    public void insertEventoCalendario(int id_prenotazione, int id_calendar, int id_evento){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="INSERT OR IGNORE INTO eventi_calendario (id_prenotazione, id_calendar, id_evento) "+
                "VALUES (" +id_prenotazione + ", " + id_calendar +", " + id_evento +  ")";
        db.execSQL(sql);
    }

    public void deleteEventoCalendario(int id_prenotazione){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="DELETE FROM eventi_calendario where id_prenotazione="+id_prenotazione;
        db.execSQL(sql);
    }

    public ArrayList<CalendarEvent> getEventiFromPrenotazione(int id_prenotazione){
        ArrayList<CalendarEvent> eventi=new ArrayList<CalendarEvent>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM eventi_calendario WHERE id_prenotazione="+id_prenotazione;
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return null;
        for(int i=0; i<cursor.getCount();i++) {
            cursor.moveToPosition(i);
            int id_calendar = cursor.getInt(cursor.getColumnIndex("id_calendar"));
            int id_event = cursor.getInt(cursor.getColumnIndex("id_evento"));
            eventi.add(new CalendarEvent(id_prenotazione,id_calendar,id_event));
        }
        db.close();
        return eventi;
    }

//GRUPPI_OFFLINE
    public void insertGruppi(Gruppo[] gruppi){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        if(gruppi.length==0){
            String sql="DELETE FROM gruppi_offline";
            db.execSQL(sql);
            return;
        }
        for(Gruppo g:gruppi){
            String sql="INSERT OR IGNORE INTO gruppi_offline "+
                    "VALUES ('" + g.getCodice_gruppo() + "', '" + g.getNome_gruppo() +"', '" + g.getNome_corso() +"', '"
                    + g.getNome_docente() +"', '" + g.getCognome_docente() +"', " + g.getOre_disponibili() +", '" + g.getData_scadenza() +"')";
            String sql1="UPDATE gruppi_offline set ore_disponibili="+g.getOre_disponibili()+", data_scadenza='"+g.getData_scadenza()+"' where codice_gruppo='"+g.getCodice_gruppo()+"'";
            db.execSQL(sql);
            db.execSQL(sql1);
        }

        String sql2 = "DELETE FROM gruppi_offline ";
        if(gruppi.length==0) db.execSQL(sql2);
        sql2+="WHERE ";
        for(int i=0;i<gruppi.length;i++){
            if(i==0) sql2+="codice_gruppo!='"+gruppi[i].getCodice_gruppo()+"' ";
            else sql2+="AND codice_gruppo!='"+gruppi[i].getCodice_gruppo()+"' ";
        }
        db.execSQL(sql2);

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


//PRENOTAZIONI_OFFLINE
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

    public void insertPrenotazioni(ArrayList<Prenotazione> prenotazioni){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        for(Prenotazione p:prenotazioni){
            String sql="INSERT OR IGNORE INTO prenotazioni_offline (id_prenotazione, orario_prenotazione, nome_aula, tavolo, gruppo) "+
                    "VALUES (" +p.getId_prenotazione() + ", '" + p.getOrario_prenotazione() + "', '" + p.getAula() + "'," + p.getNum_tavolo() + ",'" + p.getGruppo() + "')";
            db.execSQL(sql);
        }
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


//AULE_OFFLINE E ORARI_OFFLINE

    public ArrayList<Aula> readListaAule(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Aula> aule=new ArrayList<Aula>();
        Cursor cursor=null;
        String sql="";

        sql = "SELECT * FROM info_aule_offline ORDER BY id ASC";
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

    public boolean isAulaDaAggiornare(Aula a){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM info_aule_offline WHERE id='"+a.getIdAula()+"' AND last_update='"+a.getLast_update()+"'";
        Cursor cursor = db.rawQuery(sql, null);  //creazione cursore
        if(cursor==null ||cursor.getCount()==0) return true;
        return false;
    }

    public ArrayList<Aula> getAuleDaAggiornare(Aula[] array_aula){
        ArrayList<Aula> auleDaAggiornare=new ArrayList<Aula>();
        for(Aula a:array_aula){
            if(isAulaDaAggiornare(a)) auleDaAggiornare.add(a);
        }
        return auleDaAggiornare;
    }

    public void aggiornaAula(Aula a){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO info_aule_offline (id, nome, luogo, latitudine, longitudine,posti_totali, flag_gruppi, servizi, last_update) " +
                        "VALUES ('" + a.getIdAula() + "', '" + a.getNome() + "', '" + a.getLuogo() + "', " + a.getLatitudine() + "," + a.getLongitudine() + "," + a.getPosti_totali() +  "," + a.getGruppi() + ",'" + a.getServizi() + "', '"+a.getLast_update()+"')";
        db.execSQL(sql);
    }

    public void aggiornaOrariAula(Aula a, HashMap<Integer,Orario> orari){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        for(int i = 1; i<=7; i++) {
            String sql = "INSERT OR REPLACE INTO orari_offline (id_aula, giorno, apertura, chiusura)" +
                    "VALUES('" + a.getIdAula() + "', " + i + ", '" + orari.get(i).getApertura() + "','" + orari.get(i).getChiusura() + "')";
            db.execSQL(sql);
        }
    }

    public void delete_aule_offline(Aula[] array_aula){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql = "DELETE FROM info_aule_offline ";
        if(array_aula.length==0) db.execSQL(sql);
        sql+="WHERE ";
        for(int i=0;i<array_aula.length;i++){
            if(i==0) sql+="id!='"+array_aula[i].getIdAula()+"' ";
            else sql+="AND id!='"+array_aula[i].getIdAula()+"' ";
        }
        db.execSQL(sql);
    }
}
