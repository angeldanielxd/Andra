package pe.andra.model;

public enum AndraClaseExpediente {
    DOC,
    OBS,
    LAB,
    EQ;

    public boolean andraEsReservaOPrestamo() {
        return this == LAB || this == EQ;
    }
}
