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

    public Movimiento(String cif, Concepto concepto) {
        this.ID = contador;
        contador++;
        this.cif = cif;
        this.concepto = concepto;

    }

    public Movimiento(String cif, Concepto concepto, String descripcion) {
        this.ID = contador;
        contador++;
        this.cif = cif;
        this.concepto = concepto;
        this.descripcion = descripcion;
    }

    public String getCif() {
        return cif;
    }

    public Concepto getConcepto() {
        return concepto;
    }

    public void setConcepto(Concepto concepto) {
        this.concepto = concepto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String toString() {
        String cadena = "";
        cadena += "ID: " + ID + "\n";
        cadena += " Cif: " + cif + "\n";
        cadena += " Concepto " + concepto.getName() + "\n";
        cadena += " Descripcion: " + descripcion + "\n";
        return cadena;
    }



}
