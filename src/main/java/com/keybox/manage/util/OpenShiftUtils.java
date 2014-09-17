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


public class OpenShiftUtils {

    public static final String LIBRA_SERVER=System.getenv("OPENSHIFT_BROKER_HOST") != null ? System.getenv("OPENSHIFT_BROKER_HOST") : "openshift.redhat.com";
    public static final String DATA_DIR=System.getenv("OPENSHIFT_DATA_DIR");
    public static final String APP_DNS=System.getenv("OPENSHIFT_APP_DNS");
    public static final String NAMESPACE=System.getenv("OPENSHIFT_NAMESPACE");
    public static final String APP_NAME=System.getenv("OPENSHIFT_APP_NAME");
    public static final String CLIENT_NAME=APP_NAME+"-"+NAMESPACE;
}
