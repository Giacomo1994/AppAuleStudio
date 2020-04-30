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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    { }




}
