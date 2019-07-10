package es.um.poa.Objetos;


/**
 * El enumerado concepto presenta los distintos conceptos/motivos por los que
 * se hace el registro del movimiento
 */
public enum Concepto {
    REGISTRO("Registro en Lonja"),
    REGISTRAR_LOTE("Registrar Lote"),
    PUJA("Puja por lote"),
    GANANCIA("Retiro de ganancia"),
    ADJUDICACION("Adjudicacion de lote");


    private String name;

    Concepto(String s) {
        this.name = s;
    }

    public String getName() {
        return name;
    }
}
