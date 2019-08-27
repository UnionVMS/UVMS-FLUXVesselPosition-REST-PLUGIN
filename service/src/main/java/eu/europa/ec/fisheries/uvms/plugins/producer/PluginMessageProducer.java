package eu.europa.ec.fisheries.uvms.plugins.producer;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;

@Stateless
@LocalBean
public class PluginMessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(PluginMessageProducer.class);

    @Resource(mappedName = "java:/" + ExchangeModelConstants.EXCHANGE_MESSAGE_IN_QUEUE)
    private Queue exchangeQueue;

    @Resource(mappedName = "java:/" + ExchangeModelConstants.PLUGIN_EVENTBUS)
    private Topic eventBus;

    @Resource(lookup = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    public void sendResponseMessage(String text, TextMessage requestMessage) throws JMSException {
        try (Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false, 1);
                MessageProducer producer = session.createProducer(requestMessage.getJMSReplyTo());) {
            TextMessage message = session.createTextMessage();
            message.setJMSDestination(requestMessage.getJMSReplyTo());
            message.setJMSCorrelationID(requestMessage.getJMSMessageID());
            message.setText(text);

            producer.send(message);
        }
    }

    public String sendExchangeMessage(String text, String function) throws JMSException {
        try (Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false, 1);
                MessageProducer producer = session.createProducer(exchangeQueue);) {
            TextMessage message = session.createTextMessage();
            message.setText(text);
            message.setStringProperty("FUNCTION", function);

            producer.send(message);

            return message.getJMSMessageID();
        }
    }

    public String sendEventBusMessage(String text, String serviceName, String function) throws JMSException {
        try (Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false, 1);
                MessageProducer producer = session.createProducer(eventBus);) {
            TextMessage message = session.createTextMessage();
            message.setText(text);
            message.setStringProperty(ExchangeModelConstants.SERVICE_NAME, serviceName);
            message.setStringProperty("FUNCTION", function);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(message);
            return message.getJMSMessageID();
        } catch (JMSException e) {
            LOG.error(e.toString(), e);
            throw e;
        }
    }
}