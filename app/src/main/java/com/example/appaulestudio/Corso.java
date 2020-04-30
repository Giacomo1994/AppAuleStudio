package com.example.appaulestudio;

public class Corso {


    private String codiceCorso, nomeCorso, codiceUniversita, matricolaDocente;

    public Corso(String codiceCorso, String nomeCorso, String codiceUniversita, String matricolaDocente) {
        this.codiceCorso = codiceCorso;
        this.nomeCorso = nomeCorso;

        this.codiceUniversita = codiceUniversita;
        this.matricolaDocente = matricolaDocente;
    }

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
}

