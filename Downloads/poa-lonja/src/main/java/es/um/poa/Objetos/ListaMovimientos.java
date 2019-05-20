package es.um.poa.Objetos;

import java.util.LinkedList;

public class ListaMovimientos {
    private final String cif;
    private LinkedList<Movimiento> movimientos;

    public ListaMovimientos(String cif) {
        this.cif = cif;
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

    public void anadirMovimiento(Movimiento movimiento) {
        movimientos.add(movimiento);
    }

    public void eliminarMovimiento(Movimiento movimiento) {
        if (movimientos.contains(movimiento))
            movimientos.remove(movimiento);
    }
}
