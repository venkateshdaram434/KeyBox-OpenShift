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
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.model.ApplicationKey;
import com.keybox.manage.model.Auth;
import com.keybox.manage.model.HostSystem;
import com.openshift.client.*;
import com.openshift.client.configuration.OpenShiftConfiguration;
import com.opensymphony.xwork2.ActionSupport;
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

    private static final String generatedKeyNm=AppConfig.getProperty("generatedKeyNm");

    @Action(value = "/login",
            results = {
                    @Result(name = "success", location = "/login.jsp")
            }
    )
    public String login() {

        return SUCCESS;
    }

    @Action(value = "/admin/menu",
            results = {
                    @Result(name = "success", location = "/admin/menu.jsp")
            }
    )
    public String menu() {

        return SUCCESS;
    }


    @Action(value = "/loginSubmit",
            results = {
                    @Result(name = "input", location = "/login.jsp"),
                    @Result(name = "success", location = "/admin/viewSystems.action", type = "redirect")
            }
    )
    public String loginSubmit() {
        String retVal = SUCCESS;


        String authToken = null;
        try {
            String openshiftServer = new OpenShiftConfiguration().getLibraServer();
            IOpenShiftConnection connection = new OpenShiftConnectionFactory().getConnection("keybox", auth.getUsername(), auth.getPassword(), openshiftServer);

            IUser user = connection.getUser();

            authToken = AuthDB.login(auth);

            //get userId for auth token
            Long userId=AuthDB.getUserIdByAuthToken(authToken);


            //add generated public key
            ApplicationKey appKey = PrivateKeyDB.createApplicationKey(userId);

            //set public key
            String publicKey=appKey.getPublicKey().split(" ")[1];

            //check to see if key exists and matches
            ISSHPublicKey existingKey= user.getSSHKeyByName(generatedKeyNm);
            if(existingKey==null || !publicKey.equals(existingKey.getPublicKey())){
                user.deleteKey(generatedKeyNm);
                appKey.setPublicKey(publicKey);
                user.putSSHKey(generatedKeyNm, appKey);
            }


            //delete all systems for user and get latest from openshift
            SystemDB.deleteSystems(userId);
            for(IDomain domain : user.getDomains()){
                for(IApplication app : domain.getApplications()){
                    String sshUrl=app.getSshUrl().replaceAll("ssh://","");

                    HostSystem hostSystem= new HostSystem();
                    hostSystem.setUser(sshUrl.split("@")[0]);
                    hostSystem.setHost(sshUrl.split("@")[1]);
                    hostSystem.setAppNm(app.getName());
                    hostSystem.setDomain(app.getDomain().getId());
                    hostSystem.setUserId(userId);

                    SystemDB.insertSystem(hostSystem);


                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (OpenShiftEndpointException ex) {
            ex.printStackTrace();
        }


        if (authToken != null) {
            AuthUtil.setAuthToken(servletRequest.getSession(), authToken);
            AuthUtil.setTimeout(servletRequest.getSession());
        } else {
            addActionError("Invalid username and password combination");
            retVal = INPUT;
        }


        return retVal;
    }

    @Action(value = "/logout",
            results = {
                    @Result(name = "success", location = "/login.action", type = "redirect")
            }
    )
    public String logout() {
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
}
