package es.um.poa.agents;

import es.um.poa.utils.AgentLoggerWrapper;
import jade.core.Agent;

public class POAAgent extends Agent {
	private AgentLoggerWrapper logger;
	
	public void setup() {
		this.logger = new AgentLoggerWrapper(this);
	}
	
	public AgentLoggerWrapper getLogger() {
		return this.logger;
	}
	
	public void takeDown() {
		super.takeDown();
		this.logger.close();
	}
}
