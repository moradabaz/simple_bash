package es.um.poa.agents.fishmarket;

import es.um.poa.agents.TimePOAAgent;
import es.um.poa.agents.fishmarket.behaviours.*;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static jade.lang.acl.MessageTemplate.MatchConversationId;

/**
 * El Agente FishMarket representa la lonja de pescado. Este agente va a tener:
 * - Un contador de ingresos
 * - Un porcentaje de comision
 * Tambien tendrá una base de datos (@SellerBuyerDB) para almecenar los vendedores y compradores
 *
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

				/**
				 * Mensaje para crear una respuesta la solicitud de registro de un agente comprador
				 */
				MessageTemplate messageTemplate = MatchConversationId("buyer-register");
				// aniadimos el comportamiento del registro de comprrador
				addBehaviour(new RegistroCompradorResp(this, messageTemplate));

				/**
				 * Mensaje para crear una respuesta de solicitud de registro de un comprador
				 */
				MessageTemplate messageTemplateRV = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST), MatchConversationId("seller-register"));
				// Aniadimos un comportamiento de registro de vendedor
				addBehaviour(new RegistroVendedorResp(this, messageTemplateRV));

				// Mensaje para crear una respuesta para el deposito de la lonja por parte del vendedor
				MessageTemplate messageTemplateDP = MessageTemplate.MatchConversationId("deposito-fish");
				// Aniadimos un comportamiento de respuesta a la solicitud de pescado
				addBehaviour(new DepositoPescadoResp(this, messageTemplateDP));

				MessageTemplate messageTemplateAddCredit = MessageTemplate.MatchConversationId("buyer-addCredit");
				addBehaviour(new InicioCreditoResp(this, messageTemplateAddCredit));

				MessageTemplate messageTemplateRetiraCompra= MessageTemplate.MatchConversationId("buyer-Retire");
				addBehaviour(new RetiroCompraResp(this, messageTemplateRetiraCompra));

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
