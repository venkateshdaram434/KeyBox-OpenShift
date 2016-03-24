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
package com.keybox.manage.util;

import com.jcraft.jsch.*;
import com.keybox.common.util.AppConfig;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.*;
import com.keybox.manage.task.SecureShellTask;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * SSH utility class used to create public/private key for system and distribute authorized key files
 */
public class SSHUtil {



    //key type - rsa or dsa
    public static final String KEY_TYPE = AppConfig.getProperty("sshKeyType");

    //private key name
    public static final String SSH_KEY = OpenShiftUtils.DATA_DIR + "id_" + KEY_TYPE;


    public static final int SESSION_TIMEOUT = 60000;
    public static final int CHANNEL_TIMEOUT = 60000;

    /**
     * returns the system's public key
     *
     * @param userId user id for SSH key
     * @return system's public key
     */
    public static String getPublicKey(Long userId) {

        String publicKey = SSH_KEY + "_" + userId + ".pub";
        //read pub ssh key
        File file = new File(publicKey);
        try {
            publicKey = FileUtils.readFileToString(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return publicKey;
    }


    /**
     * returns the system's public key
     *
     * @param userId user id for SSH key
     * @return system's public key
     */
    public static String getPrivateKey(Long userId) {

        String privateKey = SSH_KEY + "_" + userId;

        //read pvt ssh key
        File file = new File(privateKey);
        try {
            privateKey = FileUtils.readFileToString(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return privateKey;
    }

    /**
     * generates system's public/private key par and returns passphrase
     *
     * @param userId user id for SSH key
     * @return passphrase for system generated key
     */
    public static String keyGen(Long userId) {
        //get passphrase cmd from properties file
        String passphrase = UUID.randomUUID().toString();


        return keyGen(passphrase, userId);

    }

    /**
     * delete SSH keys
     *
     * @param userId user id for SSH key
     */
    public static void deleteGenSSHKeys(Long userId) {

        String publicKey = SSH_KEY + "_" + userId + ".pub";
        deletePvtGenSSHKey(userId);
        //delete public key
        try {
            File file = new File(publicKey);
            FileUtils.forceDelete(file);
        } catch (Exception ex) {
        }
    }


    /**
     * delete SSH keys
     *
     * @param userId user id for SSH key
     */
    public static void deletePvtGenSSHKey(Long userId) {

        String privateKey = SSH_KEY + "_" + userId;
        //delete private key
        try {
            File file = new File(privateKey);
            FileUtils.forceDelete(file);
        } catch (Exception ex) {
        }


    }

    /**
     * generates system's public/private key par and returns passphrase
     *
     * @param userId user id for SSH key
     * @return passphrase for system generated key
     */
    public static String keyGen(String passphrase, Long userId) {

        deleteGenSSHKeys(userId);


        String privateKey = SSH_KEY + "_" + userId;
        String publicKey = SSH_KEY + "_" + userId + ".pub";

        //set key type
        int type = KEY_TYPE.equals("rsa") ? KeyPair.RSA : KeyPair.DSA;
        String comment = "KeyBox generated key pair";

        JSch jsch = new JSch();

        try {

            KeyPair keyPair = KeyPair.genKeyPair(jsch, type);

            keyPair.writePrivateKey(privateKey, passphrase.getBytes());
            keyPair.writePublicKey(publicKey, comment);
            keyPair.dispose();
        } catch (Exception e) {
            System.out.println(e);
        }


        return passphrase;


    }


    /**
     * return the next instance id based on ids defined in the session map
     *
     * @param userId        user id
     * @param userSessionMap user session map
     * @return
     */
    private static int getNextInstanceId(Long userId, Map<Long, UserSchSessions> userSessionMap ){

        Integer instanceId=1;
        if(userSessionMap.get(userId)!=null){

            for(Integer id :userSessionMap.get(userId).getSchSessionMap().keySet()) {
                if (!id.equals(instanceId) ) {

                    if(userSessionMap.get(userId).getSchSessionMap().get(instanceId) == null) {
                        return instanceId;
                    }
                }
                instanceId = instanceId + 1;
            }
        }
        return instanceId;

    }


    /**
     * open new ssh session on host system
     *
     * @param passphrase     key passphrase for instance
     * @param password       password for instance
     * @param userId         user id
     * @param hostSystem     host system
     * @param userSessionMap user session map
     * @return status of systems
     */
    public static HostSystem openSSHTermOnSystem(String passphrase, String password, Long userId, HostSystem hostSystem, Map<Long, UserSchSessions> userSessionMap) {

        JSch jsch = new JSch();

        int instanceId = getNextInstanceId(userId ,userSessionMap);
        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        hostSystem.setInstanceId(instanceId);

        SchSession schSession = null;

        try {
            ApplicationKey appKey = PrivateKeyDB.getApplicationKey(userId);
            //check to see if passphrase has been provided
            if (passphrase == null || passphrase.trim().equals("")) {
                passphrase = appKey.getPassphrase();
                //check for null inorder to use key without passphrase
                if (passphrase == null) {
                    passphrase = "";
                }
            }
            //add private key
            jsch.addIdentity(appKey.getId().toString(), appKey.getPrivateKey().trim().getBytes(), appKey.getPublicKey().getBytes(), passphrase.getBytes());

            //create session
            Session session = jsch.getSession(hostSystem.getUser(), hostSystem.getHost(), hostSystem.getPort());

            //set password if it exists
            if (password != null && !password.trim().equals("")) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("shell");
            if ("true".equals(AppConfig.getProperty("agentForwarding"))) {
                ((ChannelShell) channel).setAgentForwarding(true);
            }
            ((ChannelShell) channel).setPtyType("xterm");

            InputStream outFromChannel = channel.getInputStream();


            //new session output
            SessionOutput sessionOutput = new SessionOutput();
            sessionOutput.setHostSystemId(hostSystem.getId());
            sessionOutput.setInstanceId(instanceId);
            sessionOutput.setSessionId(userId);


            Runnable run = new SecureShellTask(sessionOutput, outFromChannel);
            Thread thread = new Thread(run);
            thread.start();


            OutputStream inputToChannel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputToChannel, true);


            channel.connect();

            schSession = new SchSession();
            schSession.setUserId(userId);
            schSession.setSession(session);
            schSession.setChannel(channel);
            schSession.setCommander(commander);
            schSession.setInputToChannel(inputToChannel);
            schSession.setOutFromChannel(outFromChannel);
            schSession.setHostSystem(hostSystem);



        } catch (Exception e) {
            hostSystem.setErrorMsg(e.getMessage());
            if (e.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("auth fail") || e.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystem.setStatusCd(HostSystem.AUTH_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("unknownhostexception")){
                hostSystem.setErrorMsg("DNS Lookup Failed");
                hostSystem.setStatusCd(HostSystem.HOST_FAIL_STATUS);
            } else {
                hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
            }
        }


        //add session to map
        if (hostSystem.getStatusCd().equals(HostSystem.SUCCESS_STATUS)) {
            //get the server maps for user
            UserSchSessions userSchSessions = userSessionMap.get(userId);

            //if no user session create a new one
            if (userSchSessions == null) {
                userSchSessions = new UserSchSessions();
            }
            Map<Integer , SchSession> schSessionMap = userSchSessions.getSchSessionMap();

            //add server information
            schSessionMap.put(instanceId, schSession);
            userSchSessions.setSchSessionMap(schSessionMap);
            //add back to map
            userSessionMap.put(userId, userSchSessions);
        }

        SystemStatusDB.updateSystemStatus(hostSystem, userId);

        return hostSystem;
    }


}
