package com.fisk.common.core.constants;

/**
 * @author gy
 */
public class NifiConstants {

    public static final class enableAuthentication {
        public static final String ENABLE = "1";
        public static final String NOT_ENABLE = "0";
    }

    public static final class ApiConstants {

        public static final String BASE_PATH = "http://192.168.11.130:9090/nifi-api";

        public static final String ROOT_NODE = "root";

        public static final String PROCESSOR_RUN_STATUS = "/processors/{id}/run-status";

        public static final String ALL_GROUP_RUN_STATUS = "/process-groups/{id}/process-groups";

        public static final String PUTPROCESS = "/processors/{id}";

        public static final String CREATE_INPUT_PORT = "/process-groups/{id}/input-ports";
        public static final String CREATE_OUTPUT_PORT = "/process-groups/{id}/output-ports";
        public static final String CREATE_CONNECTIONS = "/process-groups/{id}/connections";
        public static final String INPUT = "/input-ports/{id}";

        public static final String EMPTY_ALL_CONNECTIONS_REQUESTS="/process-groups/{id}/empty-all-connections-requests";

        public static final String CONTROLLER_SERVICES_RUN_STATUS="/controller-services/{id}/run-status";

    }

    public static final class DriveConstants {
        public static final String MYSQL_DRIVE_PATH = "opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar";
        public static final String SQLSERVER_DRIVE_PATH = "opt/nifi/nifi-current/jdbcdriver/sqljdbc42.jar";
        public static final String POSTGRESQL_DRIVE_PATH = "opt/nifi/nifi-current/jdbcdriver/postgresql-42.2.23.jar";
        public static final String ORACLE_DRIVE_PATH = "opt/nifi/nifi-current/jdbcdriver/ojdbc7.jar";

    }

    public static final class AttrConstants {

        public static final String INCREMENT_DB_TABLE_NAME = "tb_etl_Incremental";
        public static final String INCREMENT_DB_FIELD_START = "incremental_objectiveScore_start";
        public static final String INCREMENT_START = "IncrementStart";
        public static final String INCREMENT_DB_FIELD_END = "incremental_objectivescore_end";
        public static final String INCREMENT_END = "IncrementEnd";
        public static final String LOG_CODE = "uuids";
        public static final String NUMBERS = "numbers";
        public static final String CREATE_TIME="createtime";
        public static final String START_TIME="start_time";
        public static final String END_TIME="end_time";
        public static final String TABLE_ID="table_id";
        public static final String TABLE_TYPE="table_type";
        public static final String FIDATA_BATCH_CODE="fidata_batch_code";
        public static final String KAFKA_TOPIC="kafka.topic";
        public static final String INITIAL_TIME="1753-01-01 00:00:00";

        public static final String PIPEL_TRACE_ID="pipelTraceId";
        public static final String PIPEL_JOB_TRACE_ID="pipelJobTraceId";
        public static final String PIPEL_TASK_TRACE_ID="pipelTaskTraceId";
        public static final String PIPEL_STAGE_TRACE_ID="pipelStageTraceId";
        public static final String INCREMENTAL_OBJECTIVESCORE_END="incremental_objectivescore_end";
        public static final String INCREMENTAL_OBJECTIVESCORE_START="incremental_objectivescore_start";
        public static final String TOPIC_TYPE="topicType";

        public static final int POSITION_X_MAX = 5;
        public static final double POSITION_X = 500;
        public static final double POSITION_Y = 300;
    }

    public static final class PortConstants {
        public static final String INPUT_PORT_NAME = "_input_port";
        public static final String OUTPUT_PORT_NAME = "_output_port";
        public static final Double INPUT_PORT_Y = -250d;
        public static final Double OUTPUT_PORT_Y = 250d;
        public static final Double INPUT_PORT_OFFSET_Y = -25d;
        public static final String PORT_NAME_APP_SUFFIX = "_app";
        public static final String PORT_NAME_TABLE_SUFFIX = "_table";
        public static final String PORT_NAME_FIELD_SUFFIX = "_field";
    }
}
