package es.um.poa.agents.buyer.behaviours;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;


public class RetiroCompra  extends AchieveREInitiator{

    private Agent agente;
    private ACLMessage mensaje;

    public RetiroCompra(Agent a, ACLMessage msg) {
        super(a, msg);
        this.agente = a;
        this.mensaje = msg;
    }

    public void handleInform(ACLMessage inform) {
        System.out.println(" >> El mensaje " + inform.getConversationId() + " ha sido notificado correctamente");
    }

    public void handleRefuse(ACLMessage refuse) {
        System.out.println(" ! >> (Retirada Comprador) El mensaje enviado ha sido por rechazado por el agente " + refuse.getSender().getLocalName());
    }

    public void handleFailure(ACLMessage failure) {
        if (agente.getAMS().equals(failure.getSender())) {
            System.out.println(" X: Ha habido un fallo en el envío");
            System.out.println(agente.getAMS());
            System.out.println(failure.getSender());
        } else {
            System.out.println(" X: El agente receptor NO existe");
        }

    }


    public Agent getAgente() {
        return agente;
    }

    public void setAgente(Agent agente) {
        this.agente = agente;
    }

    public ACLMessage getMensaje() {
        return mensaje;
    }

    public void setMensaje(ACLMessage mensaje) {
        this.mensaje = mensaje;
    }

}








