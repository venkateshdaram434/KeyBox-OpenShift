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

import com.keybox.manage.model.ApplicationKey;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.SSHUtil;
import com.openshift.client.SSHKeyType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO that returns public / private key for the system generated private key
 */
public class PrivateKeyDB {


    /**
     * returns public private key for application
     *
     * @param con    DB connection
     * @param userId user id
     * @return app key values
     */
    public static ApplicationKey getApplicationKey(Connection con, Long userId) {

        ApplicationKey appKey = null;


        try {

            PreparedStatement stmt = con.prepareStatement("select * from  application_key where user_id=?");
            stmt.setLong(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                appKey = new ApplicationKey();
                appKey.setId(rs.getLong("id"));
                appKey.setPassphrase(EncryptionUtil.decrypt(rs.getString("passphrase")));
                appKey.setPrivateKey(EncryptionUtil.decrypt(rs.getString("private_key")));
                appKey.setPublicKey(rs.getString("public_key"));
                appKey.setKeyType(rs.getString("key_type").equals("rsa") ? SSHKeyType.SSH_RSA : SSHKeyType.SSH_DSA);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return appKey;
    }


    /**
     * creates an application key for user
     *
     * @param userId user id
     * @return app key values
     */
    public static ApplicationKey createApplicationKey(Long userId) {


        Connection con = null;

        ApplicationKey appKey = null;
        try {
            con = DBUtils.getConn();
            //get application key
            appKey = getApplicationKey(con, userId);

            //generate new key and insert passphrase if no key exists
            if (appKey == null) {
                System.out.println("Setting KeyBox SSH public/private key pair");

                //generate application pub/pvt key and get values
                String passphrase = SSHUtil.keyGen(userId);
                String publicKey = SSHUtil.getPublicKey(userId);
                String privateKey = SSHUtil.getPrivateKey(userId);

                //insert new keys
                PreparedStatement stmt = con.prepareStatement("insert into application_key (public_key, private_key, passphrase, key_type, user_id) values(?,?,?,?,?)");
                stmt.setString(1, publicKey);
                stmt.setString(2, EncryptionUtil.encrypt(privateKey));
                stmt.setString(3, EncryptionUtil.encrypt(passphrase));
                stmt.setString(4, SSHUtil.KEY_TYPE);
                stmt.setLong(5, userId);
                stmt.execute();
                DBUtils.closeStmt(stmt);

                System.out.println("KeyBox Public Key:");
                System.out.println(publicKey);

                passphrase = null;
                publicKey = null;
                privateKey = null;

                //set application key
                appKey = getApplicationKey(con, userId);

                //delete generated ssh keys
                SSHUtil.deleteGenSSHKeys(userId);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return appKey;
    }


    /**
     * returns public private key for application
     *
     * @param userId user id
     * @return app key values
     */
    public static ApplicationKey getApplicationKey(Long userId) {

        ApplicationKey appKey = null;

        Connection con = null;

        try {

            con = DBUtils.getConn();
            appKey = getApplicationKey(con, userId);


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return appKey;
    }


}
