package es.um.poa.agents.seller.behaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * La clase DepositoPescado representa el comportamiento ejecutado por el vendedor para enviar
 * una solicitud de deposito de sus lotes en la lonja
 */
public class DepositoPescado extends Behaviour {

    private Agent agente;
    private ACLMessage mensaje;
    private int step;
    private boolean done;

    public DepositoPescado(Agent a, ACLMessage msg) {
        this.agente = a;
        this.mensaje = msg;
        this.step = 0;
        this.done = false;
    }

    public void handleInform(ACLMessage inform) {
        System.out.println("El agente vendedor" + agente.getLocalName() + " ha depositado sus lotes con exito");
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println("El agente " + refuse.getSender().getLocalName() + " ha rechazado la solicitud de deposito de lotes"
                + agente.getLocalName());
    }

    public void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getLocalName()
                    + " failed to perform the requested action: " + agente.getLocalName());
        }
    }

    public Agent getAgente() {
        return agente;
    }

    public void setAgente(Agent agente) {
        this.agente = agente;
    }

    public ACLMessage getMsg() {
        return mensaje;
    }

    public void setMsg(ACLMessage msg) {
        this.mensaje = msg;
    }

    @Override
    public void action() {
         switch (step) {
             case 0:
                 agente.send(mensaje);
                 step++;
                 break;
             case 1:
                 ACLMessage reply = agente.receive();
                 if (reply != null) {
                     switch (reply.getPerformative()) {
                         case ACLMessage.AGREE:
                             handleInform(reply);
                             done = true;
                             break;
                         case ACLMessage.REFUSE:
                             handleFailure(reply);
                             break;
                         case ACLMessage.FAILURE:
                             handleFailure(reply);
                             break;
                         default:
                             System.err.println("NO SE HA ENTENDIDO EL MENSAJE " + reply.getPerformative());;
                     }
                 }
         }
    }

    @Override
    public boolean done() {
        return done;
    }
}
