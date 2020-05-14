package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;

public class Corso implements Parcelable {


    private String codiceCorso, nomeCorso, codiceUniversita, matricolaDocente;

    public Corso(String codiceCorso, String nomeCorso, String codiceUniversita, String matricolaDocente) {
        this.codiceCorso = codiceCorso;
        this.nomeCorso = nomeCorso;

        this.codiceUniversita = codiceUniversita;
        this.matricolaDocente = matricolaDocente;
    }

    protected Corso(Parcel in) {
        codiceCorso = in.readString();
        nomeCorso = in.readString();
        codiceUniversita = in.readString();
        matricolaDocente = in.readString();
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

    public String getCodiceCorso() {
        return codiceCorso;
    }

    public String getNomeCorso() {
        return nomeCorso;
    }

    public String getCodiceUniversita() {
        return codiceUniversita;
    }

    public String getMatricolaDocente() {
        return matricolaDocente;
    }

    public String toString(){
        String s=codiceCorso+" - "+nomeCorso;
        return s;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(codiceCorso);
        parcel.writeString(nomeCorso);
        parcel.writeString(codiceUniversita);
        parcel.writeString(matricolaDocente);
    }
}

