/**
 * (c) 2003-2017 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.mule.cassandradb.utils;

public class Constants {

    public final static String REPLICATION_FACTOR = "replication_factor";
    public final static String CLASS = "class";
    public static final String COLUMNS = "columns";
    public static final String WHERE = "where";
    public static final String PRIMARY_KEY = "primaryKey";
    public static final String CLUSTER_NAME = "newClusterName";

    //configuration
    public final static String CASS_HOST = "config.host";
    public final static String CASS_PORT = "config.port";
    public final static String KEYSPACE_NAME = "config.keyspace";

    //query specific constants
    public static final String PARAM_HOLDER = "?";
    public static final String SELECT = "SELECT";

}
