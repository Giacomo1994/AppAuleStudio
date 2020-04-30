package com.example.appaulestudio;

public class Orario_Speciale {
    private String giorno_chiusura, giorno_riapertura;
    private String ora_chiusura, ora_riapertura;
    //2020-04-22 09:40:00
    public Orario_Speciale(String chiusura, String riapertura) {
        giorno_chiusura=chiusura.substring(0,10);
        giorno_riapertura=riapertura.substring(0,10);
        ora_chiusura=chiusura.substring(11,19);
        ora_riapertura=riapertura.substring(11,19);

    }

    public String getGiorno_chiusura() {
        return giorno_chiusura;
    }

    public void setGiorno_chiusura(String giorno_chiusura) {
        this.giorno_chiusura = giorno_chiusura;
    }

    public String getGiorno_riapertura() {
        return giorno_riapertura;
    }

    public void setGiorno_riapertura(String giorno_riapertura) {
        this.giorno_riapertura = giorno_riapertura;
    }

    public String getOra_chiusura() {
        return ora_chiusura;
    }

    public void setOra_chiusura(String ora_chiusura) {
        this.ora_chiusura = ora_chiusura;
    }

    public String getOra_riapertura() {
        return ora_riapertura;
    }

    public void setOra_riapertura(String ora_riapertura) {
        this.ora_riapertura = ora_riapertura;
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
