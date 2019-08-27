package eu.europa.ec.fisheries.uvms.plugins.consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.RegisterServiceResponse;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.UnregisterServiceResponse;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.service.PluginService;

@MessageDriven(mappedName="jms/topic/EventBus", activationConfig =  {
        @ActivationConfigProperty(propertyName = "subscriptionName",          propertyValue = "eu.europa.ec.fisheries.uvms.plugins.rest.movementPLUGIN_RESPONSE"),
        @ActivationConfigProperty(propertyName = "clientId",                  propertyValue = "eu.europa.ec.fisheries.uvms.plugins.rest.movementPLUGIN_RESPONSE"),
        @ActivationConfigProperty(propertyName = "messageSelector",           propertyValue = "ServiceName='eu.europa.ec.fisheries.uvms.plugins.rest.movementPLUGIN_RESPONSE'"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability",    propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "destination",               propertyValue = "EventBus"),
        @ActivationConfigProperty(propertyName = "destinationType",           propertyValue = "javax.jms.Topic")
})
public class PluginAckEventBusListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(PluginAckEventBusListener.class);

    @EJB
    StartupBean startupService;

    @EJB
    PluginService pluginService;

    @Override
    public void onMessage(Message inMessage) {

        LOG.info("Eventbus listener for flux-vesselposition-rest at selector: {} got a message", startupService.getPluginResponseSubscriptionName());

        TextMessage textMessage = (TextMessage) inMessage;

        try {

            ExchangeRegistryBaseRequest request = tryConsumeRegistryBaseRequest(textMessage);

            if (request == null) {
                handlePluginFault(textMessage);
            } else {
                switch (request.getMethod()) {
                    case REGISTER_SERVICE:
                        RegisterServiceResponse registerResponse = JAXBMarshaller.unmarshallTextMessage(textMessage, RegisterServiceResponse.class);
                        startupService.setWaitingForResponse(Boolean.FALSE);
                        switch (registerResponse.getAck().getType()) {
                            case OK:
                                LOG.info("Register OK");
                                startupService.setIsRegistered(Boolean.TRUE);
                                break;
                            case NOK:
                                LOG.info("Register NOK: " + registerResponse.getAck().getMessage());
                                startupService.setIsRegistered(Boolean.FALSE);
                                break;
                            default:
                                LOG.error("[ Type not supperted: ]" + request.getMethod());
                        }
                        break;
                    case UNREGISTER_SERVICE:
                        UnregisterServiceResponse unregisterResponse = JAXBMarshaller.unmarshallTextMessage(textMessage, UnregisterServiceResponse.class);
                        switch (unregisterResponse.getAck().getType()) {
                            case OK:
                                LOG.info("Unregister OK");
                                break;
                            case NOK:
                                LOG.info("Unregister NOK");
                                break;
                            default:
                                LOG.error("[ Ack type not supported ] ");
                                break;
                        }
                        break;
                    default:
                        LOG.error("Not supported method");
                        break;
                }
            }
        } catch (NullPointerException e) {
            LOG.error("[ Error when receiving message in flux-vesselposition-rest ]", e);
        }
    }

    private void handlePluginFault(TextMessage fault) {
        try {
            LOG.error(
                    startupService.getPluginResponseSubscriptionName() + " received fault : " + fault.getText() + " : " );
        } catch (JMSException e) {
            LOG.error("Could not get text from incoming message in plugin");
        }
    }

    private ExchangeRegistryBaseRequest tryConsumeRegistryBaseRequest(TextMessage textMessage) {
        try {
            return JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeRegistryBaseRequest.class);
        } catch (Exception e) {
            return null;
        }
    }
}
