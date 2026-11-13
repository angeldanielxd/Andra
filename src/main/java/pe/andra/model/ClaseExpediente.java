package pe.andra.model;

public enum ClaseExpediente {
    DOC,
    OBS,
    LAB,
    EQ;

    public boolean esReservaOPrestamo() {
        return this == LAB || this == EQ;
    }
}
