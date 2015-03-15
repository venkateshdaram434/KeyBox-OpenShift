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
package com.keybox.common.db;

import com.keybox.common.util.AppConfig;
import com.keybox.manage.model.Auth;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.SSHUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initial startup task.  Creates an H2 DB. 
 */
@WebServlet(name = "DBInitServlet",
        urlPatterns = {"/config"},
        loadOnStartup = 1)
public class DBInitServlet extends javax.servlet.http.HttpServlet {

    /**
     * task init method that created DB and generated public/private keys
     *
     * @param config task config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);


        Connection connection = null;
        Statement statement = null;
        try {
            connection = DBUtils.getConn();
            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from information_schema.tables where upper(table_name) = 'USERS' and table_schema='PUBLIC'");
            if (rs == null || !rs.next()) {

                statement.executeUpdate("create table if not exists users (id INTEGER PRIMARY KEY AUTO_INCREMENT,  openshift_id varchar not null, auth_token varchar, otp_secret varchar)");
                statement.executeUpdate("create table if not exists user_theme (user_id INTEGER PRIMARY KEY, bg varchar(7), fg varchar(7), d1 varchar(7), d2 varchar(7), d3 varchar(7), d4 varchar(7), d5 varchar(7), d6 varchar(7), d7 varchar(7), d8 varchar(7), b1 varchar(7), b2 varchar(7), b3 varchar(7), b4 varchar(7), b5 varchar(7), b6 varchar(7), b7 varchar(7), b8 varchar(7), foreign key (user_id) references users(id) on delete cascade) ");
                statement.executeUpdate("create table if not exists system (id INTEGER PRIMARY KEY AUTO_INCREMENT, app_nm varchar, user varchar not null, host varchar not null, port INTEGER not null, domain varchar not null, gear_group_nm varchar, cartridge_nm varchar, user_id INTEGER, foreign key (user_id) references users(id) on delete cascade )");
                statement.executeUpdate("create table if not exists application_key (id INTEGER PRIMARY KEY AUTO_INCREMENT, public_key varchar not null, private_key varchar not null, passphrase varchar, key_type varchar, user_id INTEGER, foreign key (user_id) references users(id) on delete cascade )");
                statement.executeUpdate("create table if not exists status (id INTEGER, user_id INTEGER, status_cd varchar not null default 'INITIAL', primary key (id, user_id), foreign key (id) references system(id) on delete cascade, foreign key (user_id) references users(id) on delete cascade)");

            }

            DBUtils.closeRs(rs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        DBUtils.closeStmt(statement);
        DBUtils.closeConn(connection);
    }

}
