package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

public class DepositoPescadoResp extends AchieveREResponder {

    private Agent agent;
    private MessageTemplate mt;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public DepositoPescadoResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agent = a;
        this.mt = mt;
    }

    public ACLMessage prepareResponse(ACLMessage request) {
        System.out.println("Mensaje recibido √");

        try {
            System.out.println("Solicitud de deposito recibida : " +   request.getContentObject().toString());
        } catch (UnreadableException e) {
            System.out.println("Solicitud de deposito NO recibida correctament : ");
        }


        try {

            System.out.println(" " +   request.getContentObject().toString());
            System.out.println(">>>>>>> Estamos preparando la respuesta " + request.getContentObject());

            Seller seller = (Seller) request.getContentObject();

            if (database.checkSellerByID(seller.getCif())) {
                database.registrarSeller(seller);

                // RESPUESTA
                ACLMessage agreeReply = request.createReply();
                agreeReply.setPerformative(ACLMessage.AGREE);
                return agreeReply;

            } else {

                System.out.println("No existe ningun vendedor cuyo en la base de datos para depositar");
                ACLMessage refuseReply = request.createReply();
                refuseReply.setPerformative(ACLMessage.REFUSE);
                return refuseReply;
            }

        } catch (UnreadableException e) {
            e.printStackTrace();
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            return reply;
        }
    }

    @Override
    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
        ACLMessage informMessage = request.createReply();
        informMessage.setPerformative(ACLMessage.INFORM);
        return informMessage;
    }
}
