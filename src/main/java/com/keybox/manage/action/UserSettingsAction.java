/**
 * Copyright 2015 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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
import com.keybox.manage.db.UserThemeDB;
import com.keybox.manage.model.UserSettings;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Action for user settings
 */
public class UserSettingsAction extends ActionSupport implements ServletRequestAware {

    HttpServletRequest servletRequest;
    UserSettings userSettings;

    @Action(value = "/admin/userSettings",
            results = {
                    @Result(name = "success", location = "/admin/user_settings.jsp")
            }
    )
    public String userSettings() {
        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));
        userSettings = UserThemeDB.getTheme(userId);
        return SUCCESS;
    }


    @Action(value = "/admin/themeSubmit",
            results = {
                    @Result(name = "success", location = "/admin/setSystems.action", type = "redirect")
            }
    )
    public String themeSubmit() {

        Long userId = AuthDB.getUserIdByAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));
        UserThemeDB.saveTheme(userId, userSettings);

        return SUCCESS;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
}
