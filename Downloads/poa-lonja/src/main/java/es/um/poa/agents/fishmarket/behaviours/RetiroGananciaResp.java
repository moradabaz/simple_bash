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

    /**
     * Este metodo se utiliza para crear un mensaje de respuesta a la solicitud de retiro de dinero
     * por parte de un vendedor.
     * Lo que hace basicamente este metodo es recorrer la lista de vendedores y ganancias.
     * Se actualizan los valores y se envia una performativa AGREE.
     * @param request
     * @return
     */
    public ACLMessage prepareResponse(ACLMessage request)  {
        if (request != null) {
            String idVendedor = request.getSender().getLocalName();
            if (request.getPerformative() == ACLMessage.REQUEST) {
                if (!((FishMarketAgent) agente).getGananciasVendedore().isEmpty()) {
                    Iterator<String> it = ((FishMarketAgent) agente).getGananciasVendedore().keySet().iterator();
                    while (it.hasNext()) {
                        String cifVendedor = it.next();
                        if (idVendedor.equals(cifVendedor)) {
                            double ganacia = ((FishMarketAgent) agente).getGananciasVendedore().get(cifVendedor);
                            double comisionLonja = ganacia * FishMarketAgent.COMISION_LOTE;
                            Seller vendedor = dataBase.getSeller(cifVendedor);
                            ganacia -= comisionLonja;
                            vendedor.incrementarGanancia(ganacia);
                            dataBase.actualizarSeller(vendedor);
                            System.out.println("[FISH_MARKET] La lonja obtiene actualmente " + comisionLonja + " euros");

                            ACLMessage respone = request.createReply();
                            respone.setPerformative(ACLMessage.AGREE);
                            try {
                                respone.setContentObject(ganacia);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String descripcion = "El vendedor " + cifVendedor + " ha ganado " +  ganacia + " euros ";
                            Movimiento movimiento = new Movimiento(cifVendedor, ((FishMarketAgent) agente).getSimTime().getTime(), Concepto.GANANCIA, descripcion);
                            dataBase.registarMovimientoSeller(cifVendedor, movimiento);
                            return respone;
                        }
                    }


                } else {
                    ACLMessage respone = request.createReply();
                    respone.setPerformative(ACLMessage.REFUSE);
                    return respone;
                }

            }
        }
        return null;
    }


    @Override
    public void action() {
        if (((FishMarketAgent) agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_RETIRADA_VENDEDOR) {
                ACLMessage request = agente.receive(mensaje);
                ACLMessage response = prepareResponse(request);
                if (response != null) {
                    agente.send(response);
                }
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}
