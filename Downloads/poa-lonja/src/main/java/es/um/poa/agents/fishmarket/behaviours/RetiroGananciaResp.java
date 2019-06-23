package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Concepto;
import es.um.poa.Objetos.Movimiento;
import es.um.poa.Objetos.Seller;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.Iterator;

public class RetiroGananciaResp extends Behaviour {


    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();
    private boolean done;

    public RetiroGananciaResp(Agent a, MessageTemplate mt) {
        this.agente = a;
        this.mensaje = mt;
        this.done = false;
    }


    public ACLMessage prepareResponse(ACLMessage request)  {

        try {

            Seller seller = ((Seller) request.getContentObject());
            String cif = seller.getCif();
            String nombre = seller.getNombre();
            System.out.println("Mensaje recibido de Retiro de Ganancia");

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
    public void action() {
        if (((FishMarketAgent) agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_RETIRADA_VENDEDOR) {
                ACLMessage request = agente.receive(mensaje);
                if (request != null) {
                    String idVendedor = request.getSender().getLocalName();
                    if (request.getPerformative() == ACLMessage.REQUEST) {
                        if (!((FishMarketAgent) agente).getGananciasVendedore().isEmpty()) {
                            System.out.println("LET'S GET THE MONEY");
                            Iterator<String> it = ((FishMarketAgent) agente).getGananciasVendedore().keySet().iterator();
                            while (it.hasNext()) {
                                String cifVendedor = it.next();
                                if (idVendedor.equals(cifVendedor)) {
                                    double ganacia = ((FishMarketAgent) agente).getGananciasVendedore().get(cifVendedor);
                                    ACLMessage respone = request.createReply();
                                    respone.setPerformative(ACLMessage.AGREE);
                                    try {
                                        respone.setContentObject(ganacia);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String descripcion = "El vendedor " + cifVendedor + " ha ganadao " +  ganacia + " € ";
                                    Movimiento movimiento = new Movimiento(cifVendedor, Concepto.ADJUDICACION, descripcion);
                                    dataBase.registarMovimientoSeller(cifVendedor, movimiento);
                                    agente.send(respone);
                                }
                            }

                        } else {
                            ACLMessage respone = request.createReply();
                            try {
                                respone.setContentObject(ACLMessage.REFUSE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            agente.send(respone);
                        }

                    }
                }
            }
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
