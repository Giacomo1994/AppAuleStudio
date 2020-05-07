package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

public class Orario_Ufficiale implements Comparable<Orario_Ufficiale>, Parcelable {
    private String data;
    private String giorno;
    private String apertura;
    private String chiusura;



    public Orario_Ufficiale(String data, String giorno, String apertura, String chiusura) {
        this.data = data;
        this.giorno = giorno;
        this.apertura = apertura;
        this.chiusura = chiusura;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getGiorno() {
        return giorno;
    }

    public void setGiorno(String giorno) {
        this.giorno = giorno;
    }

    public String getApertura() {
        return apertura;
    }

    public void setApertura(String apertura) {
        this.apertura = apertura;
    }

    public String getChiusura() {
        return chiusura;
    }

    public void setChiusura(String chiusura) {
        this.chiusura = chiusura;
    }

    @Override
    public int compareTo(Orario_Ufficiale o) {
        return this.getData().compareTo(o.getData());
    }

    protected Orario_Ufficiale(Parcel in) {
        data = in.readString();
        giorno = in.readString();
        apertura = in.readString();
        chiusura = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
        dest.writeString(giorno);
        dest.writeString(apertura);
        dest.writeString(chiusura);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Orario_Ufficiale> CREATOR = new Creator<Orario_Ufficiale>() {
        @Override
        public Orario_Ufficiale createFromParcel(Parcel in) {
            return new Orario_Ufficiale(in);
        }

        @Override
        public Orario_Ufficiale[] newArray(int size) {
            return new Orario_Ufficiale[size];
        }
    };


}
