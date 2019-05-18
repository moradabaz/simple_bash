package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

public class RegistroVendedorResp extends AchieveREResponder {

    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public RegistroVendedorResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agente = a;
        this.mensaje = mt;
    }

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
