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
package com.keybox.manage.action;

import com.keybox.common.util.AppConfig;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.model.ApplicationKey;
import com.keybox.manage.model.Auth;
import com.keybox.manage.util.OTPUtil;
import com.keybox.manage.util.OpenShiftUtils;
import com.openshift.client.*;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Action to auth to keybox
 */
public class LoginAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    HttpServletResponse servletResponse;
    HttpServletRequest servletRequest;
    Auth auth;
    //check if otp is enabled
    boolean otpEnabled="true".equals(AppConfig.getProperty("enableOTP"));

    private static final String generatedKeyNm = AppConfig.getProperty("generatedKeyNm") + "-" + OpenShiftUtils.APP_DNS;

    @Action(value = "/login",
            results = {
                    @Result(name = "success", location = "/login.jsp")
            }
    )
    public String login() {

        return SUCCESS;
    }

    @Action(value = "/loginSubmit",
            results = {
                    @Result(name = "input", location = "/login.jsp"),
                    @Result(name = "otp", location = "/admin/viewOTP.action", type = "redirect"),
                    @Result(name = "success", location = "/admin/setSystems.action", type = "redirect")
            }
    )
    public String loginSubmit() {


        String sharedSecret = null;
        try {
            IOpenShiftConnection connection = new OpenShiftConnectionFactory().getConnection(OpenShiftUtils.CLIENT_NAME, auth.getUsername(), auth.getPassword(), OpenShiftUtils.LIBRA_SERVER);

            IUser user = connection.getUser();

            //if user is not apart of domain return
            if (!user.hasDomain(OpenShiftUtils.NAMESPACE)) {
                addActionError("User is not associated with domain");
                return INPUT;
            }

            //set auth token
            auth.setAuthToken(user.createAuthorization(OpenShiftUtils.APP_DNS, "session", 60 * 60 * 24).getToken());
            auth.setOpenshiftId(user.getId());

            //check OTP token
            if (otpEnabled) {
                //get shared secret
                sharedSecret = AuthDB.getSharedSecret(auth.getOpenshiftId());
                if (StringUtils.isNotEmpty(sharedSecret) && (auth.getOtpToken() == null || !OTPUtil.verifyToken(sharedSecret, auth.getOtpToken()))) {
                    addFieldError("auth.otpToken", "Invalid");
                    return INPUT;
                }
            }

            auth = AuthDB.setAuth(auth);

            //add generated public key
            ApplicationKey appKey = PrivateKeyDB.createApplicationKey(auth.getId());

            //set public key
            String publicKey = appKey.getPublicKey().split(" ")[1];


            //check to see if key exists and matches
            ISSHPublicKey existingKey = user.getSSHKeyByName(generatedKeyNm);
            if (existingKey == null || !publicKey.equals(existingKey.getPublicKey())) {
                user.removeSSHKey(generatedKeyNm);
                appKey.setPublicKey(publicKey);
                user.addSSHKey(generatedKeyNm, appKey);
            }

            AuthUtil.setAuthToken(servletRequest.getSession(), auth.getAuthToken());
            AuthUtil.setTimeout(servletRequest.getSession());

        } catch (OpenShiftException ex) {
            ex.printStackTrace();
            addActionError("Invalid username and password combination");
            return INPUT;
        }

        //for first time login redirect to set OTP
        if (otpEnabled && StringUtils.isEmpty(sharedSecret)) {
            return "otp";
        }

        return SUCCESS;
    }

    @Action(value = "/logout",
            results = {
                    @Result(name = "success", location = "/login.action", type = "redirect")
            }
    )
    public String logout() {

        try {
            String authToken = AuthUtil.getAuthToken(servletRequest.getSession());
            IOpenShiftConnection connection = new OpenShiftConnectionFactory().getAuthTokenConnection(OpenShiftUtils.CLIENT_NAME, authToken, OpenShiftUtils.LIBRA_SERVER);
            connection.getUser().getAuthorization().destroy();
        } catch (OpenShiftException ex) {
            ex.printStackTrace();
        }
        AuthUtil.deleteAllSession(servletRequest.getSession());

        return SUCCESS;
    }

    /**
     * Validates fields for auth submit
     */
    public void validateLoginSubmit() {
        if (auth.getUsername() == null ||
                auth.getUsername().trim().equals("")) {
            addFieldError("auth.username", "Required");
        }
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", "Required");
        }


    }


    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }


    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public boolean isOtpEnabled() {
        return otpEnabled;
    }

    public boolean getOtpEnabled() {
        return otpEnabled;
    }

    public void setOtpEnabled(boolean otpEnabled) {
        this.otpEnabled = otpEnabled;
    }
}
