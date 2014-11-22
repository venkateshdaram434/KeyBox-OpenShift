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

import com.keybox.manage.model.Auth;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DAO to login administrative users
 */
public class AuthDB {

    /**
     * auth user and return auth object with user id
     *
     * @param auth user object
     * @return auth user object if success
     */
    public static Auth setAuth(Auth auth) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            Auth user = getUserByOpenShiftId(con, auth.getOpenshiftId());

            if (user != null && user.getId() != null) {
                auth.setId(user.getId());
                updateLogin(con, auth);
            } else {
                auth.setId(insertLogin(con, auth));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return auth;


    }

    /**
     * checks to see if user is an admin based on auth token
     *
     * @param authToken auth token string
     * @return boolean if authorized
     */
    public static boolean isAuthorized(String authToken) {

        boolean authorized = false;

        Connection con = null;
        if (authToken != null && !authToken.trim().equals("")) {

            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement("select * from users where auth_token=?");
                stmt.setString(1, authToken);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    authorized = true;

                }
                DBUtils.closeRs(rs);

                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DBUtils.closeConn(con);
        return authorized;


    }



    /**
     * updates the admin table based on auth id
     *
     * @param con  DB connection
     * @param auth user object
     */
    private static void updateLogin(Connection con, Auth auth) {


        try {
            PreparedStatement stmt = con.prepareStatement("update users set openshift_id=?, auth_token=? where id=?");
            stmt.setString(1, auth.getOpenshiftId());
            stmt.setString(2, auth.getAuthToken());
            stmt.setLong(3, auth.getId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * inserts into the admin table based
     *
     * @param con  DB connection
     * @param auth user object
     */
    private static Long insertLogin(Connection con, Auth auth) {

       Long id=null;

        try {
            PreparedStatement stmt = con.prepareStatement("insert into users (openshift_id, auth_token) values (?,?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, auth.getOpenshiftId());
            stmt.setString(2, auth.getAuthToken());
            stmt.execute();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs != null && rs.next()) {
                id= rs.getLong(1);
            }
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;

    }


    /**
     * returns user id based on auth token
     *
     * @param con       DB connection
     * @param authToken auth token
     * @return user id
     */
    public static Long getUserIdByAuthToken(Connection con, String authToken) {


        Long userId = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ?");
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userId = rs.getLong("id");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return userId;

    }

    /**
     * returns user id based on auth token
     *
     * @param authToken auth token
     * @return user id
     */
    public static Long getUserIdByAuthToken(String authToken) {

        Long userId = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            userId = getUserIdByAuthToken(con, authToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return userId;

    }

    /**
     * returns auth object based on openshift id
     *
     * @param con         DB connection
     * @param openShiftId user openshift id
     * @return auth object
     */
    public static Auth getUserByOpenShiftId(Connection con, String openShiftId) {
        Auth user = null;
        try {
            if (StringUtils.isNotEmpty(openShiftId)) {

                PreparedStatement stmt = con.prepareStatement("select * from users where lower(openshift_id) like ?");
                stmt.setString(1, openShiftId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    user = new Auth();
                    user.setId(rs.getLong("id"));
                    user.setAuthToken(rs.getString("auth_token"));
                    user.setOpenshiftId(rs.getString("openshift_id"));
                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;

    }


    /**
     * returns the shared secret based on openshift id
     *
     * @param  openshiftId openshift id
     * @return auth object
     */
    public static String getSharedSecret(String openshiftId) {

        String sharedSecret = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from users where openshift_id like ?");
            stmt.setString(1, openshiftId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sharedSecret = EncryptionUtil.decrypt(rs.getString("otp_secret"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return sharedSecret;

    }

    /**
     * updates shared secret based on auth token
     *
     * @param secret OTP shared secret
     * @param authToken auth token
     */
    public static void updateSharedSecret(String secret, String authToken) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set otp_secret=? where auth_token=?");
            stmt.setString(1, EncryptionUtil.encrypt(secret));
            stmt.setString(2, authToken);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * returns auth user object based on auth token
     *
     * @param con DB connection
     * @return user id
     */
    public static Auth getUserByAuthToken(Connection con, String authToken) {


        Auth user = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ?");
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = new Auth();
                user.setId(rs.getLong("id"));
                user.setAuthToken(rs.getString("auth_token"));
                user.setOpenshiftId(rs.getString("openshift_id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return user;

    }

    /**
     * returns auth user object based on auth token
     *
     * @param authToken auth token
     * @return Auth user object
     */
    public static Auth getUserByAuthToken(String authToken) {

        Auth user = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            user = getUserByAuthToken(con, authToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return user;

    }
}
