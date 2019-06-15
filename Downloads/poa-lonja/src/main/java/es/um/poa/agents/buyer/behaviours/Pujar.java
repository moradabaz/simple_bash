package es.um.poa.agents.buyer.behaviours;

import es.um.poa.agents.buyer.BuyerAgent;
import es.um.poa.productos.Fish;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

public class Pujar extends ContractNetResponder {

    private Agent agent;
    private MessageTemplate cfp;

    public Pujar(Agent a, MessageTemplate cfp) {
        super(a, cfp);
        this.agent = a;
        this.cfp = cfp;
    }

    public ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

        Fish objetoSubastado = null;
        try {
             objetoSubastado = (Fish) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

        if (objetoSubastado != null) {
            if (((BuyerAgent) agent).estaInterasadoEn(objetoSubastado.getNombre())) {
                double saldo = ((BuyerAgent) agent).getSaldo();
                double precioPropuesta = calcularPrecioPuja(objetoSubastado);
                if (esFavorable(objetoSubastado, precioPropuesta, saldo)) {
                    ACLMessage propose = cfp.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                    propose.setContent(String.valueOf(((BuyerAgent) agent).getPrecioPropuesta()));
                    return propose;
                } else {
                    ACLMessage refuse = cfp.createReply();
                    refuse.setPerformative(ACLMessage.REFUSE);
                    return refuse;
                }
            } else {
                ACLMessage refuse = cfp.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }

        } else {
            ACLMessage failure = cfp.createReply();
            failure.setPerformative(ACLMessage.FAILURE);
            return failure;
        }
    }

    public boolean esFavorable(Fish fishEnSubasta, double propuesta, double saldo) {
       return (fishEnSubasta.getPrecioFinal() <= propuesta && saldo > propuesta);

    }

    public double calcularPrecioPuja(Fish fish) {
        double precio = Math.floor(Math.random() * (fish.getPrecioMinimo() - fish.getPrecioFinal() + 1) + fish.getPrecioFinal());
        return precio;
    }


    public Agent getAgente() {
        return agent;
    }

    public void setAgente(Agent agente) {
        this.agent = agente;
    }

    public MessageTemplate getMsg() {
        return cfp;
    }

    public void setMsg(MessageTemplate msg) {
        this.cfp = msg;
    }

}
