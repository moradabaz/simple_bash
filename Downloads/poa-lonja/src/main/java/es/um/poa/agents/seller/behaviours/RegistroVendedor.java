package es.um.poa.agents.seller.behaviours;

import es.um.poa.agents.seller.SellerAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;


/**
 * La clase RegistroVendedor representa el comportamiento ejecutado por el vendedor para enviar
 * una solicitud de registro.
 */
public class RegistroVendedor extends Behaviour {

    private Agent agente;
    private ACLMessage mensaje;
    private int step;
    private boolean done;

    public RegistroVendedor(Agent a, ACLMessage msg) {
        this.agente = a;
        this.mensaje = msg;
        this.step = 0;
        this.done = false;
    }

    public void handleInform(ACLMessage inform) {
        System.out.println("El agente vendedor" + agente.getLocalName() + " se ha registrado correctamente");
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println("El agente " + refuse.getSender().getLocalName() + " ha rechazado la solicitud del agente vendedor"
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
        if (((SellerAgent) agente).getSimTime() != null) {
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
                                System.err.println("NO SE HA ENTENDIDO EL MENSAJE " + reply.getPerformative());
                        }
                        done = true;
                    }
                    break;
                default:
                    System.out.println("NO SE HA ENTENDIDO EL MENSAJE DE REGISTRO VENDEDOR ");
                    break;
            }
        }

    }

    @Override
    public boolean done() {
        return done;
    }
}
