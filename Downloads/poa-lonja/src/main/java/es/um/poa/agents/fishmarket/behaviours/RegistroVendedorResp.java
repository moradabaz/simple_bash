package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;


/**
 * La clase RegistroVendedorResp representa el comportamiento que ejecuta la lonja cuando recibe una
 * peticion de registro por parte de un vendedor.
 */
public class RegistroVendedorResp extends AchieveREResponder {

    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public RegistroVendedorResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agente = a;
        this.mensaje = mt;
    }

    /**
     * Crea una respuesta en funcion del cumplimiento de los requisitos para
     * registra un vendedor.
     * Recibe como argumento un mensaje de peticion que contiene el objeto Seller.
     * Si el objeto Seller no esta registrado, entonces se registra y se envia un mensaje
     * de aceptacion. En otro caso, se envia un mensaje de denegacion
     * @param request
     * @return Devuelve una respuesta adecuada
     */
    public ACLMessage prepareResponse(ACLMessage request) {
        System.out.println("Agente " + agente.getLocalName() + ": Iniciador: " + request.getSender().getLocalName());

        try {
            Seller seller = (Seller) request.getContentObject();        // Recoje el objeto Seller (ERROR DE CAST: le llega la lista de pescado)
            if (!database.checkSellerByID(seller.getCif())) {           // Comprueba que NO si esta en la BBDD para registrarlo
                database.registrarSeller(seller);
                ACLMessage agreeReply = request.createReply();
                agreeReply.setPerformative(ACLMessage.AGREE);
                return agreeReply;
            } else {
                ACLMessage refuseReply = request.createReply();
                refuseReply.setPerformative(ACLMessage.REFUSE);
                return refuseReply;
            }
        } catch (UnreadableException e) {
            //e.printStackTrace();
            ACLMessage refuseReply = request.createReply();
            refuseReply.setPerformative(ACLMessage.FAILURE);
            return refuseReply;
        }
    }

    @Override
    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        ACLMessage informMessage = request.createReply();
        informMessage.setPerformative(ACLMessage.INFORM);
        return informMessage;
    }
}
