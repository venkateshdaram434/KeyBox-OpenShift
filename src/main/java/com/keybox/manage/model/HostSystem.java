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
package com.keybox.manage.model;

/**
 * Value object that contains host system information
 */
public class HostSystem {
    Long id;
    String appNm;
    String user;
    String host;
    Integer port = 22;
    String domain;
    String displayLabel;
    boolean checked=false;
    String statusCd=INITIAL_STATUS;
    String errorMsg;
    Long userId;

    public static final String INITIAL_STATUS="INITIAL";
    public static final String AUTH_FAIL_STATUS="AUTHFAIL";
    public static final String PUBLIC_KEY_FAIL_STATUS="KEYAUTHFAIL";
    public static final String GENERIC_FAIL_STATUS="GENERICFAIL";
    public static final String SUCCESS_STATUS="SUCCESS";



    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }



    public String getDisplayLabel() {
        return getAppNm() +" - ( " +getUser() +"@"+getHost()+" )";
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {

        this.errorMsg = errorMsg;
    }

    public String getDomain() {
        return domain;
    }

    public String getAppNm() {
        return appNm;
    }

    public void setAppNm(String appNm) {
        this.appNm = appNm;
    }

    public void setDomain(String domain) {

        this.domain = domain;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
