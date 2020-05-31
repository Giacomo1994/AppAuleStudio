package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Collection;

public class Corso implements Parcelable, Serializable {


    private String codiceCorso, nomeCorso;

    public Corso(String codiceCorso, String nomeCorso) {
        this.codiceCorso = codiceCorso;
        this.nomeCorso = nomeCorso;
    }

    public String getCodiceCorso() {
        return codiceCorso;
    }

    public String getNomeCorso() {
        return nomeCorso;
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

