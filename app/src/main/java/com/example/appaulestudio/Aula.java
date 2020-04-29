package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Aula implements Parcelable {
     String idAula;
     String nome;
     String luogo;
     double latitudine;
     double longitudine;
     int gruppi;
     int posti_totali;
     int posti_liberi;
     String servizi;
     //String last_update;

     Map<Integer, Orario> orari;
     Orario orario;

     boolean aperta;


    public Aula(String idAula, String nome, String luogo, double latitudione,
                double longitudine, int gruppi, int posti_totali, int posti_liberi, String servizi){
        this.idAula=idAula;
        this.nome=nome;
        this.luogo=luogo;
        this.latitudine=latitudione;
        this.longitudine=longitudine;
        this.gruppi=gruppi;
        this.posti_totali=posti_totali;
        this.posti_liberi=posti_liberi;
        this.servizi=servizi;
        this.orari=new HashMap<Integer, Orario>();
        this.aperta=false;
        this.orario=null;
        //this.last_update=null;
    }

    //"yyyy-MM-dd HH:mm:ss"
    public boolean isAperta(String currentTime){
        //controllo orari speciali
        if(aperta==false) return false;
        //controllo orari default
        String orarioAttuale=currentTime.substring(11,19);
        String aperturaDefault=orario.apertura;
        String chiusuraDefault=orario.chiusura;
        if(orarioAttuale.compareTo(aperturaDefault)<0||orarioAttuale.compareTo(chiusuraDefault)>0) return false;
        return true;
    }

    protected Aula(Parcel in) {
        idAula = in.readString();
        nome = in.readString();
        luogo = in.readString();
        latitudine = in.readDouble();
        longitudine = in.readDouble();
        gruppi = in.readInt();
        posti_totali = in.readInt();
        posti_liberi = in.readInt();
        servizi = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idAula);
        dest.writeString(nome);
        dest.writeString(luogo);
        dest.writeDouble(latitudine);
        dest.writeDouble(longitudine);
        dest.writeInt(gruppi);
        dest.writeInt(posti_totali);
        dest.writeInt(posti_liberi);
        dest.writeString(servizi);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Aula> CREATOR = new Creator<Aula>() {
        @Override
        public Aula createFromParcel(Parcel in) {
            return new Aula(in);
        }

        @Override
        public Aula[] newArray(int size) {
            return new Aula[size];
        }
    };
}
