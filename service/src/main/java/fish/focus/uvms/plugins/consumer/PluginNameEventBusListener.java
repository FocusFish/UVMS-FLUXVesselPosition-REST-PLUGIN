package fish.focus.uvms.plugins.consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fish.focus.schema.exchange.common.v1.AcknowledgeType;
import fish.focus.schema.exchange.common.v1.AcknowledgeTypeType;
import fish.focus.schema.exchange.plugin.v1.PingRequest;
import fish.focus.schema.exchange.plugin.v1.PluginBaseRequest;
import fish.focus.schema.exchange.plugin.v1.SetCommandRequest;
import fish.focus.schema.exchange.plugin.v1.SetConfigRequest;
import fish.focus.schema.exchange.plugin.v1.SetReportRequest;
import fish.focus.schema.exchange.plugin.v1.StartRequest;
import fish.focus.schema.exchange.plugin.v1.StopRequest;
import fish.focus.uvms.exchange.model.mapper.ExchangePluginResponseMapper;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.plugins.StartupBean;
import fish.focus.uvms.plugins.producer.PluginMessageProducer;
import fish.focus.uvms.plugins.service.PluginService;

@MessageDriven(mappedName = "jms/topic/EventBus", activationConfig = {
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "fish.focus.uvms.plugins.rest.movement"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "fish.focus.uvms.plugins.rest.movement"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "ServiceName='fish.focus.uvms.plugins.rest.movement'"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/topic/EventBus"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class PluginNameEventBusListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(PluginNameEventBusListener.class);

    @EJB
    PluginService service;

    @EJB
    PluginMessageProducer messageProducer;

    @EJB
    StartupBean startup;

    @Override
    public void onMessage(Message inMessage) {

        LOG.debug("Eventbus listener for flux-vesselposition-rest (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());

        TextMessage textMessage = (TextMessage) inMessage;

        try {

            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);

            String responseMessage = null;

            switch (request.getMethod()) {
                case SET_CONFIG:
                    SetConfigRequest setConfigRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetConfigRequest.class);
                    AcknowledgeTypeType setConfig = service.setConfig(setConfigRequest.getConfigurations());
                    AcknowledgeType setConfigAck = ExchangePluginResponseMapper.mapToAcknowledgeType(setConfig);
                    responseMessage = ExchangePluginResponseMapper.mapToSetConfigResponse(startup.getRegisterClassName(), setConfigAck);
                    break;
                case SET_COMMAND:
                    SetCommandRequest setCommandRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetCommandRequest.class);
                    AcknowledgeTypeType setCommand = service.setCommand(setCommandRequest.getCommand());
                    AcknowledgeType setCommandAck = ExchangePluginResponseMapper.mapToAcknowledgeType(setCommandRequest.getCommand().getLogId(), setCommandRequest.getCommand().getUnsentMessageGuid(), setCommand);
                    responseMessage = ExchangePluginResponseMapper.mapToSetCommandResponse(startup.getRegisterClassName(), setCommandAck);
                    break;
                case SET_REPORT:
                    SetReportRequest setReportRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetReportRequest.class);
                    AcknowledgeTypeType setReport = service.setReport(setReportRequest.getReport());
                    AcknowledgeType setReportAck = ExchangePluginResponseMapper.mapToAcknowledgeType(setReportRequest.getReport().getLogId(), setReportRequest.getReport().getUnsentMessageGuid(), setReport);
                    responseMessage = ExchangePluginResponseMapper.mapToSetReportResponse(startup.getRegisterClassName(), setReportAck);
                    break;
                case START:
                    JAXBMarshaller.unmarshallTextMessage(textMessage, StartRequest.class);
                    AcknowledgeTypeType start = service.start();
                    AcknowledgeType startAck = ExchangePluginResponseMapper.mapToAcknowledgeType(start);
                    responseMessage = ExchangePluginResponseMapper.mapToStartResponse(startup.getRegisterClassName(), startAck);
                    break;
                case STOP:
                    JAXBMarshaller.unmarshallTextMessage(textMessage, StopRequest.class);
                    AcknowledgeTypeType stop = service.stop();
                    AcknowledgeType stopAck = ExchangePluginResponseMapper.mapToAcknowledgeType(stop);
                    responseMessage = ExchangePluginResponseMapper.mapToStopResponse(startup.getRegisterClassName(), stopAck);
                    break;
                case PING:
                    JAXBMarshaller.unmarshallTextMessage(textMessage, PingRequest.class);
                    responseMessage = ExchangePluginResponseMapper.mapToPingResponse(startup.isIsEnabled(), startup.isIsEnabled());
                    break;
                default:
                    LOG.error("Not supported method: {}", request.getMethod());
                    break;
            }

            messageProducer.sendResponseMessage(responseMessage, textMessage);

        } catch (NullPointerException e) {
            LOG.error("[ Error when receiving message in flux-vesselposition-rest " + startup.getRegisterClassName() + " ]", e);
        } catch (JMSException ex) {
            LOG.error("[ Error when handling JMS message in flux-vesselposition-rest " + startup.getRegisterClassName() + " ]", ex);
        }
    }
}
