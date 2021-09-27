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

        public static final String PUTPROCESS="/processors/{id}";

        public static final String CREATE_INPUT_PORT="/process-groups/{id}/input-ports";
        public static final String INPUT="/input-ports/{id}";

    }

    public static final class DriveConstants {
        public static final String MYSQL_DRIVE_PATH = "/opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar";
        public static final String SQLSERVER_DRIVE_PATH = "/opt/nifi/nifi-current/jdbcdriver/sqljdbc42.jar";
        public static final String POSTGRESQL_DRIVE_PATH="/opt/nifi/nifi-current/jdbcdriver/postgresql-42.2.23.jar";

    }

    public static final class AttrConstants {

        public static final String INCREMENT_DB_TABLE_NAME = "tb_etl_Incremental";
        public static final String INCREMENT_DB_FIELD_START = "Incremental_ObjectiveScore_Start";
        public static final String INCREMENT_START = "IncrementStart";
        public static final String INCREMENT_DB_FIELD_END = "Incremental_ObjectiveScore_End";
        public static final String INCREMENT_END = "IncrementEnd";
        public static final String LOG_CODE = "uuids";
        public static final String NUMBERS = "numbers";

        public static final int POSITION_X_MAX = 5;
        public static final double POSITION_X = 500;
        public static final double POSITION_Y = 300;
    }
}
