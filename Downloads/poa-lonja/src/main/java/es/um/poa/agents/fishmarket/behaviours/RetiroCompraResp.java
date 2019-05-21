package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

public class RetiroCompraResp extends AchieveREResponder {

    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();

    public RetiroCompraResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agente = a;
        this.mensaje = mt;
    }

    @Override
    public ACLMessage prepareResponse(ACLMessage request)  {

        try {

            Buyer buyer = ((Buyer) request.getContentObject());

            String cif = buyer.getCif();
            String nombre = buyer.getNombre();

            System.out.println("Mensaje recibido de Retiro de Compra");

            /*
             * en la subasta habria que buscar con el cif lo que no se ha retirado y ponerlo
             * a entregado
             * */

            dataBase.retirarCompra(cif);

            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);
            return reply;


        } catch (UnreadableException e) {

            e.printStackTrace();
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            return reply;
        }

    }

    @Override
    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        //Esta funcion es para decir que hemos enviado esto por pantalla
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







