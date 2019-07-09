package es.um.poa.Objetos;

/**
 * La clase movimiento representa los movimientos que realizan los vendedores y compradores
 * en la lonja para que queden registrados en esta.
 * Contiene:
 * - El cif de la persona que realiza el movimiento
 * - El concepto en el que se registra el movimiento
 * - una breve descripción del movimiento
 */
public class Movimiento {
    private static int contador = 0;
    private final int ID;
    private final String cif;
    private Concepto concepto;
    private String descripcion;


    /**
     * Constructor de la clase movimiento
     * @param cif   El cif de la persona (vendedor/comprador) de la que se registran los movimientos
     * @param concepto  El concepto por el que se registra le nuevo movimiento
     */
    public Movimiento(String cif, Concepto concepto) {
        this.ID = contador;
        contador++;
        this.cif = cif;
        this.concepto = concepto;

    }

    /**
     * Constructor de la clase movimiento
     * @param cif
     * @param concepto
     * @param descripcion
     */
    public Movimiento(String cif, Concepto concepto, String descripcion) {
        this.ID = contador;
        contador++;
        this.cif = cif;
        this.concepto = concepto;
        this.descripcion = descripcion;
    }

    /**
     *
     * @return
     */
    public String getCif() {
        return cif;
    }


    /**
     *
     * @return
     */
    public Concepto getConcepto() {
        return concepto;
    }

    /**
     *
     * @param concepto
     */
    public void setConcepto(Concepto concepto) {
        this.concepto = concepto;
    }

    /**
     *
     * @return
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     *
     * @param descripcion
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     *
     * @return
     */
    public String toString() {
        String cadena = "";
        cadena += " Cif: " + cif + "\n";
        cadena += " Concepto " + concepto.getName() + "\n";
        cadena += " Descripcion: " + descripcion + "\n";
        return cadena;
    }



}
