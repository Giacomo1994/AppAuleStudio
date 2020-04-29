package com.example.appaulestudio;

public class Orario_Speciale {
    String giorno_chiusura, giorno_riapertura;
    String ora_chiusura, ora_riapertura;
    //2020-04-22 09:40:00
    public Orario_Speciale(String chiusura, String riapertura) {
        giorno_chiusura=chiusura.substring(0,10);
        giorno_riapertura=riapertura.substring(0,10);
        ora_chiusura=chiusura.substring(11,19);
        ora_riapertura=riapertura.substring(11,19);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orario_Speciale that = (Orario_Speciale) o;
        return giorno_chiusura.equals(that.giorno_chiusura) &&
                giorno_riapertura.equals(that.giorno_riapertura) &&
                ora_chiusura.equals(that.ora_chiusura) &&
                ora_riapertura.equals(that.ora_riapertura);
    }
}
