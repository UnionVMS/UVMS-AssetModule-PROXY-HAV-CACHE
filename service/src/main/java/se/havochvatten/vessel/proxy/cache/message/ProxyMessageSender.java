/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package se.havochvatten.vessel.proxy.cache.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.havochvatten.vessel.proxy.cache.constant.Constants;
import se.havochvatten.vessel.proxy.cache.exception.ProxyException;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.*;

@LocalBean
@Stateless
public class ProxyMessageSender {
    
    final static Logger LOG = LoggerFactory.getLogger(ProxyMessageSender.class);
    
    @Resource(lookup = Constants.CONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;

    /**
     *
     * Send a "fire and forget" message to a recipient
     *
     * @param toQueue The destinsation of the response
     * @param responseQueue
     * @param textMessag The actual message as a String representation of an XML
     * @param deliveryMode The delivery mode to use
     * @param defultPriority The priority for this message
     * @param timeToLive The message's lifetime (in milliseconds)
     * @return
     * @throws se.havochvatten.vessel.proxy.cache.exception.ProxyException
     */
    public String sendMessage(Destination toQueue, Destination responseQueue, String textMessag, int deliveryMode, int defultPriority, long timeToLive) throws ProxyException {
        return sendMessage(toQueue, responseQueue, textMessag, null, deliveryMode, defultPriority, timeToLive);
    }

    /**
     * Sends a response message to a reciever. The corralationId is the
     * JMSMessage id provided in the message this metod responds to.
     *
     * @param responseQueue The destinsation of the response
     * @param textMessage The actual message as a String representation of an
     * XML
     * @param correlationId The correlationId to set on the message that is
     * returned
     * @return The JMSMessage id of the sent message
     * @throws se.havochvatten.vessel.proxy.cache.exception.ProxyException
     */
    public String sendMessage(Destination responseQueue, String textMessage, String correlationId) throws ProxyException {
        return sendMessage(responseQueue, null, textMessage, correlationId, null, null, null);
    }

    /**
     *
     * Sends a JS message to a recipient and sets teh expected reponse queue
     *
     * @param toQueue The destinsation of the message
     * @param replyQueue The destination that shis message should respond to
     * when arriving at the toQueue
     * @param textMessage The actual message as a String representation of an
     * XML
     * @return The JMSMessage id of the sent message
     * @throws se.havochvatten.vessel.proxy.cache.exception.ProxyException
     */
    public String sendMessage(Destination toQueue, Destination replyQueue, String textMessage) throws ProxyException {
        return sendMessage(toQueue, replyQueue, textMessage, null, null, null, null);
    }

    /**
     *
     * Sends a message to a JMS destination
     *
     * @param toQueue The destinsation of the message
     * @param replyQueue The destination that shis message should respond to
     * when arriving at the toQueue
     * @param textMessage The actual message as a String representation of an
     * XML
     * @param correlationId The correlationId to set on the message that is
     * returned
     * @param deliveryMode The delivery mode to use
     * @param defaultPriority The priority for this message
     * @param timetoLive The message's lifetime (in milliseconds)
     * @return The JMSMessage id of the sent message
     * @throws ProxyException
     */
    private String sendMessage(Destination toQueue, Destination replyQueue, String textMessage, String correlationId, Integer deliveryMode, Integer defaultPriority, Long timetoLive) throws ProxyException {

        Connection connection = null;
        Session session = null;
        try {
            
            LOG.info("[ Sending message to recipient on queue ] {}", toQueue);
            
            connection = createConnection();
            session = getSession(connection);
            
            TextMessage message = session.createTextMessage();
            message.setText(textMessage);
            message.setJMSReplyTo(replyQueue);
            message.setJMSDestination(toQueue);
            message.setJMSCorrelationID(correlationId);
            message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);

            if (deliveryMode != null && defaultPriority != null && timetoLive != null) {
                session.createProducer(toQueue).send(message, deliveryMode, defaultPriority, timetoLive);
            } else {
                session.createProducer(toQueue).send(message);
            }

            return message.getJMSMessageID();
        } catch (JMSException ex) {
            LOG.error("Error when sending message JMS queue", ex);
            throw new ProxyException("Error when sending message JMS queue");
        }finally {
            try {
                if(session !=null){
                    session.close();
                }
                if (connection != null) {
                    connection.stop();
                    connection.close();
                }
            } catch (JMSException e) {
                LOG.error("Error when closing JMS queue", e);
                throw new ProxyException("Error when closing JMS queue");
            }

        }
    }

    /**
     * Gets the session that will be used to send a JMS message
     *
     * @param connection The created connection that will be used for the
     * session
     * @return The newly created session
     * @throws JMSException
     */
    protected Session getSession(Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        return session;
    }

    /**
     * Creates a connection from the connection factory provided by the
     * application server.
     *
     * @return The newly created connection
     * @throws JMSException
     */
    protected Connection createConnection() throws JMSException {
        return connectionFactory.createConnection();
    }
    
}
