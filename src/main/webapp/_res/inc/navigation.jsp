<%
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
%>
<%@ taglib prefix="s" uri="/struts-tags" %>


<div class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">

        <div class="navbar-header">
            <div class="navbar-brand" >
                <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_50x38.png" alt="keybox"/></div>
            </div>
            <div class="navbar-brand">
                KeyBox<span class="note" style="padding-top:2px;font-size: x-small"> - OpenShift</span>
            </div>
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                     <span class="sr-only">Toggle navigation</span>
                     <span class="icon-bar"></span>
                     <span class="icon-bar"></span>
                     <span class="icon-bar"></span>
            </button>
        </div>
        <div class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="../admin/setSystems.action">Secure Shell</a></li>
                <li><a href="../admin/userSettings.action">Settings</a></li>

                <li><a href="../logout.action">Logout</a></li>
            </ul>
        </div>
        <!--/.nav-collapse -->
    </div>
</div>

