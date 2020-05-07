package com.example.appaulestudio;

import java.util.Date;

public class Gruppo {

    private String codice_gruppo, nome_gruppo, codice_corso, matricola_docente;
    private int  componenti_max, ore_disponibili;
    private String data_scadenza;


    public Gruppo(String codice_gruppo,String nome_gruppo, String codice_corso,
                  String matricola_docente, int componenti_max,
                  int ore_disponibili, String data_scadenza) {
        this.codice_gruppo = codice_gruppo;
        this.codice_corso = codice_corso;

        this.nome_gruppo = nome_gruppo;
        this.matricola_docente = matricola_docente;
        this.componenti_max = componenti_max;

        this.ore_disponibili = ore_disponibili;
        this.data_scadenza = data_scadenza;
    }

    public void setCodice_gruppo(String codice_gruppo) {
        this.codice_gruppo = codice_gruppo;
    }
    public void setNome_gruppo(String nome_gruppo) {
        this.nome_gruppo= nome_gruppo;
    }

    public void setCodice_corso(String codice_corso) {
        this.codice_corso = codice_corso;
    }



    public void setMatricola_docente(String matricola_docente) {
        this.matricola_docente = matricola_docente;
    }

    public void setComponenti_max(int componenti_max) {
        this.componenti_max = componenti_max;
    }



    public void setOre_disponibili(int ore_disponibili) {
        this.ore_disponibili = ore_disponibili;
    }

    public void setData_scadenza(String data_scadenza) {
        this.data_scadenza = data_scadenza;
    }

    public String getCodice_gruppo() {
        return codice_gruppo;
    }

    public String getCodice_corso() {
        return codice_corso;
    }


    public String getMatricola_docente() {
        return matricola_docente;
    }

    public int getComponenti_max() {
        return componenti_max;
    }

    public String getNome_gruppo() {
        return nome_gruppo;
    }

    public int getOre_disponibili() {
        return ore_disponibili;
    }

    public String getData_scadenza() {
        return data_scadenza;
    }
}
