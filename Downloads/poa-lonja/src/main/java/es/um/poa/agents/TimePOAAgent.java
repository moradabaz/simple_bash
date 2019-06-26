package es.um.poa.agents;

import es.um.poa.agents.clock.SimTimeOntology;
import es.um.poa.utils.OntologyFactory;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

public abstract class TimePOAAgent  extends POAAgent {
	private SimTimeOntology simTime;

	public static final int FASE_REGISTRO = 5;
	public static final int FASE_SUBASTA = 20;
	public static final int FASE_RETIRADA_COMPRADOR = 22;
	public static final int FASE_RETIRADA_VENDEDOR = 25;


	public void setup() {
		super.setup();

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("simulated-time");
		template.addServices(sd);
		Behaviour clockDFSub =  new SubscriptionInitiator(
				this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
					if(dfds.length > 0) {
						AID clockAgent = dfds[0].getName();
						ACLMessage request = new ACLMessage(ACLMessage.SUBSCRIBE);
						request.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
						request.setReplyWith(""+System.currentTimeMillis());
						request.addReceiver(clockAgent);

						myAgent.addBehaviour(new TimerUpdaterBehaviour(getAgent(), request));
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		};

		addBehaviour(clockDFSub);
	}

	private class TimerUpdaterBehaviour extends SubscriptionInitiator {

		public TimerUpdaterBehaviour(Agent a, ACLMessage msg) {
			super(a, msg);
		}

		@Override
		protected void handleInform(ACLMessage inform) {
			String content = inform.getContent();
			SimTimeOntology sto = OntologyFactory.getSimTimeOntologyObject(content);

			((POAAgent)getAgent()).getLogger().info("TimerUpdaterBehaviour", sto.toString());
			simTime = sto;
			getLogger().info("TimerUpdaterBehaviour", simTime.toString());
			checkAgentBehaviours();
			if(simTime.getSimState().equals(SimTimeOntology.END)) {
				ACLMessage cancel = inform.createReply();
				cancel.setPerformative(ACLMessage.CANCEL);
				getAgent().send(cancel);

				getAgent().doDelete();
			}
		}
	}

	public SimTimeOntology getSimTime() {
		return simTime;
	}

	public int getFaseActual() {
		if (simTime.getTime() < FASE_REGISTRO)
			return FASE_REGISTRO;
		else if (simTime.getTime() < FASE_SUBASTA)
			return FASE_SUBASTA;

		else if (simTime.getTime() < FASE_RETIRADA_COMPRADOR)
			return FASE_RETIRADA_COMPRADOR;
		else
			return FASE_RETIRADA_VENDEDOR;
	}

	public abstract void checkAgentBehaviours();


	protected class CheckTimeNotNullBehav extends Behaviour {

		private boolean done;

		public CheckTimeNotNullBehav() {
			done = false;
		}

		@Override
		public void action() {
			if (getSimTime() != null) {
				System.out.println(" >>> EL tiempo no es nulo: " + getSimTime());
				done = true;
			}

		}

		@Override
		public boolean done() {
			return done;
		}
	}
}
