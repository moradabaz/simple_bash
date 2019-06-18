package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Movimiento;
import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import es.um.poa.productos.Fish;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

import java.util.LinkedList;

/**
 * Este es un comportamiento del vendedor que se usa para responder
 * a una solicitud de deposito de pescado por parte del vendedor
 */
public class DepositoPescadoResp extends AchieveREResponder {

    private Agent agent;
    private MessageTemplate mt;
    private SellerBuyerDB database = SellerBuyerDB.getInstance();

    public DepositoPescadoResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agent = a;
        this.mt = mt;
    }

    /**
     * La funcion comprueba que el vendedor existe en la base de datos.
     * En caso de que el vendedor esté registrado, el vendedor registra todos sus lotes y genera
     * una serie de movimientos. Luego, aniade los lotes a la subasta y registra la lista de
     * movimientos en la BBDD. Despues manda una respuesta de aceptacion
     * En caso de que no este registrado en la lonja, manda un mensaje de rechazo.
     *
     * @param request
     * @return
     */
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
                LinkedList<Movimiento> movimientos = registrarLotes(seller);
                for (Fish fish : seller.getListaPescado())
                    ((FishMarketAgent) agent).anadirLoteASubasta(fish);
                database.registrarSeller(seller);
                for (Movimiento movimiento : movimientos)
                    database.registarMovimientoSeller(seller.getCif(), movimiento); /// NULLPOINTEREXCEPTION

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


    public LinkedList<Movimiento> registrarLotes(Seller seller) {
       return seller.registrarLotes();
    }
}
