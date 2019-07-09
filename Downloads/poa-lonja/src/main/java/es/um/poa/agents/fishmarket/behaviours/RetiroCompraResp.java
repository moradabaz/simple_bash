package es.um.poa.agents.fishmarket.behaviours;

import es.um.poa.Objetos.Buyer;
import es.um.poa.Objetos.Concepto;
import es.um.poa.Objetos.Movimiento;
import es.um.poa.Objetos.SellerBuyerDB;
import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.FishMarketAgent;
import es.um.poa.productos.Fish;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.Iterator;


/**
 * La clase RetiroCompraResp representa el comportamiento que ejecuta la lonja cuando recibe
 * una peticion del comprador de retirar el lote adquirido en la subasta.
 *
 */
public class RetiroCompraResp extends Behaviour {

    private Agent agente;
    private MessageTemplate mensaje;
    private SellerBuyerDB dataBase = SellerBuyerDB.getInstance();
    private boolean done;

    public RetiroCompraResp(Agent a, MessageTemplate mt) {
        this.agente = a;
        this.mensaje = mt;
        this.done = false;
    }

    /**
     * Dada una peticion que ha sido enviada por el comprador que ha ganado al subasta,
     * se recoge su CIF y su nombre, y se hace una llamada a la base de datos para
     * retirar una compra
     * @param request
     * @return
     */
    public ACLMessage prepareResponse(ACLMessage request)  {

        try {

            Buyer buyer = ((Buyer) request.getContentObject());
            String cif = buyer.getCif();
            String nombre = buyer.getNombre();
            System.out.println("Mensaje recibido de Retiro de Compra");

            /*
             * en la subasta habria que buscar con el cif lo que no se ha retirado y ponerlo
             * a entregado
             * */
            dataBase.retirarCompra(cif);
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

    /*
    @Override
    public void action() {
        if (((FishMarketAgent) agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_RETIRADA_COMPRADOR) {
                ACLMessage request = agente.receive(mensaje);
                if (request != null) {
                    if (request.getPerformative() == ACLMessage.REQUEST) {
                        try {
                                HashMap<Integer, Fish> adjudicaciones = (HashMap<Integer, Fish>) request.getContentObject();
                            if (adjudicaciones != null) {

                                Iterator<Integer> it = adjudicaciones.keySet().iterator();
                                while (it.hasNext()) {
                                    int tiempo = it.next();
                                    Fish fish = adjudicaciones.get(tiempo);
                                    double precio = fish.getPrecioFinal();
                                    Buyer buyer = dataBase.getBuyer(request.getSender().getLocalName());
                                    dataBase.registrarVenta(buyer.getCif(), fish, precio);
                                    ((FishMarketAgent) agente).incrementarIngreso(precio);
                                    String descripcion = "Se adjudica el lote " + fish.toString() + " al comprador " +
                                            buyer.getNombre() + " cuyo CIF es: " + buyer.getCif();
                                    Movimiento movimiento = new Movimiento(buyer.getCif(), Concepto.ADJUDICACION, descripcion);
                                    dataBase.registrarMovimientoBuyer(buyer.getCif(), movimiento);
                                }

                                ACLMessage respone = request.createReply();
                                respone.setPerformative(ACLMessage.AGREE);
                                agente.send(respone);
                                done = true;

                            }
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }*/

    @Override
    public void action() {
        if (((FishMarketAgent) agente).getSimTime() != null) {
            if (((FishMarketAgent) agente).getFaseActual() == TimePOAAgent.FASE_RETIRADA_COMPRADOR) {
                ACLMessage request = agente.receive(mensaje);
                if (request != null) {
                    if (request.getPerformative() == ACLMessage.PROPOSE) {
                        try {
                            HashMap<Integer, Fish> adjudicaciones = (HashMap<Integer, Fish>) request.getContentObject();
                            if (adjudicaciones != null) {

                                Iterator<Integer> it = adjudicaciones.keySet().iterator();
                                while (it.hasNext()) {
                                    int tiempo = it.next();
                                    Fish fish = adjudicaciones.get(tiempo);
                                    double precio = fish.getPrecioFinal();
                                    Buyer buyer = dataBase.getBuyer(request.getSender().getLocalName());
                                    ((FishMarketAgent) agente).incrementarIngreso(precio);
                                    String idVendedor = fish.getIdVendedor();
                                    anadirGananciaVendedor(idVendedor, precio);
                                    String descripcion = "Se adjudica el lote " + fish.toString() + " al comprador " +
                                            buyer.getNombre() + " cuyo CIF es: " + buyer.getCif();
                                    Movimiento movimiento = new Movimiento(buyer.getCif(), Concepto.ADJUDICACION, descripcion);
                                    dataBase.registrarMovimientoBuyer(buyer.getCif(), movimiento);
                                }

                                ACLMessage respone = request.createReply();
                                respone.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                agente.send(respone);
                            } else {
                                ACLMessage respone = request.createReply();
                                respone.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                agente.send(respone);
                            }
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void anadirGananciaVendedor(String idVendedor, double precio) {
        if (((FishMarketAgent) agente).getGananciasVendedore().containsKey(idVendedor)) {
            double ganacia = ((FishMarketAgent) agente).getGananciasVendedore().get(idVendedor);
            ganacia += precio;
            ((FishMarketAgent) agente).anadirVendedorGanancia(idVendedor, ganacia);
        } else {
            ((FishMarketAgent) agente).anadirVendedorGanancia(idVendedor, precio);
        }
    }

    @Override
    public boolean done() {
        return done;
    }
}







