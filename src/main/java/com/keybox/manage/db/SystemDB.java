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
    public static final String SORT_BY_DOMAIN = "domain";
    public static final String SORT_BY_GEAR_GROUP_NM = "gear_group_nm";
    public static final String SORT_BY_CARTRIDGE_NM = "cartridge_nm";


    /**
     * method to do order by based on the sorted set object for systems
     *
     * @param sortedSet sorted set object
     * @param filter    name value pair to filter by
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet, Map<String, String> filter, Long userId) {

        Connection con = null;

        try {
            con = DBUtils.getConn();

            sortedSet = getSystemSet(con, sortedSet, filter, userId);


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return sortedSet;
    }
    
    /**
     * method to do order by based on the sorted set object for systems
     *
     * @param con DB Connection
     * @param sortedSet sorted set object
     * @param filter    name value pair to filter by
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(Connection con, SortedSet sortedSet, Map<String, String> filter, Long userId) {
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from  system where ";

        //append filter
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            sql = sql + entry.getKey() + " like ? and ";
        }

        //append user id
        sql = sql + " user_id=? " + orderBy;

        try {
            PreparedStatement stmt = con.prepareStatement(sql);

            //set the values for the filter
            int i = 1;
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
                hostSystem.setGearGroupNm(rs.getString("gear_group_nm"));
                hostSystem.setCartridgeNm(rs.getString("cartridge_nm"));
                hostSystemList.add(hostSystem);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        sortedSet.setItemList(hostSystemList);
        return sortedSet;

    }


    /**
     * returns system by id
     *
     * @param id     system id
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
     * @param con    DB connection
     * @param id     system id
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
                hostSystem.setGearGroupNm(rs.getString("gear_group_nm"));
                hostSystem.setCartridgeNm(rs.getString("cartridge_nm"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hostSystem;
    }

    /**
     * sets host system in DB from list
     *
     * @param hostSystemList host system object
     * @param userId user id                      
     */
    public static void setSystem(List<HostSystem> hostSystemList, Long userId) {
        Connection con = null;
        try {
            con = DBUtils.getConn();

            deleteSystems(con, userId);
            for(HostSystem hostSystem: hostSystemList){
                insertSystem(con, hostSystem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }
    

    /**
     * inserts host system into DB
     *
     * @param con DB connection
     * @param hostSystem host system object
     * @return host system id
     */
    public static Long insertSystem(Connection con, HostSystem hostSystem) {

        Long systemId = null;
        try {
            PreparedStatement stmt = con.prepareStatement("insert into system (app_nm, user, host, port, domain, gear_group_nm, cartridge_nm, user_id) values (?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, hostSystem.getAppNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getDomain());
            stmt.setString(6, hostSystem.getGearGroupNm());
            stmt.setString(7, hostSystem.getCartridgeNm());
            stmt.setLong(8, hostSystem.getUserId());
            stmt.execute();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                systemId = rs.getLong(1);
            }
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return systemId;

    }

    /**
     * updates host system record
     *
     * @param con DB connection
     * @param hostSystem host system object
     */
    public static void updateSystem(Connection con, HostSystem hostSystem) {

        try {
            PreparedStatement stmt = con.prepareStatement("update system set app_nm=?, user=?, host=?, port=?, domain=?, gear_group_nm=?, cartridge_nm=?, user_id=?  where id=?");
            stmt.setString(1, hostSystem.getAppNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getDomain());
            stmt.setString(6, hostSystem.getGearGroupNm());
            stmt.setString(7, hostSystem.getCartridgeNm());
            stmt.setLong(8, hostSystem.getUserId());
            stmt.setLong(9, hostSystem.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

    } 

    /**
     * deletes host system
     *
     * @param con DB connection
     * @param userId host system id
     */
    public static void deleteSystems(Connection con, Long userId) {

        try {
            PreparedStatement stmt = con.prepareStatement("delete from system where user_id=?");
            stmt.setLong(1, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public static List<String> getDomains(Connection con, Long userId) {

        List<String> domainList = new ArrayList<String>();


        try {
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

        return domainList;
    }


    public static List<String> getAppNms(Connection con, Long userId) {

        List<String> appNmList = new ArrayList<String>();


        try {
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

        return appNmList;
    }

    public static List<String> getGearNms(Connection con, Long userId) {

        List<String> gearGroupList = new ArrayList<String>();


        try {
            PreparedStatement stmt = con.prepareStatement("select distinct gear_group_nm from system where user_id=?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                gearGroupList.add(rs.getString("gear_group_nm"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return gearGroupList;
    }

    public static List<String> getCartridgeNms(Connection con, Long userId) {

        List<String> cartridgeList = new ArrayList<String>();

        try {
            PreparedStatement stmt = con.prepareStatement("select distinct cartridge_nm from system where user_id=?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cartridgeList.add(rs.getString("cartridge_nm"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cartridgeList;
    }
}
