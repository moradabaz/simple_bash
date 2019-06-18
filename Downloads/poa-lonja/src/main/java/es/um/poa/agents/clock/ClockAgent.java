package es.um.poa.agents.clock;

import es.um.poa.agents.POAAgent;
import es.um.poa.utils.OntologyFactory;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Agente que controla la tiempo de la simulación
 * 
 * @author pablo
 *
 */
public class ClockAgent extends POAAgent {
	
	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 3) {
			int unitTimeMillis = (Integer) args[0];
			int numUnitDay = (Integer) args[1];
			int numSimDays = (Integer) args[2];
			this.getLogger().info("setup()", "setup (" +
					"unitTimeMillis=" + unitTimeMillis+", "+
					"numUnitDay=" + numUnitDay+", "+
					"numSimDays=" + numSimDays);
			
			ClockTickerBehaviour clockBehaviour = new ClockTickerBehaviour(this, unitTimeMillis, numUnitDay, numSimDays);
			addBehaviour(clockBehaviour);
			
			MessageTemplate mt = SubscriptionResponder.createMessageTemplate(ACLMessage.SUBSCRIBE);
			addBehaviour(new SubscriptionResponder(this, mt, clockBehaviour));

			// Registrar servicio de medición del tiempo en el DF
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("simulated-time");
			sd.setName("Lonja-Simulated-Time");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			} catch(FIPAException fe) {
				fe.printStackTrace();
			}
		} else {
			System.out.println("Son necesarios 3 argumentos (<unitTimeMillis>,<numUnitDay>,<numSimDays>)");
			doDelete();
		}
	}
	
	@Override
	public void takeDown() {
		super.takeDown();
		try {
			DFService.deregister(this);
		} catch(FIPAException fe) {
			fe.printStackTrace();
		}
		super.takeDown();
	}
	
	/**
	 * Comportamiento que simula el paso del tiempo y notifica a los agentes que se hayan subscrito
	 * 
	 * @author pablo
	 *
	 */
	private class ClockTickerBehaviour extends TickerBehaviour implements SubscriptionManager {
		private int unitTimeMillis; // Número de milisegundos que forman una unidad de tiempo
		private int numUnitDay; // Número de unidades que forman un día
		private int numSimDays; // Número de días que durará la simulación
		
		private int time = 0; // contador de unidades de tiempo
		private int day = 0; // contador de días
		
		List<Subscription> subs = new ArrayList<Subscription>();
		
		public ClockTickerBehaviour(Agent agent, int unitTimeMillis, int numUnitDay, int numSimDays) {
			super(agent, unitTimeMillis);
			this.unitTimeMillis = unitTimeMillis;
			this.numUnitDay = numUnitDay;
			this.numSimDays = numSimDays;
		}
		
		@Override
		protected void onTick() {
			time += 1;
			if (numUnitDay <= time) {
				// Otro día
				time = 0;
				day += 1;
			}
			
			if(isSimEnd()) {
				// Notificar fin simulación
				((POAAgent)this.getAgent()).getLogger().info("ClockTickerBehaviour","Fin Simulaci?n!");
				// Mandar notificaciones de fin de la simulación
				notifySubsciptors(true);
			} else {
				// notificar agentes subscritos día y unidad de tiempo
				((POAAgent)this.getAgent()).getLogger().info("ClockTickerBehaviour","day="+day+", time="+time);
				// TODO Actualizar GUI del reloj vitual?
				notifySubsciptors(false);
			}
		}
		
		public boolean isSimEnd() {
			return day >= numSimDays;
		}
		
		private void notifySubsciptors(boolean end) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			SimTimeOntology obj = new SimTimeOntology(day, time);
			if(end) {
				obj.setSimState(SimTimeOntology.END);
			}
			String content = OntologyFactory.getSimTimeOntologyJSON(obj);
			msg.setContent(content);
			for(Subscription subscription: subs) {
				subscription.notify(msg);
			}
		}

		@Override
		public boolean deregister(Subscription subscription) throws FailureException {
			synchronized (subs) {
				((POAAgent)this.getAgent()).getLogger().info("ClockTickerBehaviour","deregister("+subscription.getMessage().getSender().getLocalName()+")");
				subs.remove(subscription);
				
				if(subs.isEmpty() && isSimEnd()) {
					((POAAgent)this.getAgent()).getLogger().info("deregister()", "Todos los subscriptiores se han ido -> doDelete()");
					getAgent().doDelete();
				}
			}
			return true;
		}

		@Override
		public boolean register(Subscription subscription) throws RefuseException, NotUnderstoodException {
			synchronized (subs) {
				((POAAgent)this.getAgent()).getLogger().info("ClockTickerBehaviour","register("+subscription.getMessage().getSender().getLocalName()+")");
				subs.add(subscription);
			}
			return true;
		}
	}
}
