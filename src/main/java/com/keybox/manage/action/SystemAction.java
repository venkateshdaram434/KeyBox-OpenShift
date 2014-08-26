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
import com.keybox.manage.model.SortedSet;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to manage systems
 */
public class SystemAction extends ActionSupport  implements ServletRequestAware {

    HttpServletRequest servletRequest;

    SortedSet sortedSet = new SortedSet();

    List<String> appNmList = new ArrayList();
    List<String> domainList = new ArrayList();

    String domain;
    String appNm;


    @Action(value = "/admin/viewSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp")
            }
    )
    public String viewAdminSystems() {


        Long userId= AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));

        //create filter for map
        Map<String, String> filter= new LinkedHashMap<String,String>();
        if(StringUtils.isNotEmpty(appNm)){
            filter.put("app_nm", appNm);
        }
        if(StringUtils.isNotEmpty(domain)){
            filter.put("domain", domain);
        }
        sortedSet = SystemDB.getSystemSet(sortedSet, filter, userId);

        domainList=SystemDB.getDomains(userId);
        appNmList=SystemDB.getAppNms(userId);


        return SUCCESS;
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

    public List<String> getDomainList() {
        return domainList;
    }

    public void setDomainList(List<String> domainList) {
        this.domainList = domainList;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAppNm() {
        return appNm;
    }

    public void setAppNm(String appNm) {
        this.appNm = appNm;
    }
}
