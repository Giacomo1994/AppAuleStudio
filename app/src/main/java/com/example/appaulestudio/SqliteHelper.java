package com.example.appaulestudio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteHelper extends SQLiteOpenHelper {
    public SqliteHelper(Context context) {
        super(context, "info_aule_offline", null, 1);
    }

    public void onCreate(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE \"info_aule_offline\" (\n" +
                "\t\"id\"\tTEXT,\n" +
                "\t\"nome\"\tTEXT,\n" +
                "\t\"luogo\"\tTEXT,\n" +
                "\t\"latitudine\"\tREAL,\n" +
                "\t\"longitudine\"\tREAL,\n" +
                "\t\"posti_totali\"\tINTEGER,\n" +
                "\t\"flag_gruppi\"\tINTEGER,\n" +
                "\t\"servizi\"\tTEXT,\n" +
                "\t\"last_update\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"id\")\n" +
                ")";
        db.execSQL(sql);

        String sql1 = "CREATE TABLE \"orari_offline\" (\n" +
                "\t\"id_aula\"\tTEXT,\n" +
                "\t\"giorno\"\tINTEGER,\n" +
                "\t\"apertura\"\tTEXT,\n" +
                "\t\"chiusura\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"id_aula\",\"giorno\")\n" +
                ")";
        db.execSQL(sql1);

        String sql2 = "CREATE TABLE \"prenotazioni_offline\" (\n" +
                "\t\"id_prenotazione\"\tINTEGER PRIMARY KEY,\n" +
                "\t\"orario_prenotazione\"\tTEXT,\n" +
                "\t\"nome_aula\"\tTEXT,\n" +
                "\t\"tavolo\"\tINTEGER," +
                "\t\"gruppo\"\tTEXT)";
        db.execSQL(sql2);

        String sql3 = "CREATE TABLE \"eventi_calendario\" (\n" +
                "\t\"id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"id_prenotazione\"\tINTEGER,\n" +
                "\t\"id_calendar\"\tINTEGER,\n" +
                "\t\"id_evento\"\tINTEGER)";
        db.execSQL(sql3);

        String sql4 = "CREATE TABLE \"gruppi_offline\" (\n" +
                "\t\"codice_gruppo\"\tTEXT PRIMARY KEY,\n" +
                "\t\"nome_gruppo\"\tTEXT,\n" +
                "\t\"nome_corso\"\tTEXT,\n" +
                "\t\"nome_docente\"\tTEXT,\n" +
                "\t\"cognome_docente\"\tTEXT,\n" +
                "\t\"ore_disponibili\"\tREAL,\n" +
                "\t\"data_scadenza\"\tTEXT)";
        db.execSQL(sql4);

        String sql5 = "CREATE TABLE \"alarm_trigger\" (\n" +
                "\t\"id_prenotazione\"\tINTEGER PRIMARY KEY,\n" +
                "\t\"orario_alarm\"\tTEXT)";
        db.execSQL(sql5);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    { }




}
