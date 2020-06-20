package com.example.appaulestudio;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Gruppo implements Parcelable {

    private String codice_gruppo, nome_gruppo, codice_corso, matricola_docente, nome_docente, cognome_docente, nome_corso;
    private int componenti_max;
    private double ore_disponibili;
    private String data_scadenza;


    public Gruppo(String codice_gruppo, String nome_gruppo, String codice_corso, String matricola_docente, int componenti_max, double ore_disponibili, String data_scadenza) {
        this.codice_gruppo = codice_gruppo;
        this.nome_gruppo = nome_gruppo;
        this.codice_corso = codice_corso;
        this.matricola_docente = matricola_docente;
        this.componenti_max = componenti_max;
        this.ore_disponibili = ore_disponibili;
        this.data_scadenza = data_scadenza;
    }
    public Gruppo(String codice_gruppo, String nome_gruppo, String nome_corso, String nome_docente, String cognome_docente, String data_scadenza) {
        this.codice_gruppo = codice_gruppo;
        this.nome_gruppo = nome_gruppo;
        this.nome_corso = nome_corso;
        this.nome_docente = nome_docente;
        this.cognome_docente = cognome_docente;
        this.data_scadenza = data_scadenza;
    }

    public Gruppo(Gruppo gruppo){
        this.codice_gruppo=gruppo.getCodice_gruppo();
        this.nome_gruppo=gruppo.getNome_gruppo();
        this.codice_corso=gruppo.getCodice_corso();
        this.matricola_docente=gruppo.getMatricola_docente();
        this.componenti_max=gruppo.getComponenti_max();
        this.ore_disponibili=gruppo.getOre_disponibili();
        this.data_scadenza=gruppo.getData_scadenza();
    }


    protected Gruppo(Parcel in) {
        codice_gruppo = in.readString();
        nome_gruppo = in.readString();
        codice_corso = in.readString();
        nome_corso=in.readString();
        matricola_docente = in.readString();
        componenti_max = in.readInt();
        ore_disponibili = in.readInt();
        data_scadenza = in.readString();
        nome_docente=in.readString();
        cognome_docente=in.readString();
    }


    public static final Creator<Gruppo> CREATOR = new Creator<Gruppo>() {
        @Override
        public Gruppo createFromParcel(Parcel in) {
            return new Gruppo(in);
        }

        @Override
        public Gruppo[] newArray(int size) {
            return new Gruppo[size];
        }
    };

    public String getCodice_gruppo() {
        return codice_gruppo;
    }

    public void setCodice_gruppo(String codice_gruppo) {
        this.codice_gruppo = codice_gruppo;
    }

    public String getNome_gruppo() {
        return nome_gruppo;
    }

    public void setNome_gruppo(String nome_gruppo) {
        this.nome_gruppo = nome_gruppo;
    }

    public String getCodice_corso() {
        return codice_corso;
    }

    public void setCodice_corso(String codice_corso) {
        this.codice_corso = codice_corso;
    }

    public String getMatricola_docente() {
        return matricola_docente;
    }

    public void setMatricola_docente(String matricola_docente) {
        this.matricola_docente = matricola_docente;
    }

    public int getComponenti_max() {
        return componenti_max;
    }

    public void setComponenti_max(int componenti_max) {
        this.componenti_max = componenti_max;
    }

    public double getOre_disponibili() {
        return ore_disponibili;
    }

    public void setOre_disponibili(double ore_disponibili) {
        this.ore_disponibili = ore_disponibili;
    }

    public String getData_scadenza() {
        return data_scadenza;
    }

    public void setData_scadenza(String data_scadenza) {
        this.data_scadenza = data_scadenza;
    }

    public String getNome_docente() {
        return nome_docente;
    }

    public void setNome_docente(String nome_docente) {
        this.nome_docente = nome_docente;
    }

    public String getCognome_docente() {
        return cognome_docente;
    }

    public void setCognome_docente(String cognome_docente) {
        this.cognome_docente = cognome_docente;
    }

    public String getNome_corso() {
        return nome_corso;
    }

    public void setNome_corso(String nome_corso) {
        this.nome_corso = nome_corso;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(codice_gruppo);
        parcel.writeString(nome_gruppo);
        parcel.writeString(codice_corso);
        parcel.writeString(nome_corso);
        parcel.writeString(matricola_docente);
        parcel.writeInt(componenti_max);
        parcel.writeDouble(ore_disponibili);
        parcel.writeString(data_scadenza);
        parcel.writeString(nome_docente);
        parcel.writeString(cognome_docente);
    }


}