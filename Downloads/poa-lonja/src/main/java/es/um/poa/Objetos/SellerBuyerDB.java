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

    private static final String PREFIX_BUYER = "BU";
    private static final String PREFIX_SELLER = "SE";

    private static SellerBuyerDB ourInstance;
    private Map<String, Seller> sellers;
    private Map<String, Buyer> buyers;
    private Map<String, ListaMovimientos> movimientosSeller;
    private Map<String, ListaMovimientos> movimientosBuyer;



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
    }

    /**
     * Registra un Vendedor en el mapa de compradores
     * @param seller Vendedor a registrar
     */
    public void registrarSeller(Seller seller) {
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
     * Comprueba si un objeto vendedor esta registrado en el mapa
     * @param seller Objeto a verificar en la BBDD
     * @return Retorna un valor True si existe dicho objeto en el mapa de vendedores
     */
    public boolean existeSeller(Seller seller) {
        return sellers.containsValue(seller);
    }

    /**
     * Comprueba que existe un vendedor dado su identificador
     * @param sellerID
     * @return
     */
    public boolean checkSellerByID(String sellerID) {
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
     *
     * @param seller
     */
    public void removeSeller(Seller seller) {
        sellers.remove(seller.getCif(), seller);
    }

    /**
     *
     * @param seller
     */
    public void modificarSeller(Seller seller) {
        if (checkBuyerByID(seller.getCif())) {
            sellers.remove(seller.getCif());
            sellers.put(seller.getCif(), seller);
        }
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
     *
     * @param buyer
     * @return
     */
    public boolean existeBuyer(Buyer buyer) {
        return buyers.containsValue(buyer);
    }

    /**
     *
     * @param buyerID
     * @return
     */
    public boolean checkBuyerByID(String buyerID) {
        return buyers.containsKey(buyerID);
    }

    /**
     *
     * @param buyerID
     */
    public void removerBuyerByID(String buyerID) {
        buyers.remove(buyerID);
    }

    /**
     *
     * @param buyer
     */
    public void modificarBuyer(Buyer buyer) {
       if (buyers.containsKey(buyer.getCif())) {
           buyers.remove(buyer.getCif());
           buyers.put(buyer.getCif(), buyer);
       }
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
    public Set<String> getBuyerIDs() {
        return buyers.keySet();
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
    public LinkedList<Seller> getAllSellers() {
        return new LinkedList<>(sellers.values());
    }

    /**
     *
     * @return
     */
    public LinkedList<Buyer> getAllBuyers() {
        return new LinkedList<>(buyers.values());
    }

    /**
     *
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
     * @return
     */
    public ListaMovimientos getListaMovimientoSeller(String cif) {
        if (movimientosSeller.containsKey(cif))
            return movimientosSeller.get(cif);
        return null;
    }

    public ListaMovimientos getListaMovimientosBuyer(String cif) {
        if (movimientosBuyer.containsKey(cif))
            return movimientosBuyer.get(cif);
        return null;
    }


    public LinkedList<Movimiento> getSellerMovimientos(String sellerCif) {
        if (movimientosSeller.containsKey(sellerCif)) {
            return movimientosSeller.get(sellerCif).getMovimientos();
        }
        return null;
    }

    public LinkedList<Movimiento> getBuyerMovimientos(String buyerCif) {
        if (movimientosSeller.containsKey(buyerCif)) {
            return movimientosSeller.get(buyerCif).getMovimientos();
        }
        return null;
    }

    /**
     * Le inicia el credito a un comprador
     * @param buyerCif Comprador a Iniciar Credito
     * @param credito a poner Credito
     */
    public void iniciarCreditoBuyer(String buyerCif, double credito) {
        Buyer buyerAddCredit = buyers.get(buyerCif);    // Aqui hay un NULL_POINTER_EXCEPTION
        buyerAddCredit.setSaldo(credito);
        buyers.put(buyerAddCredit.getCif(), buyerAddCredit);
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


    public void registrarVenta(String buyercif, Fish articulo, double precioFinal) {
        if (!buyers.containsKey(buyercif)) {
            System.err.println("## ERROR ## -> El cif" + buyercif + "No está contenido");
        } else {
            Buyer buyer = buyers.get(buyercif);
            articulo.setEstadoVenta(EstadoVenta.ADJUDICADO);
            articulo.setIdComprador(buyercif);
            articulo.setPrecioFinal(precioFinal);
            buyer.comprarLote(articulo);
            buyers.put(buyercif, buyer);

        }
    }
}
