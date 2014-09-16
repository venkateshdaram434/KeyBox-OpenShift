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

import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.OpenShiftUtils;
import com.openshift.client.*;
import com.openshift.client.cartridge.ICartridge;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to manage systems
 */
public class SystemAction extends ActionSupport implements ServletRequestAware {

    HttpServletRequest servletRequest;

    SortedSet sortedSet = new SortedSet();

    List<String> appNmList = new ArrayList();
    List<String> gearGroupList = new ArrayList();
    List<String> cartridgeNmList = new ArrayList();

    boolean showGears = true;

    String appNm;
    String gearGroupNm;
    String cartridgeNm;

    @Action(value = "/admin/setSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp"),
                    @Result(name = "error", location = "/login.action", type = "redirect")
            }
    )
    public String setAdminSystems() {

        String retVal = SUCCESS;

        String authToken = AuthUtil.getAuthToken(servletRequest.getSession());


        Long userId = AuthDB.getUserIdByAuthToken(authToken);
        
        List<HostSystem>hostSystemList = new ArrayList<HostSystem>();
        

        try {
            IOpenShiftConnection connection = new OpenShiftConnectionFactory().getAuthTokenConnection(OpenShiftUtils.CLIENT_NAME, authToken, OpenShiftUtils.LIBRA_SERVER);
            IUser user = connection.getUser();


            IDomain domain = user.getDomain(OpenShiftUtils.NAMESPACE);
            for (IApplication app : domain.getApplications()) {

                //show all gears
                if (showGears) {
                    for (IGearGroup gearGroup : app.getGearGroups()) {
                        List<String> cartridgeNmList = new ArrayList<>();
                        for(ICartridge cartridge : gearGroup.getCartridges()){
                                cartridgeNmList.add(cartridge.getName());
                        }
                        for (IGear gear : gearGroup.getGears()) {
                            if (StringUtils.isNotEmpty(gear.getSshUrl())) {
                                String sshUrl = gear.getSshUrl().replaceAll("ssh://", "");

                                HostSystem hostSystem = new HostSystem();
                                hostSystem.setUser(sshUrl.split("@")[0]);
                                hostSystem.setHost(sshUrl.split("@")[1]);
                                hostSystem.setAppNm(app.getName());
                                hostSystem.setDomain(app.getDomain().getId());
                                hostSystem.setGearGroupNm(gearGroup.getName());
                                hostSystem.setUserId(userId);
                                hostSystem.setCartridgeNm(StringUtils.join(cartridgeNmList, ", "));

                                hostSystemList.add(hostSystem);
                            }
                        }

                    }
                } else {
                    if (StringUtils.isNotEmpty(app.getSshUrl())) {
                        String sshUrl = app.getSshUrl().replaceAll("ssh://", "");

                        HostSystem hostSystem = new HostSystem();
                        hostSystem.setUser(sshUrl.split("@")[0]);
                        hostSystem.setHost(sshUrl.split("@")[1]);
                        hostSystem.setAppNm(app.getName());
                        hostSystem.setDomain(app.getDomain().getId());
                        hostSystem.setUserId(userId);

                        hostSystemList.add(hostSystem);
                    }

                }
                
            }
            SystemDB.setSystem(hostSystemList, userId);
            retVal = viewAdminSystems();
            
        } catch (OpenShiftEndpointException ex) {
            ex.printStackTrace();
            retVal = ERROR;
        }

        return retVal;
    }

    @Action(value = "/admin/viewSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp")
            }
    )
    public String viewAdminSystems() {


        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));

        //create filter for map
        Map<String, String> filter = new LinkedHashMap<String, String>();
        if (StringUtils.isNotEmpty(appNm)) {
            filter.put("app_nm", appNm);
        }
        if (StringUtils.isNotEmpty(gearGroupNm)) {
            filter.put("gear_group_nm", gearGroupNm);
        }
        if (StringUtils.isNotEmpty(cartridgeNm)) {
            filter.put("cartridge_nm", cartridgeNm);
        }

        Connection con = DBUtils.getConn();
        sortedSet = SystemDB.getSystemSet(con, sortedSet, filter, userId);

        appNmList = SystemDB.getAppNms(con, userId);
        gearGroupList = SystemDB.getGearNms(con, userId);
        cartridgeNmList = SystemDB.getCartridgeNms(con,userId);
        
        DBUtils.closeConn(con);

        return SUCCESS;
    }

    public boolean getShowGears() {
        return showGears;
    }

    public void setShowGears(boolean showGears) {
        this.showGears = showGears;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public List<String> getAppNmList() {
        return appNmList;
    }

    public void setAppNmList(List<String> appNmList) {
        this.appNmList = appNmList;
    }

    public String getAppNm() {
        return appNm;
    }

    public void setAppNm(String appNm) {

        this.appNm = appNm;
    }

    public List<String> getGearGroupList() {
        return gearGroupList;
    }

    public void setGearGroupList(List<String> gearGroupList) {
        this.gearGroupList = gearGroupList;
    }

    public String getGearGroupNm() {
        return gearGroupNm;
    }

    public void setGearGroupNm(String gearGroupNm) {
        this.gearGroupNm = gearGroupNm;
    }

    public String getCartridgeNm() {
        return cartridgeNm;
    }

    public void setCartridgeNm(String cartridgeNm) {
        this.cartridgeNm = cartridgeNm;
    }

    public List<String> getCartridgeNmList() {
        return cartridgeNmList;
    }

    public void setCartridgeNmList(List<String> cartridgeNmList) {
        this.cartridgeNmList = cartridgeNmList;
    }
    
    
}
