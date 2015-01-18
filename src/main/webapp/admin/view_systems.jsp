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
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="../_res/inc/header.jsp"/>
    <script type="text/javascript">
        $(document).ready(function () {

            $("#view_btn").button().click(function () {
                $("#viewSystems").submit();
            });

            $(".select_frm_btn").button().click(function () {
                $("#select_frm").submit();
            });
            //select all check boxes
            $("#select_frm_systemSelectAll").click(function () {
                if ($(this).is(':checked')) {
                    $(".systemSelect").prop('checked', true);
                } else {
                    $(".systemSelect").prop('checked', false);
                }
            });
            $(".sort,.sortAsc,.sortDesc").click(function () {
                var id = $(this).attr('id')

                if ($('#viewSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSystems_sortedSet_orderByField').attr('value', id);
                $("#viewSystems").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>


            $('.scrollableTable').tableScroll({height: 500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>


    <title>KeyBox - Manage Systems</title>
</head>
<body>


<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">


    <h3>Composite SSH Terminals</h3>

    <s:if test="appNmList!= null && !appNmList.isEmpty()">
        Select the systems below to generate composite SSH sessions in multiple terminals
        <table style="min-width:0px">
            <tr>
                <td class="align_left">
                    <s:form action="setSystems" theme="simple">

                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:if test="showGears==true">
                            <s:hidden name="showGears" value="false"/>
                            <s:submit cssClass="btn btn-danger" value="Show Applications"/>
                        </s:if>
                        <s:else>
                            <s:hidden name="showGears" value="true"/>
                            <s:submit cssClass="btn btn-success" value="Show All Gears"/>
                        </s:else>
                    </s:form>
                </td>
                <td> |</td>
                <td>
                    <s:form action="viewSystems" theme="simple">
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <table style="min-width:0px">
                            <tr>
                                <td style="padding-left:0px;"><s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_NAME}']" class="view_frm_select"
                                                                        list="appNmList"
                                                                        headerKey=""
                                                                        headerValue="-Select Application-"/>
                                </td>
                                <td style="padding-left:0px;"><s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_DOMAIN}']" class="view_frm_select"
                                                                        list="domainList"
                                                                        headerKey=""
                                                                        headerValue="-Select Domain-"/>
                                </td>
                                <s:if test="showGears==true">
                                    <td style="padding-left:0px;"><s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_CARTRIDGE_NM}']" class="view_frm_select"
                                                                            list="cartridgeNmList"
                                                                            headerKey=""
                                                                            headerValue="-Select Cartridge-"/>
                                    </td>
                                    <td style="padding-left:0px;"><s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_GEAR_GROUP_NM}']" class="view_frm_select"
                                                                            list="gearGroupList"
                                                                            headerKey=""
                                                                            headerValue="-Select Gear Group-"/>
                                    </td>
                                </s:if>
                                <td style="padding:5px 5px 0px 5px;">
                                    <s:hidden name="showGears"/>
                                    <div id="view_btn" class="btn btn-default">Filter</div>
                                </td>
                            </tr>
                        </table>
                    </s:form>
                </td>
            </tr>
        </table>

    </s:if>
    <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

        <s:form action="selectSystemsForCompositeTerms" id="select_frm" theme="simple">
            <table class="table-striped scrollableTable" style="min-width:80%">
                <thead>

                <tr>
                    <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                    theme="simple"/></th>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Application
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_DOMAIN"/>" class="sort">Domain
                    </th>
                    <s:if test="showGears==true">
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_CARTRIDGE_NM"/>"
                            class="sort">Cartridge
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_GEAR_GROUP_NM"/>"
                            class="sort">Gear Group
                        </th>
                    </s:if>

                </tr>
                </thead>
                <tbody>

                <s:iterator var="system" value="sortedSet.itemList" status="stat">
                    <tr>

                        <td>
                            <s:checkboxlist name="systemSelectId" list="#{id:''}" cssClass="systemSelect"
                                            theme="simple"/>
                        </td>
                        <td>
                            <s:property value="appNm"/>
                        </td>
                        <td><s:property value="user"/></td>
                        <td><s:property value="host"/></td>
                        <td><s:property value="domain"/></td>
                        <s:if test="showGears==true">
                            <td><s:property value="cartridgeNm"/></td>
                            <td><s:property value="gearGroupNm"/></td>
                        </s:if>
                    </tr>

                </s:iterator>
                </tbody>
            </table>
        </s:form>
    </s:if>

    <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
        <div class="btn btn-default select_frm_btn spacer spacer-bottom">Create SSH Terminals</div>
    </s:if>
    <s:else>
        <div class="actionMessage">
            <p class="error">Systems not found 
            </p>
        </div>
    </s:else>
</div>


</body>
</html>
