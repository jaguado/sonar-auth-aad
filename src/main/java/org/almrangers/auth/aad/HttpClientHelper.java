/*******************************************************************************
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package org.almrangers.auth.aad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpClientHelper {

  private static final String RESPONSE_MSG = "responseMsg";
  private static final String RESPONSE_CODE = "responseCode";

  private HttpClientHelper() {
    // Static methods
  }

  public static String getResponseStringFromConn(HttpURLConnection conn, boolean isSuccess) throws IOException {

    BufferedReader reader;
    if (isSuccess) {
      reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    } else {
      reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
    }
    StringBuffer stringBuffer = new StringBuffer();
    String line = "";
    while ((line = reader.readLine()) != null) {
      stringBuffer.append(line);
    }

    return stringBuffer.toString();
  }

  /**
   * for bad response, whose responseCode is not 200 level
   *
   * @param responseCode
   * @param goodRespStr
   * @return
   * @throws JSONException
   */
  public static JSONObject processGoodRespStr(int responseCode, String goodRespStr) throws JSONException {
    JSONObject response = new JSONObject();
    response.put(RESPONSE_CODE, responseCode);
    if ("".equalsIgnoreCase(goodRespStr)) {
      response.put(RESPONSE_MSG, "");
    } else {
      response.put(RESPONSE_MSG, new JSONObject(goodRespStr));
    }

    return response;
  }

  /**
   * for good response
   *
   * @param responseCode
   * @param responseMsg
   * @return
   * @throws JSONException
   */
  public static JSONObject processBadRespStr(int responseCode, String responseMsg) throws JSONException {

    JSONObject response = new JSONObject();
    response.put(RESPONSE_CODE, responseCode);
    if ("".equalsIgnoreCase(responseMsg)) { // good response is empty string
      response.put(RESPONSE_MSG, "");
    } else { // bad response is json string
      JSONObject errorObject = new JSONObject(responseMsg).optJSONObject("odata.error");

      String errorCode = errorObject.optString("code");
      String errorMsg = errorObject.optJSONObject("message").optString("value");
      response.put(RESPONSE_CODE, responseCode);
      response.put("errorCode", errorCode);
      response.put("errorMsg", errorMsg);
    }

    return response;
  }
}
