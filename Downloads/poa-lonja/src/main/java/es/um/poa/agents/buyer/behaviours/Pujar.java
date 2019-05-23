package es.um.poa.agents.buyer.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class Pujar extends ContractNetResponder {

    private Agent agent;
    private MessageTemplate cfp;

    public Pujar(Agent a, MessageTemplate cfp) {
        super(a, cfp);
        this.agent = a;
        this.cfp = cfp;
    }

    public ACLMessage prepareResponse(ACLMessage cfp) {


      //  if (((BuyerAgent) agent).is)
        return null;
    }

}
