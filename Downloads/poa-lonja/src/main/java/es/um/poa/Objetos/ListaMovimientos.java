package es.um.poa.Objetos;

import java.util.LinkedList;

/**
 * La Clase ListaMovimiento representa una lista de movimientos que tiene un
 * vendedor o un comprador. Esta clase fue creada para mejorar la legibilidad
 * de los movimeinto que tiene un comprador/vendedor.
 * Esta formado por:
 * - El cif de la persona de la que se tienen los movimientos
 * - Una lista (LINKEDLIST) de movimientos que tiene la persona
 */
public class ListaMovimientos {
    private final String cif;
    private LinkedList<Movimiento> movimientos;

    /**
     * Constructor ListaMovimientos
     * @param cif
     */
    public ListaMovimientos(String cif) {
        this.cif = cif;
        this.movimientos = new LinkedList<>();
    }

    public String getCif() {
        return cif;
    }

    public LinkedList<Movimiento> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(LinkedList<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    /**
     * Aniade un nuevo movimientoa la lista
     * @param movimiento
     */
    public void anadirMovimiento(Movimiento movimiento) {
        movimientos.add(movimiento);
    }

    /**
     * Retira un movimiento de la lista en caso de estar contenido en esta.
     * @param movimiento
     */
    public void eliminarMovimiento(Movimiento movimiento) {
        if (movimientos.contains(movimiento))
            movimientos.remove(movimiento);
    }
}
