package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Collection;

public class Corso implements Parcelable, Serializable {


    private String codiceCorso, nomeCorso;
    private int gruppi_totali, gruppi_in_scadenza, gruppi_scaduti;

    public Corso(String codiceCorso, String nomeCorso) {
        this.codiceCorso = codiceCorso;
        this.nomeCorso = nomeCorso;
        gruppi_totali=0;
        gruppi_scaduti=0;
        gruppi_in_scadenza=0;
    }

    public String getCodiceCorso() {
        return codiceCorso;
    }

    public String getNomeCorso() {
        return nomeCorso;
    }

    public void setCodiceCorso(String codiceCorso) {
        this.codiceCorso = codiceCorso;
    }

    public void setNomeCorso(String nomeCorso) {
        this.nomeCorso = nomeCorso;
    }

    public int getGruppi_totali() {
        return gruppi_totali;
    }

    public void setGruppi_totali(int gruppi_totali) {
        this.gruppi_totali = gruppi_totali;
    }

    public int getGruppi_in_scadenza() {
        return gruppi_in_scadenza;
    }

    public void setGruppi_in_scadenza(int gruppi_in_scadenza) {
        this.gruppi_in_scadenza = gruppi_in_scadenza;
    }

    public int getGruppi_scaduti() {
        return gruppi_scaduti;
    }

    public void setGruppi_scaduti(int gruppi_scaduti) {
        this.gruppi_scaduti = gruppi_scaduti;
    }

    public String toString(){
        String s=codiceCorso+" - "+nomeCorso;
        return s;
    }

    protected Corso(Parcel in) {
        codiceCorso = in.readString();
        nomeCorso = in.readString();
    }

    public static final Creator<Corso> CREATOR = new Creator<Corso>() {
        @Override
        public Corso createFromParcel(Parcel in) {
            return new Corso(in);
        }

        @Override
        public Corso[] newArray(int size) {
            return new Corso[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(codiceCorso);
        parcel.writeString(nomeCorso);
    }
}

