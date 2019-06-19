package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * La clase InicioCreditoResp corresponde con el comportamiento que tiene la lonja cuando
 * recibe una peticion de inicio de credito por parte de un comprador
 * La lonja envia una respuesta afirmativa en caso de haber registrado el credito del comprdor solicitante
 * y una respuesta negativa en caso contrario
 */
public class InicioCreditoResp extends Behaviour {

    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();
    private boolean done;
    private int step;

    public InicioCreditoResp(Agent a, MessageTemplate mt) {
        this.agente = a;
        this.mensaje = mt;
        this.done = false;
        this.step = 0;
    }

    //@Override
    public ACLMessage prepareResponse(ACLMessage request)  {

        try {

            Buyer buyer = (Buyer) request.getContentObject();   // AQUI HAY UN CASTING
            String cif = buyer.getCif();
            if (dataBase.checkBuyerByID(cif)) {
                double creditoAnadir = buyer.getSaldo();

                System.out.println(" $$$$$ Mensaje recibido de inicio de credito");

                // Se supone que siempre podemos a�adir el credito, mandamos el AGREE
                dataBase.iniciarCreditoBuyer(cif, creditoAnadir);

                ACLMessage reply = request.createReply();
                reply.setPerformative(ACLMessage.AGREE);
                return reply;
            } else {
                ACLMessage reply = request.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                return reply;
            }

        } catch (UnreadableException e) {
            e.printStackTrace();
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            return reply;
        }

    }

    //@Override
    public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        //Esta funcion es para decir que hemos enviado esto por pantalla
        System.out.println("Se ha anyadido el credito correctamente");
        System.out.println("[TIEMPO_RESPUESTA] ++ " + ((FishMarketAgent) agente).getSimTime());

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

    @Override
    public void action() {
        ACLMessage request = agente.receive(mensaje);
        if (request != null) {

            if (request.getPerformative() == ACLMessage.REQUEST) {
                ACLMessage response = null;
                response = prepareResponse(request);
                if (response != null) {
                    agente.send(response);
                }
                step = 1;
                done = true;
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
