//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.wso2telco.logging;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropertyReader {
    private static final String REQUEST = "REQUEST";
    private static final String RESPONSE = "RESPONSE";
    private static HashMap<String, String> requestpropertyMap = new LinkedHashMap();
    private static HashMap<String, String> responsepropertyMap = new LinkedHashMap();
    private static HashMap<String, String> errorpropertyMap = new LinkedHashMap();

    public PropertyReader() {
    }

    public static HashMap<String, String> getRequestpropertyMap() {
        return requestpropertyMap;
    }

    public static HashMap<String, String> getResponsepropertyMap() {
        return responsepropertyMap;
    }

    public static HashMap<String, String> getErrorPropertiesMap() {
        return errorpropertyMap;
    }

    public static void setLogProperties(NodeList requestNodeList, String flag) {
        Node requestNode = requestNodeList.item(0);
        if (requestNode.getNodeType() == 1) {
            Element requestElement = (Element)requestNode;
            String requestEl = requestElement.getTextContent().trim();
            String[] requestArray = requestEl.split("\n");
            int y;
            if (flag.equalsIgnoreCase("REQUEST")) {
                for(y = 0; y < requestArray.length; ++y) {
                    requestpropertyMap.put(requestElement.getElementsByTagName("*").item(y).getNodeName(), requestArray[y].trim());
                }
            } else if (flag.equalsIgnoreCase("RESPONSE")) {
                for(y = 0; y < requestArray.length; ++y) {
                    responsepropertyMap.put(requestElement.getElementsByTagName("*").item(y).getNodeName(), requestArray[y].trim());
                }
            } else {
                for(y = 0; y < requestArray.length; ++y) {
                    errorpropertyMap.put(requestElement.getElementsByTagName("*").item(y).getNodeName(), requestArray[y].trim());
                }
            }
        }

    }
}
