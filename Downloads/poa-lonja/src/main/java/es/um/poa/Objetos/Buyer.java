package es.um.poa.Objetos;

import es.um.poa.productos.Fish;

import java.io.Serializable;
import java.util.LinkedList;

public class Buyer implements Serializable {

    private final String identificador;
    private final String nombre;
    private double saldo;
    private double gastos;
    private LinkedList<Fish> articulosComprados;


    /**
     *  @param identificador
     * @param nombre
     * @param saldo
     */
    public Buyer(String identificador, String nombre, double saldo) {
        this.identificador = identificador;
        this.nombre = nombre;
        this.saldo = saldo;
        this.gastos = 0;
        this.articulosComprados = new LinkedList<>();

    }

    /*
     * Retorna el identificador del comprador
     */
    public String getCif() {
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
    
    public void comprarArticulo(Fish fish) {
        if (!articulosComprados.contains(fish))
            articulosComprados.add(fish);
    }

    public void setArticulosComprados(LinkedList<Fish> articulosComprados) {
        this.articulosComprados = articulosComprados;
    }

    public String getNombre() {
        return nombre;
    }
}
