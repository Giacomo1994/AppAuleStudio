package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

public class Aula implements Parcelable {

     String idAula;
     String nome;
     String luogo;
     double latitudine;
     double longitudine;
     int gruppi;
     int posti_liberi;

    public Aula(String idAula, String nome, String luogo, double latitudione,
                double longitudine, int gruppi, int posti_liberi){
        this.idAula=idAula;

        this.nome=nome;
        this.luogo=luogo;
        this.latitudine=latitudione;
        this.longitudine=longitudine;
        this.gruppi=gruppi;
        this.posti_liberi=posti_liberi;
    }

    public String stampaAula(){
        String s=""+this.nome+" - "+"/n"+this.luogo;
        if(this.gruppi==0){
            s+="/nAula disponibile alla prenotazione per gruppi";
        }
        return s;
    }

    protected Aula(Parcel in) {
        idAula = in.readString();
        nome = in.readString();
        luogo = in.readString();
        latitudine = in.readDouble();
        longitudine = in.readDouble();
        gruppi = in.readInt();
        posti_liberi = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idAula);
        dest.writeString(nome);
        dest.writeString(luogo);
        dest.writeDouble(latitudine);
        dest.writeDouble(longitudine);
        dest.writeInt(gruppi);
        dest.writeInt(posti_liberi);
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
