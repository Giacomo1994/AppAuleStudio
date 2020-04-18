package com.example.appaulestudio;

public class Aula {

    private String idAula;
    private String idUniversita;
    private String nome;
    private String indirizzo;
    private double latitudine;
    private double longitudine;
    private boolean gruppi;


    public Aula(String idAula, String idUniversita, String nome, String indirizzo, double latitudione,
                double longitudine, boolean gruppi){
        this.idAula=idAula;
        this.idUniversita=idUniversita;
        this.nome=nome;
        this.indirizzo=indirizzo;
        this.latitudine=latitudione;
        this.longitudine=longitudine;
        this.gruppi=gruppi;
    }

    public String stampaAula(Aula a){
        String s=""+a.nome+" - "+a.idUniversita+"/n"+a.indirizzo;
        if(a.gruppi==true){
            s+="/nAula disponibile alla prenotazione per gruppi";
        }
        return s;
    }
}
