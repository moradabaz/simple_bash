package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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

    public ACLMessage prepareResponse(ACLMessage request)  {
        try {
            Buyer buyer = (Buyer) request.getContentObject();
            String cif = buyer.getCif();
            if (dataBase.checkBuyerByID(cif)) {
                double creditoAnadir = buyer.getSaldo();
                dataBase.iniciarCreditoBuyer(cif, creditoAnadir);
                ACLMessage reply = request.createReply();
                reply.setPerformative(ACLMessage.AGREE);
                System.out.println("Se acepta el deposito de credito del comprador [" + buyer.getCif() + "] cuyo nombre es [" + buyer.getNombre() + "]");
                System.out.println("Saldo: " + buyer.getSaldo());
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
        if (((FishMarketAgent)agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_REGISTRO) {
                ACLMessage request = agente.receive(mensaje);
                if (request != null) {
                    if (request.getPerformative() == ACLMessage.REQUEST) {
                        ACLMessage response = null;
                        response = prepareResponse(request);
                        if (response != null) {
                            agente.send(response);
                        }
                    }
                }
            } else {
                done = true;
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
