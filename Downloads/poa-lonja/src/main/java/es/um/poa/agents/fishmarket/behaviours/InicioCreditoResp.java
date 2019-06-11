package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;


/**
 * La clase InicioCreditoResp corresponde con el comportamiento que tiene la lonja cuando
 * recibe una peticion de inicio de credito por parte de un comprador
 * La lonja envia una respuesta afirmativa en caso de haber registrado el credito del comprdor solicitante
 * y una respuesta negativa en caso contrario
 */
public class InicioCreditoResp extends AchieveREResponder {

    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();

    public InicioCreditoResp(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agente = a;
        this.mensaje = mt;
    }

    @Override
    public ACLMessage prepareResponse(ACLMessage request)  {

        try {


            Buyer buyer = (Buyer) request.getContentObject();
            String cif = buyer.getCif();
            double creditoAnadir = buyer.getSaldo();

            System.out.println(" $$$$$ Mensaje recibido de inicio de credito");

            // Se supone que siempre podemos a�adir el credito, mandamos el AGREE
            dataBase.iniciarCreditoBuyer(cif, creditoAnadir);

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
        //Esta funcion es para decir que hemos enviado esto por pantalla
        System.out.println("Se ha anyadido el credito correctamente");

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
