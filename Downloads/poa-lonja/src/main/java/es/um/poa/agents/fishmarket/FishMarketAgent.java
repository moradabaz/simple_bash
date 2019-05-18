package es.um.poa.agents.fishmarket;

import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.behaviours.DepositoPescadoResp;
import es.um.poa.agents.fishmarket.behaviours.RegistroCompradorResp;
import es.um.poa.agents.fishmarket.behaviours.RegistroVendedorResp;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static jade.lang.acl.MessageTemplate.MatchConversationId;

/**
 * El Agente FishMarket representa la lonja de pescado.
 */
public class FishMarketAgent extends TimePOAAgent {

	private double ingresos = 0;
	private double comisionPorLote = 0.2;

	public void setup() {
		super.setup();
		Object[] args = getArguments();
		if (args != null && args.length == 1) {

			String configFile = (String) args[0];
			FishMarketAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
				MessageTemplate messageTemplate = MatchConversationId("buyer-register");
				addBehaviour(new RegistroCompradorResp(this, messageTemplate));

				MessageTemplate messageTemplateRV = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST), MatchConversationId("seller-register"));
				addBehaviour(new RegistroVendedorResp(this, messageTemplateRV));

				MessageTemplate messageTemplateDP = MessageTemplate.MatchConversationId("deposito-fish");
				addBehaviour(new DepositoPescadoResp(this, messageTemplateDP));
			}

		} else {
			this.getLogger().info("ERROR", "Requiere fichero de cofiguración.");
			doDelete();
		}
	}
	
	private FishMarketAgentConfig initAgentFromConfigFile(String fileName) {
		FishMarketAgentConfig config = null;
		try {
			Yaml yaml = new Yaml();
			InputStream inputStream;
			inputStream = new FileInputStream(fileName);
			config = yaml.load(inputStream);
			getLogger().info("initAgentFromConfigFile", config.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return config;
	}


	private void parseBuyerConfig(String buyerFile) {}
}
