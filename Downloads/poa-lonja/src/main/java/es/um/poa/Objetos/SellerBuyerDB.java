package es.um.poa.Objetos;

import java.util.HashMap;
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
    }

    /**
     * Registra un Vendedor en el mapa de compradores
     * @param seller Vendedor a registrar
     */
    public void registrarSeller(Seller seller) {
        sellers.put(seller.getIdentificador(), seller);
    }


    /**
     * Registra un comprador en el mapa de compradores
     * @param buyer Comprador a registrar
     */
    public void registrarBuyer(Buyer buyer){
        buyers.put(buyer.getIdentificador(), buyer);
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

    public void removeSeller(Seller seller) {
        sellers.remove(seller.getIdentificador(), seller);
    }

    public void modificarSeller(Seller seller) {
        if (checkBuyerByID(seller.getIdentificador())) {
            sellers.remove(seller.getIdentificador());
            sellers.put(seller.getIdentificador(), seller);
        }
    }

    public Seller getSeller(String sellerID) {
        return sellers.getOrDefault(sellerID, null);
    }


    public boolean existeBuyer(Buyer buyer) {
        return buyers.containsValue(buyer);
    }

    public boolean checkBuyerByID(String buyerID) {
        return buyers.containsKey(buyerID);
    }

    public void removerBuyerByID(String buyerID) {
        buyers.remove(buyerID);
    }

    public void modificarBuyer(Buyer buyer) {
       if (buyers.containsKey(buyer.getIdentificador())) {
           buyers.remove(buyer.getIdentificador());
           buyers.put(buyer.getIdentificador(), buyer);
       }
    }

    public Buyer getBuyer(String buyerID) {
        return buyers.getOrDefault(buyerID, null);
    }

    public Set<String> getBuyerIDs() {
        return buyers.keySet();
    }

    public Set<String> getSellerIDs() {
        return sellers.keySet();
    }



}
