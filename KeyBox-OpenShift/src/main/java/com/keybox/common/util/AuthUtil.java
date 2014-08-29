/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.common.util;

import com.keybox.manage.util.EncryptionUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Utility to obtain the authentication token from the http session and the user id from the auth token
 */
public class AuthUtil {

    /**
     * query session for authentication token
     *
     * @param session http session
     * @return authentication token
     */
    public static String getAuthToken( HttpSession session) {
        String authToken = (String) session.getAttribute("authToken");
        authToken = EncryptionUtil.decrypt(authToken);
        return authToken;
    }

    /**
     * query session for timeout
     *
     * @param session http session
     * @return timeout string
     */
    public static String getTimeout(HttpSession session) {
        String timeout = (String) session.getAttribute("timeout");
        return timeout;
    }

    /**
     * set session authentication token
     *
     * @param session http session
     * @param authToken authentication token
     */
    public static void setAuthToken(HttpSession session, String authToken) {
        if (authToken != null && !authToken.trim().equals("")) {
            session.setAttribute("authToken", EncryptionUtil.encrypt(authToken));
        }
    }

    /**
     * set session timeout
     *
     * @param session http session
     */
    public static void setTimeout(HttpSession session) {
        //set session timeout
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
        Calendar timeout = Calendar.getInstance();
        timeout.add(Calendar.MINUTE, 15);
        session.setAttribute("timeout", sdf.format(timeout.getTime()));
    }

    /**
     * delete all session information
     *
     * @param session
     */
    public static void deleteAllSession(HttpSession session) {

        session.setAttribute("timeout", null);
        session.setAttribute("authToken", null);
        session.setAttribute("sessionId",null);

        session.invalidate();
    }

}
