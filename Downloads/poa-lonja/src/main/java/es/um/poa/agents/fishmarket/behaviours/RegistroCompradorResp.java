package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

public class RegistroCompradorResp extends AchieveREResponder {


    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();

    public RegistroCompradorResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agente = a;
        this.mensaje = mt;
    }

    @Override
    public ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {

        try {

            System.out.println("Nos han llegao las perrillas " +   request.getContentObject().toString());
            System.out.println(">>>>>>> Estamos preparando la respuesta " + request.getContentObject());

            Buyer buyer = ((Buyer) request.getContentObject());

            String cif = buyer.getCif();
            String nombre = buyer.getNombre();
            dataBase.registrarBuyer(buyer);
            System.out.println("Mensaje recibido √");
            System.out.println("Agente tipo " + buyer.getClass().getName() + " { " + "cif: " + cif + " | " + " nombre: " + nombre + " }");

            // RESPUESTA
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            return reply;

        } catch (UnreadableException e) {
            e.printStackTrace();
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            return reply;
        }

    }

    @Override
    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {

        System.out.println(
                "Agente " + agente.getLocalName() + ": completado con exito: " + request.getSender().getLocalName());

        ACLMessage inform = request.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        return inform;
    }

    public Agent getAgente() {
        return agente;
    }

    public void setAgente(Agent agente) {
        this.agente = agente;
    }

    public MessageTemplate getMsg() {
        return mensaje;
    }

    public void setMsg(MessageTemplate msg) {
        this.mensaje = msg;
    }
}
