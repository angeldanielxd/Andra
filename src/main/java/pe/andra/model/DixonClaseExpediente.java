package pe.andra.model;

public enum DixonClaseExpediente {
    DOC,
    OBS,
    LAB,
    EQ;

    public boolean dixonEsReservaOPrestamo() {
        return this == LAB || this == EQ;
    }
}
