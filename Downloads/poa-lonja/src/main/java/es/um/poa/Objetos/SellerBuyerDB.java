package es.um.poa.Objetos;

import es.um.poa.productos.EstadoVenta;
import es.um.poa.productos.Fish;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Esta clase sirve como Base de datos para almacenar los compradores y vendedores que se registran en la lonja. Esta clase implementa
 * el patrón Singleton.
 * Esta formado por:
 *  - un mapa de vendedores en el que se utiliza su numero de identificacion como clave
 *  - un mapa de compradores en el que se utiliza su numero de identificacion como clave
 */
public class SellerBuyerDB {

    private static SellerBuyerDB ourInstance;
    private Map<String, Seller> sellers;
    private Map<String, Buyer> buyers;
    private Map<String, ListaMovimientos> movimientosSeller;
    private Map<String, ListaMovimientos> movimientosBuyer;
    private LinkedList<Fish> lotes;


    public static SellerBuyerDB getInstance() {
        if (ourInstance == null) {
            ourInstance = new SellerBuyerDB();
        }
        return ourInstance;
    }


    /**
     * Constructor
     */
    private SellerBuyerDB() {
        this.sellers = new HashMap<String, Seller>();
        this.buyers = new HashMap<String, Buyer>();
        this.movimientosBuyer = new HashMap<String, ListaMovimientos>();
        this.movimientosSeller = new HashMap<String, ListaMovimientos>();
        this.lotes = new LinkedList<>();
    }

    /**
     * Registra un Vendedor en el mapa de compradores
     * @param seller Vendedor a registrar
     */
    public void actualizarSeller(Seller seller) {
        sellers.put(seller.getCif(), seller);
    }


    /**
     * Registra un comprador en el mapa de compradores
     * @param buyer Comprador a registrar
     */
    public void registrarBuyer(Buyer buyer){
        buyers.put(buyer.getCif(), buyer);
    }



    /**
     * Comprueba que existe un vendedor dado su identificador
     * @param sellerID
     * @return
     */
    public boolean isSellerRegistered(String sellerID) {
        return sellers.containsKey(sellerID);
    }

    /**
     * Elimina un vendedor dado su ID
     * @param sellerID
     */
    public void removeSellerByID(String sellerID) {
        sellers.remove(sellerID);
    }

    /**
     * Elimina un vendedor
     * @param seller
     */
    public void removeSeller(Seller seller) {
        sellers.remove(seller.getCif(), seller);
    }


    /**
     *
     * @param sellerID
     * @return
     */
    public Seller getSeller(String sellerID) {
        return sellers.getOrDefault(sellerID, null);
    }


    /**
     * Comprueaba que existe un comprador
     * @param buyerID
     * @return
     */
    public boolean isBuyerRegistered(String buyerID) {
        return buyers.containsKey(buyerID);
    }

    /**
     * Elimina un comprador
     * @param buyerID
     */
    public void removerBuyerByID(String buyerID) {
        buyers.remove(buyerID);
    }


    /**
     *
     * @param buyerID
     * @return
     */
    public Buyer getBuyer(String buyerID) {
        return buyers.getOrDefault(buyerID, null);
    }


    /**
     *
     * @return
     */
    public Set<String> getSellerIDs() {
        return sellers.keySet();
    }


    /**
     *
     * @return
     */
    public LinkedList<Buyer> getAllBuyers() {
        return new LinkedList<>(buyers.values());
    }

    /**
     * Registra un movimiento de un vendedor
     * @param cifSeller
     * @param movimiento
     */
    public void registarMovimientoSeller(String cifSeller, Movimiento movimiento) {
        if (movimientosSeller.containsKey(cifSeller)) {
            ListaMovimientos listaMovimientos = getListaMovimientoSeller(cifSeller);
            listaMovimientos.anadirMovimiento(movimiento);
            this.movimientosSeller.put(cifSeller, listaMovimientos);
        } else {
            ListaMovimientos listaMovimientos = new ListaMovimientos(cifSeller);
            listaMovimientos.anadirMovimiento(movimiento);
            this.movimientosSeller.put(cifSeller, listaMovimientos);
        }
    }

    /**
     *
     * Registra un movimiento de un comprador
     * @param cifBuyer
     * @param movimiento
     */
    public void registrarMovimientoBuyer(String cifBuyer, Movimiento movimiento) {
        if (movimientosBuyer.containsKey(cifBuyer)) {
            ListaMovimientos listaMovimientos = getListaMovimientosBuyer(cifBuyer);
            listaMovimientos.anadirMovimiento(movimiento);
            this.movimientosBuyer.put(cifBuyer, listaMovimientos);
        } else {
            ListaMovimientos listaMovimientos = new ListaMovimientos(cifBuyer);
            listaMovimientos.anadirMovimiento(movimiento);
            this.movimientosBuyer.put(cifBuyer, listaMovimientos);
        }
    }

    /**
     *
     * @param cif
     * @return Devuelve una lista de movimientos de un vendedor
     */
    public ListaMovimientos getListaMovimientoSeller(String cif) {
        if (movimientosSeller.containsKey(cif))
            return movimientosSeller.get(cif);
        return null;
    }

    /**
     *
     * @param cif
     * @return Devuelve una lista de movimientos de un comprador
     */
    public ListaMovimientos getListaMovimientosBuyer(String cif) {
        if (movimientosBuyer.containsKey(cif))
            return movimientosBuyer.get(cif);
        return null;
    }


    /**
     * Le inicia el credito a un comprador
     * @param buyerCif Comprador a Iniciar Credito
     * @param credito a poner Credito
     */
    public void iniciarCreditoBuyer(String buyerCif, double credito) {

        if (this.isBuyerRegistered(buyerCif)) {
            Buyer buyerAddCredit = this.getBuyer(buyerCif);    // Aqui hay un NULL_POINTER_EXCEPTION
            buyerAddCredit.setSaldo(credito);
            buyers.put(buyerAddCredit.getCif(), buyerAddCredit);
        } else {
            System.err.println(buyerCif + " No existe");
        }
    }

    public void retirarCompra(String buyerCif) {
        Buyer buyer = buyers.get(buyerCif);
        LinkedList<Fish> productoComprados = buyer.getArticulosComprados();
        for (Fish fish : productoComprados) {
            if (fish.getEstadoVenta() != EstadoVenta.ENTREGADO) {
                fish.setEstadoVenta(EstadoVenta.ENTREGADO);
            }
        }
    }


    /**
     * Registra un articulo en la lista de articulos comprados de un comprador
     * @param buyercif
     * @param articulo
     * @param precioFinal
     */
    public void registrarVenta(String buyercif, Fish articulo, double precioFinal) { // TODO: Aqui hay un fallo
        if (!buyers.containsKey(buyercif)) {
            System.err.println("## ERROR ## -> El cif " + buyercif + " No está contenido");
        } else {
            Buyer buyer = buyers.get(buyercif);
            articulo.setEstadoVenta(EstadoVenta.ADJUDICADO);
            articulo.setIdComprador(buyercif);
            articulo.setPrecioFinal(precioFinal);
            buyer.decrementarSaldo(articulo.getPrecioFinal());
            buyer.comprarLote(articulo);
            buyers.put(buyercif, buyer);
            System.out.println("[DATABASE] Se registra una venta del articulo " + articulo.getNombre() + " por valor de "+ precioFinal + " del comprador " + buyercif);
        }
    }


    public void mostrarVendedores() {
        for (Seller seller : sellers.values()) {
            System.out.println("[ " + seller.getCif() + " - " + seller.getNombre() + " ]");
        }
    }


    public void anadirLotes(LinkedList<Fish> listaPescado) {
        lotes.addAll(listaPescado);
    }

    public LinkedList<Fish> getLotes() {
        return lotes;
    }

    /**
     *
     * @param cif
     * @return Devuelve los lotes adjudicados de un comprador
     */
    public LinkedList<Fish> getLotesAdjudicados(String cif) {
        if (!isBuyerRegistered(cif))
            return null;
        return buyers.get(cif).getArticulosComprados();
    }
}
