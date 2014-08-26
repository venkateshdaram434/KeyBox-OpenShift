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
package com.keybox.manage.db;

import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * DAO used to manage systems
 */
public class SystemDB {

    public static final String SORT_BY_NAME = "app_nm";
    public static final String SORT_BY_USER = "user";
    public static final String SORT_BY_HOST = "host";
    public static final String SORT_BY_DOMAIN="domain";

    /**
     * method to do order by based on the sorted set object for systems
     *
     * @param sortedSet sorted set object
     * @param filter name value pair to filter by
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet, Map<String,String> filter, Long userId) {
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from  system where ";

        //append filter
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            sql=sql + entry.getKey() + " like ? and ";
        }

        //append user id
        sql=sql+" user_id=? " + orderBy;


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);

            //set the values for the filter
            int i=1;
            for (Map.Entry<String, String> entry : filter.entrySet()) {
                stmt.setString(i++, entry.getValue());
            }
            stmt.setLong(i, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HostSystem hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setAppNm(rs.getString("app_nm"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setDomain(rs.getString("domain"));
                hostSystemList.add(hostSystem);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        sortedSet.setItemList(hostSystemList);
        return sortedSet;

    }


    /**
     * returns system by id
     *
     * @param id system id
     * @param userId user id
     * @return system
     */
    public static HostSystem getSystem(Long id, Long userId) {

        HostSystem hostSystem = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            getSystem(con, id, userId);


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystem;
    }


    /**
     * returns system by id
     *
     * @param con DB connection
     * @param id  system id
     * @param userId user id
     * @return system
     */
    public static HostSystem getSystem(Connection con, Long id, Long userId) {

        HostSystem hostSystem = null;


        try {

            PreparedStatement stmt = con.prepareStatement("select * from  system where id=? and user_id=?");
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setAppNm(rs.getString("app_nm"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setDomain(rs.getString("domain"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hostSystem;
    }


    /**
     * inserts host system into DB
     *
     * @param hostSystem host system object
     * @return user id
     */
    public static Long insertSystem(HostSystem hostSystem) {


        Connection con = null;

        Long userId = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("insert into system (app_nm, user, host, port, domain, user_id) values (?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, hostSystem.getAppNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getDomain());
            stmt.setLong(6, hostSystem.getUserId());
            stmt.execute();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                userId = rs.getLong(1);
            }
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return userId;

    }

    /**
     * updates host system record
     *
     * @param hostSystem host system object
     */
    public static void updateSystem(HostSystem hostSystem) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("update system set app_nm=?, user=?, host=?, port=?, domain=?, user_id=?  where id=?");
            stmt.setString(1, hostSystem.getAppNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getDomain());
            stmt.setLong(6, hostSystem.getUserId());
            stmt.setLong(7, hostSystem.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * deletes host system
     *
     * @param userId host system id
     */
    public static void deleteSystems(Long userId) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("delete from system where user_id=?");
            stmt.setLong(1, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }



    /**
     * method to check system permissions for user
     *
     * @param systemSelectIdList list of system ids to check
     * @param userId             user id
     * @return only system ids that user has perms for
     */
    public static List<Long> checkSystemPerms(List<Long> systemSelectIdList, Long userId) {

        List<Long> systemIdList = new ArrayList<Long>();
        if (systemSelectIdList != null && !systemSelectIdList.isEmpty()) {


            //get user for auth token
            Connection con = null;
            try {
                con = DBUtils.getConn();
                String sql = "select * from system where user_id=? ";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setLong(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Long systemId = rs.getLong("id");
                    if (systemSelectIdList.contains(systemId)) {
                        systemIdList.add(systemId);
                    }
                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
            DBUtils.closeConn(con);

        }

        return systemIdList;

    }



    public static List<String> getDomains(Long userId){

        List<String> domainList = new ArrayList<String>();



        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select distinct domain from system where user_id=?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
               domainList.add(rs.getString("domain"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);



        return domainList;
    }

    public static List<String> getAppNms(Long userId){

        List<String> appNmList = new ArrayList<String>();



        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select distinct app_nm from system where user_id=?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appNmList.add(rs.getString("app_nm"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);



        return appNmList;
    }
}
