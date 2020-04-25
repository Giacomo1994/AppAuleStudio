package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Orario implements Serializable, Parcelable {
    String chiusura;
    String apertura;
    public Orario(String apertura, String chiusura) {
        this.chiusura = chiusura;
        this.apertura = apertura;
    }

    protected Orario(Parcel in) {
        chiusura = in.readString();
        apertura = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chiusura);
        dest.writeString(apertura);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Orario> CREATOR = new Creator<Orario>() {
        @Override
        public Orario createFromParcel(Parcel in) {
            return new Orario(in);
        }

        @Override
        public Orario[] newArray(int size) {
            return new Orario[size];
        }
    };


}
