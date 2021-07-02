package com.fisk.common.constants;

/**
 * @author gy
 */
public class NifiConstants {

    public static final class ApiConstants {

        public static final String BASE_PATH = "http://192.168.11.130:9090/nifi-api";

        public static final String ROOT_NODE = "root";

        public static final String PROCESSOR_RUN_STATUS = "/processors/{id}/run-status";

        public static final String ALL_GROUP_RUN_STATUS = "/process-groups/{id}/process-groups";

    }

    public static final class DirverConstants {
        public static final String MYSQL_DIRVER_PATH = "/opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar";
    }

    public static final class AttrConstants {

        public static final String INCREMENT_NAME = "Increment";

        public static final int POSITION_X_MAX = 5;
        public static final double POSITION_X = 500;
        public static final double POSITION_Y = 300;
    }
}
