package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import static jade.lang.acl.MessageTemplate.MatchConversationId;


/**
 * La clase RegistroVendedorResp representa el comportamiento que ejecuta la lonja cuando recibe una
 * peticion de registro por parte de un vendedor.
 */
public class RegistroVendedorResp extends Behaviour {

    private Agent agente;
    private MessageTemplate mensaje;
    private int step;
    private boolean done;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public RegistroVendedorResp(Agent a, MessageTemplate mt) {
        this.agente = a;
        this.mensaje = MatchConversationId("seller-register");
        this.step = 0;
        this.done = false;
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
        try {
            Seller seller = (Seller) request.getContentObject();        // Recoje el objeto Seller (ERROR DE CAST: le llega la lista de pescado)
            if (!database.checkSellerByID(seller.getCif())) {           // Comprueba que NO si esta en la BBDD para registrarlo
                database.actualizarSeller(seller);
                ACLMessage agreeReply = request.createReply();
                agreeReply.setPerformative(ACLMessage.AGREE);
                System.out.println("La peticion del agente vendedor "+ seller.getNombre() + ", cuyo CIF es " + seller.getCif() + " HA SIDO ACEPTADA");
                return agreeReply;
            } else {
                ACLMessage refuseReply = request.createReply();
                refuseReply.setPerformative(ACLMessage.REFUSE);
                return refuseReply;
            }
        } catch (UnreadableException e) {
            ACLMessage refuseReply = request.createReply();
            refuseReply.setPerformative(ACLMessage.FAILURE);
            return refuseReply;
        }
    }

    @Override
    public void action() {
        if (((FishMarketAgent)agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_REGISTRO) {
                ACLMessage request = ((FishMarketAgent) agente).receive(mensaje);
                if (request != null) {
                    if (request.getPerformative() == ACLMessage.REQUEST) {
                        ACLMessage response = null;
                        response = prepareResponse(request);
                        if (response != null) {
                            agente.send(response);
                        }
                        step++;
                    }
                }
            } else {
                done = false;
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
