package es.um.poa.Objetos;

public enum Concepto {
    REGISTRO("Registro en Longja"),
    REGISTRAR_LOTE("Registrar Lote"),
    PUJA("Puja por lote"),
    ADJUDICACION("Adjudicacion de lote");

    private String name;

    Concepto(String s) {
        this.name = s;
    }

    public String getName() {
        return name;
    }
}
