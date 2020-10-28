//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.wso2telco.logging;

import org.apache.axis2.context.MessageContext;

public class UniqueIDGenerator {
    private static long id;
    private static final String REQUEST_ID = "mife.prop.requestId";

    public UniqueIDGenerator() {
    }

    public static synchronized String generateAndSetUniqueID(String axtype, MessageContext context) {
        String requestId = System.currentTimeMillis() + axtype + "0" + id++;
        context.setProperty("mife.prop.requestId", requestId);
        return requestId;
    }
}
