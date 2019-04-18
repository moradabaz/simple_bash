package es.um.poa.Objetos;

import es.um.poa.productos.Fish;

import java.util.LinkedList;

public class Buyer {

    private final String identificador;
    private double saldo;
    private double gastos;
    private LinkedList<Fish> articulosComprados;


    /**
     *
     * @param identificador
     * @param saldo
     */
    public Buyer(String identificador, double saldo) {
        this.identificador = identificador;
        this.saldo = saldo;
        this.gastos = 0;
        this.articulosComprados = new LinkedList<>();

    }

    /*
     * Retorna el identificador del comprador
     */
    public String getIdentificador() {
        return identificador;
    }

    /**
     * @return Retorna su saldo actual
     */
    public double getSaldo() {
        return saldo;
    }

    /**
     * Se establece el saldo del comprador
     * @param saldo
     */
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    /**
     * @return
     */
    public double getGastos() {
        return gastos;
    }

    public void setGastos(double gastos) {
        this.gastos = gastos;
    }

    public LinkedList<Fish> getArticulosComprados() {
        return articulosComprados;
    }

    public void setArticulosComprados(LinkedList<Fish> articulosComprados) {
        this.articulosComprados = articulosComprados;
    }


}
