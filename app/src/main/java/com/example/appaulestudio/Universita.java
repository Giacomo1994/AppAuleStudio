package com.example.appaulestudio;

public class Universita {
    public String codice;
    public String nome;

    public Universita(String codice, String nome) {
        this.codice = codice;
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
