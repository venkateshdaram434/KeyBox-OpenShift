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

import com.google.gson.Gson;
import com.jcraft.jsch.ChannelShell;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.*;
import com.keybox.manage.model.*;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This action will create composite ssh terminals to be used
 */
public class SecureShellAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    List<SessionOutput> outputList;
    String command;
    HttpServletResponse servletResponse;
    HttpServletRequest servletRequest;
    List<Long> systemSelectId;
    HostSystem currentSystemStatus;
    HostSystem pendingSystemStatus;
    String password;
    String passphrase;
    Integer id;
    List<HostSystem> systemList = new ArrayList<HostSystem>();
    List<HostSystem> allocatedSystemList = new ArrayList<HostSystem>();
    UserSettings userSettings;

    static Map<Long, UserSchSessions> userSchSessionMap = new ConcurrentHashMap<Long, UserSchSessions>();




    /**
     * creates composite terminals if there are errors or authentication issues.
     */
    @Action(value = "/admin/createTerms",
            results = {
                    @Result(name = "success", location = "/admin/secure_shell.jsp")
            }
    )
    public String createTerms() {

        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));
        if (pendingSystemStatus != null && pendingSystemStatus.getId() != null) {


            //get status
            currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId(), userId);
            //if initial status run script
            if (currentSystemStatus != null
                    && (HostSystem.INITIAL_STATUS.equals(currentSystemStatus.getStatusCd())
                    || HostSystem.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())
                    || HostSystem.PUBLIC_KEY_FAIL_STATUS.equals(currentSystemStatus.getStatusCd()))
                    ) {

                //set current session
                currentSystemStatus = SSHUtil.openSSHTermOnSystem(passphrase, password, userId, currentSystemStatus, userSchSessionMap);

            }
            if (currentSystemStatus != null
                    && (HostSystem.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())
                    || HostSystem.PUBLIC_KEY_FAIL_STATUS.equals(currentSystemStatus.getStatusCd()))) {

                pendingSystemStatus = currentSystemStatus;

            } else {

                pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);
                //if success loop through systems until finished or need password
                while (pendingSystemStatus != null && currentSystemStatus != null && HostSystem.SUCCESS_STATUS.equals(currentSystemStatus.getStatusCd())) {
                    currentSystemStatus = SSHUtil.openSSHTermOnSystem(passphrase, password, userId, pendingSystemStatus, userSchSessionMap);
                    pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);
                }


            }

        }
        //set system list if no pending systems
        if (SystemStatusDB.getNextPendingSystem(userId) == null) {
            setSystemList(userId);
            
            //set allocated systems for connect to
            SortedSet sortedSet=new SortedSet();
            sortedSet.setOrderByField(SystemDB.SORT_BY_NAME);
            
            sortedSet=SystemDB.getSystemSet(sortedSet,userId);
           
            allocatedSystemList = (List<HostSystem>) sortedSet.getItemList();
            //set theme
            this.userSettings =UserThemeDB.getTheme(userId);

        }


        return SUCCESS;
    }


    @Action(value = "/admin/getNextPendingSystemForTerms",
            results = {
                    @Result(name = "success", location = "/admin/secure_shell.jsp")
            }
    )
    public String getNextPendingSystemForTerms() {
        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));
        currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId(), userId);
        currentSystemStatus.setErrorMsg("Auth fail");
        currentSystemStatus.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);


        SystemStatusDB.updateSystemStatus(currentSystemStatus, userId);

        pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

        //set system list if no pending systems
        if (pendingSystemStatus == null) {
            setSystemList(userId);
        }

        return SUCCESS;
    }

    @Action(value = "/admin/selectSystemsForCompositeTerms",
            results = {
                    @Result(name = "success", location = "/admin/secure_shell.jsp")
            }
    )
    public String selectSystemsForCompositeTerms() {

        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));

        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            SystemStatusDB.setInitialSystemStatus(systemSelectId, userId);
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

        }
        return SUCCESS;
    }


    @Action(value = "/admin/exitTerms",
            results = {
                    @Result(name = "success", location = "/admin/setSystems.action", type = "redirect")

            }
    )
    public String exitTerms() {


        return SUCCESS;
    }

    @Action(value = "/admin/disconnectTerm")
    public String disconnectTerm() {
        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));
        if (SecureShellAction.getUserSchSessionMap() != null) {
            UserSchSessions userSchSessions = SecureShellAction.getUserSchSessionMap().get(userId);
            if (userSchSessions != null) {
                try {
                    SchSession schSession = userSchSessions.getSchSessionMap().get(id);

                    //disconnect ssh session
                    if(schSession!=null) {
                        if (schSession.getChannel() != null)
                            schSession.getChannel().disconnect();
                        if (schSession.getSession() != null)
                            schSession.getSession().disconnect();
                        schSession.setChannel(null);
                        schSession.setSession(null);
                        schSession.setInputToChannel(null);
                        schSession.setCommander(null);
                        schSession.setOutFromChannel(null);
                        schSession = null;
                    }
                    //remove from map
                    userSchSessions.getSchSessionMap().remove(id);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


        }


        return null;
    }


    @Action(value = "/admin/createSession")
    public String createSession() {

        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));

        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            SystemStatusDB.setInitialSystemStatus(systemSelectId, userId);
            
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

            createTerms();
            
        }

        return null;


    }

    @Action(value = "/admin/setPtyType")
    public String setPtyType() {

        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));
        if (SecureShellAction.getUserSchSessionMap() != null) {
            UserSchSessions userSchSessions = SecureShellAction.getUserSchSessionMap().get(userId);
            if (userSchSessions != null && userSchSessions.getSchSessionMap() != null) {

                SchSession schSession = userSchSessions.getSchSessionMap().get(id);

                ChannelShell channel = (ChannelShell) schSession.getChannel();
                channel.setPtySize((int) Math.floor(userSettings.getPtyWidth() / 7.2981), (int) Math.floor(userSettings.getPtyHeight() / 14.4166), userSettings.getPtyWidth(), userSettings.getPtyHeight());
                schSession.setChannel(channel);

            }


        }

        return null;
    }

    /**
     * set system list once all connections have been attempted
     *
     * @param userId    user id
     */
    private void setSystemList(Long userId) {


        //check user map
        if (userSchSessionMap != null && !userSchSessionMap.isEmpty() && userSchSessionMap.get(userId) != null) {

            //get user sessions
            Map<Integer, SchSession> schSessionMap = userSchSessionMap.get(userId).getSchSessionMap();


            for (SchSession schSession : schSessionMap.values()) {
                //add to host system list
                systemList.add(schSession.getHostSystem());
                //run script it exists
            }
        }

    }

    public List<SessionOutput> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<SessionOutput> outputList) {
        this.outputList = outputList;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public List<Long> getSystemSelectId() {
        return systemSelectId;
    }

    public void setSystemSelectId(List<Long> systemSelectId) {
        this.systemSelectId = systemSelectId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HostSystem getCurrentSystemStatus() {
        return currentSystemStatus;
    }

    public void setCurrentSystemStatus(HostSystem currentSystemStatus) {
        this.currentSystemStatus = currentSystemStatus;
    }

    public HostSystem getPendingSystemStatus() {
        return pendingSystemStatus;
    }

    public void setPendingSystemStatus(HostSystem pendingSystemStatus) {
        this.pendingSystemStatus = pendingSystemStatus;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public List<HostSystem> getSystemList() {
        return systemList;
    }

    public void setSystemList(List<HostSystem> systemList) {
        this.systemList = systemList;
    }

    public static Map<Long, UserSchSessions> getUserSchSessionMap() {
        return userSchSessionMap;
    }

    public static void setUserSchSessionMap(Map<Long, UserSchSessions> userSchSessionMap) {
        SecureShellAction.userSchSessionMap = userSchSessionMap;
    }

    public List<HostSystem> getAllocatedSystemList() {
        return allocatedSystemList;
    }

    public void setAllocatedSystemList(List<HostSystem> allocatedSystemList) {
        this.allocatedSystemList = allocatedSystemList;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
}


