package org.wso2.apim;

import org.apache.commons.codec.binary.Base64;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jwt Decode Mediator Implementation.
 */
public class JwtDecodeMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(JwtDecodeMediator.class);
    private static JSONObject accountIdsJson = new JSONObject();
    private String jwtHeader;
    private String accountIds;
    private static String externalID;

    private static String retrieveAccountId(String accountRequestInfo) {
        String[] split_string = accountRequestInfo.split("\\.");
        String base64EncodedBody = split_string[1];

        Base64 base64 = new Base64();
        try {
            String decodedString = new String(base64.decode(base64EncodedBody.getBytes()));
            JSONParser parser = new JSONParser();
            JSONObject accountRequestInfoJson = (JSONObject) parser.parse(decodedString);
            if (accountRequestInfoJson.containsKey("http://wso2.org/claims/externalid")) {

                externalID = accountRequestInfoJson.get("http://wso2.org/claims/externalid").toString();
                accountIdsJson.put("data", externalID);

                String transformedJson = accountIdsJson.toString();
                return transformedJson;
            } else {
                if (log.isDebugEnabled()) log.error("external id is not available");
            }
        } catch (ParseException e) {
            log.error("Error in passing Account-Request-Information " + e.toString());
        }
        return null;
    }

    @Override
    public boolean mediate(MessageContext context) {
        accountIds = retrieveAccountId(getJWT_HEADER());
        JsonUtil.newJsonPayload(((Axis2MessageContext) context).getAxis2MessageContext(), accountIds,
                true, true);
        context.setProperty("accountIds", accountIdsJson);
        context.setProperty("externalID", externalID);

        log.info("----------- ACCOUNT_ID--------------------" + context.getProperty("accountIds").toString());
        log.info("----------  EXT_ID    --------------------" + context.getProperty("externalID").toString());
        return true;
    }

    public String getJWT_HEADER() {
        return jwtHeader;
    }

    public void setJWT_HEADER(String jwtHeader) {
        this.jwtHeader = jwtHeader;
    }

    public String getAccountIds() {
        return accountIds;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public void setAccountIds(String accountIds) {
        this.accountIds = accountIds;
    }
}
