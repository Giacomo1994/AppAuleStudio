package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;



public class Aula implements Parcelable, Comparable<Aula> {
    private String idAula;
    private String nome;
    private String luogo;
    private double latitudine;
    private double longitudine;
    private int gruppi;
    private int posti_totali;
    private int posti_liberi;
    private String servizi;
    private String last_update;

    private Map<Integer, Orario> orari;
    private boolean aperta;


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
        last_update=null;
    }

    @Override
    public String toString() {
        return nome;
    }

    public String getIdAula() {
        return idAula;
    }

    public void setIdAula(String idAula) {
        this.idAula = idAula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLuogo() {
        return luogo;
    }

    public void setLuogo(String luogo) {
        this.luogo = luogo;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public int getGruppi() {
        return gruppi;
    }

    public void setGruppi(int gruppi) {
        this.gruppi = gruppi;
    }

    public int getPosti_totali() {
        return posti_totali;
    }

    public void setPosti_totali(int posti_totali) {
        this.posti_totali = posti_totali;
    }

    public int getPosti_liberi() {
        return posti_liberi;
    }

    public void setPosti_liberi(int posti_liberi) {
        this.posti_liberi = posti_liberi;
    }

    public String getServizi() {
        return servizi;
    }

    public void setServizi(String servizi) {
        this.servizi = servizi;
    }

    public Map<Integer, Orario> getOrari() {
        return orari;
    }

    public void setOrari(Map<Integer, Orario> orari) {
        this.orari = orari;
    }


    public boolean isAperta() {
        return aperta;
    }

    public void setAperta(boolean aperta) {
        this.aperta = aperta;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
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
        last_update=in.readString();
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
        dest.writeString(last_update);
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

    @Override
    public int compareTo(Aula o) {
        return nome.compareTo(o.getNome());
    }
}
