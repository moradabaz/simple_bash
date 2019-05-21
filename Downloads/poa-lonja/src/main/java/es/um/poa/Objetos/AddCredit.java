package es.um.poa.Objetos;

import java.io.Serializable;


public class AddCredit implements Serializable{


    private final String identificador;
    private final String nombre;
    private double budget;


    /**
     *  @param identificador
     * @param nombre
     * @param saldo
     */
    public AddCredit(String identificador, String nombre, double budget) {
        this.identificador = identificador;
        this.nombre = nombre;
        this.budget = budget;

    }

    /*
     * Retorna el identificador del comprador
     */
    public String getCif() {
        return identificador;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * @return Retorna su presupuesto actual
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Se establece el budget del comprador
     * @param budget
     */
    public void setBudget(int budget) {
        this.budget = budget;
    }


}
