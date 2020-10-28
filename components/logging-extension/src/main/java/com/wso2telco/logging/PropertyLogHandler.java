//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.wso2telco.logging;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

public class PropertyLogHandler extends AbstractMediator implements ManagedLifecycle {
	private static final String REGISTRY_PATH = "gov:/apimgt/";
	private static final String MESSAGE_TYPE = "message.type";
	private static final String PAYLOAD_LOGGING_ENABLED = "payload.logging.enabled";
	private static final String REQUEST = "REQUEST";
	private static final String RESPONSE = "RESPONSE";
	private static final String ERRORRESPONSE = "ERRORRESPONSE";
	private static final String UUID = "MESSAGE_ID";
	private static final String ERROR = "error";
	private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
	private static final String API_RESOURCE_CACHE_KEY = "API_RESOURCE_CACHE_KEY";
	private static final String CONTENT_TYPE = "messageType";
	private static final Log logHandler = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");
	private static final String MC = "MC";
	private static final String AX = "AX";
	private static final String TH = "TH";
	private static final String FILE_NAME = "logManagerConfig.xml";

	public PropertyLogHandler() {
	}

	public void init(SynapseEnvironment synapseEnvironment) {
		try {
			String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "logManagerConfig.xml";
			File fXmlFile = new File(configPath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			Document document = documentBuilder.parse(fXmlFile);
			document.getDocumentElement().normalize();
			NodeList requestAttributes = document.getElementsByTagName("REQUEST");
			PropertyReader.setLogProperties(requestAttributes, "REQUEST");
			NodeList responseAttributes = document.getElementsByTagName("RESPONSE");
			PropertyReader.setLogProperties(responseAttributes, "RESPONSE");
			NodeList errorAttributes = document.getElementsByTagName("ERRORRESPONSE");
			PropertyReader.setLogProperties(errorAttributes, "ERRORRESPONSE");
		} catch (ParserConfigurationException var10) {
			var10.printStackTrace();
		} catch (SAXException var11) {
			var11.printStackTrace();
		} catch (IOException var12) {
			var12.printStackTrace();
		}

	}

	public void destroy() {
		throw new UnsupportedOperationException();
	}

	public boolean mediate(MessageContext messageContext) {
		boolean isPayloadLoggingEnabled = true;
		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)messageContext).getAxis2MessageContext();
		isPayloadLoggingEnabled = this.extractPayloadLoggingStatus(messageContext);
		String direction = (String)axis2MessageContext.getProperty("message.type");
		if (direction != null) {
			if (direction.equalsIgnoreCase("REQUEST")) {
				this.logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, "REQUEST");
			} else if (direction.equalsIgnoreCase("RESPONSE")) {
				this.logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, "RESPONSE");
			} else if (direction.equalsIgnoreCase("error")) {
				this.logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, "error");
			}

			return true;
		} else {
			return false;
		}
	}

	private void logProperties(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled, String typeFlag) {
		if (isPayloadLoggingEnabled) {
			String transactionPayload = this.handleAndReturnPayload(messageContext);
			Map<String, Object> headerMap = (Map)axis2MessageContext.getProperty("TRANSPORT_HEADERS");
			StringBuilder transactionLog = new StringBuilder("TRANSACTION:" + typeFlag.toLowerCase());
			HashMap transactionMap;
			if (typeFlag.equalsIgnoreCase("REQUEST")) {
				transactionMap = PropertyReader.getRequestpropertyMap();
			} else if (typeFlag.equalsIgnoreCase("RESPONSE")) {
				transactionMap = PropertyReader.getResponsepropertyMap();
			} else {
				transactionMap = PropertyReader.getErrorPropertiesMap();
			}

			if (transactionMap.isEmpty()) {
				this.init((SynapseEnvironment)null);
			}

			Iterator var9 = transactionMap.keySet().iterator();

			while(var9.hasNext()) {
				String i = (String)var9.next();
				if (((String)transactionMap.get(i)).split(",")[1].equalsIgnoreCase("MC")) {
					transactionLog.append("," + i + ":" + messageContext.getProperty(((String)transactionMap.get(i)).split(",")[0]));
				} else if (((String)transactionMap.get(i)).split(",")[1].equalsIgnoreCase("AX")) {
					transactionLog.append("," + i + ":" + axis2MessageContext.getProperty(((String)transactionMap.get(i)).split(",")[0]));
				} else if (((String)transactionMap.get(i)).split(",")[1].equalsIgnoreCase("TH")) {
					transactionLog.append("," + i + ":" + headerMap.get(((String)transactionMap.get(i)).split(",")[0]));
				} else {
					transactionLog.append("," + i + ":" + transactionPayload.replaceAll("\n", ""));
				}
			}

			logHandler.info(transactionLog);
		}

	}

	private boolean extractPayloadLoggingStatus(MessageContext messageContext) {
		boolean isPayloadLoggingEnabled = true;
		Entry payloadEntry = new Entry("gov:/apimgt/payload.logging.enabled");
		OMTextImpl payloadEnableRegistryValue = (OMTextImpl)messageContext.getConfiguration().getRegistry().getResource(payloadEntry, (Properties)null);
		if (payloadEnableRegistryValue != null) {
			String payloadLogEnabled = payloadEnableRegistryValue.getText();
			if (nullOrTrimmed(payloadLogEnabled) != null) {
				isPayloadLoggingEnabled = Boolean.valueOf(payloadLogEnabled);
			}
		}

		return isPayloadLoggingEnabled;
	}

	private static String nullOrTrimmed(String inputString) {
		String result = null;
		if (inputString != null && inputString.trim().length() > 0) {
			result = inputString.trim();
		}

		return result;
	}

	private String handleAndReturnPayload(MessageContext messageContext) {
		String payload = "";

		try {
			payload = messageContext.getEnvelope().getBody().toString();
		} catch (Exception var4) {
			var4.printStackTrace();
			payload = "payload dropped due to invalid format";
		}

		return payload;
	}

	private String getPayloadSting(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext) {
		String payload;
		if (axis2MessageContext.getProperty("messageType").equals("application/json")) {
			payload = XML.toJSONObject(messageContext.getEnvelope().getBody().getFirstElement().getFirstElement().toString()).toString();
		} else if (axis2MessageContext.getProperty("messageType").equals("text/plain")) {
			payload = messageContext.getEnvelope().getBody().getFirstElement().toString();
		} else {
			payload = messageContext.getEnvelope().getBody().toString();
		}

		return payload;
	}
}
