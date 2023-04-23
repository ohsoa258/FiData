package com.fisk.common.core.constants;

/**
 * @author Lock
 */
public class MqConstants {

    public static final class ExchangeConstants {

        public static final String TASK_EXCHANGE_NAME = "fk.task.exchange";

    }

    /*
     * topic前缀
     * */
    public static final class TopicPrefix {

        public static final String TOPIC_PREFIX = "dmp.datafactory.nifi.";

    }

    /**
     * topic消费者组id
     */
    public static final class TopicGroupId{

        /**
         * task模块kafka消费者组名称
         */
        public static final String TASK_GROUP_ID = "test";
    }

    public static final class QueueConstants {

        public static final class MetaDataTopicConstants{

            public static final String BUILD_ATLAS_INSTANCE_FLOW = "task.build.atlas.instance.flow";

            public static final String BUILD_ATLAS_TABLECOLUMN_FLOW = "task.build.atlas.tablecolumn.flow";

            public static final String BUILD_ATLAS_ENTITYDELETE_FLOW = "task.build.atlas.entitydelete.flow";

            public static final String BUILD_METADATA_FIELD_FLOW = "task.build.metadat.fielddelete.flow";
            public static final String BUILD_METADATA_FLOW = "task.build.metadata.flow";
        }

        public static final class NifiTopicConstants{

            public static final String BUILD_NIFI_FLOW = "task.build.nifi.flow";

            public static final String BUILD_IMMEDIATELYSTART_FLOW = "task.build.immediatelyStart.flow";
        }

        public static final class PipleTopicConstants{

            public static final String PIPELINE_SUPERVISION = "pipeline.supervision";
        }

        public static final class DataInputTopicConstants{

            public static final String BUILD_DATAINPUT_PGSQL_TABLE_FLOW = "task.build.datainput.pgsql.table.flow";

            public static final String BUILD_DATAINPUT_PGSQL_STGTOODS_FLOW = "task.build.datainput.pgsql.stgtoods.flow";

            public static final String BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW = "task.build.datainput.delete.pgsql.table.flow";
        }

        public static final class MdmTopicConstants{
            public static final String BUILD_MDM_APPROVAL_DATA = "task.build.mdm.approval";
            public static final String BUILD_MDM_MODEL_DATA = "task.build.mdm.model";

            public static final String BUILD_MDM_ENTITY_DATA = "task.build.mdm.entity";

            public static final String BUILD_DATAMODEL_DORIS_TABLE = "task.build.datamodel.doris.table.flow";
        }

        public static final class DataServiceTopicConstants{

            public static final String BUILD_TABLE_SERVER_FLOW = "task.build.table.server.flow";

            public static final String BUILD_DELETE_TABLE_SERVER_FLOW = "task.build.delete.table.server.flow";

            public static final String BUILD_CUSTOMWORK_FLOW = "task.build.customwork.flow";
        }

        public static final class DorisTopicConstants{

            public static final String BUILD_DORIS_FLOW = "task.build.doris.flow";

            public static final String BUILD_DORIS_INCREMENTAL_FLOW = "task.build.doris.incremental.flow";
        }

        public static final class OlapTopicConstants{

            public static final String BUILD_OLAP_CREATEMODEL_FLOW = "task.build.olap.createmodel.flow";

            public static final String BUILD_OLAP_WIDE_TABLE_FLOW = "task.build.olap.wide.table.flow";
        }

        public static final class GovernanceTopicConstants{

            public static final String BUILD_GOVERNANCE_TEMPLATE_FLOW = "task.build.governance.template.flow";
        }

        public static final class DataSecurityTopicConstants{

            public static final String BUILD_DATA_SECURITY_INTELLIGENT_DISCOVERY_FLOW = "task.build.security.intelligent.discovery.flow";
        }

        public static final class DispatchTopicConstants{

            public static final String BUILD_TASK_BUILD_NIFI_DISPATCH_FLOW = "task.build.nifi.dispatch.flow";
        }

        public static final String TASK_PUBLIC_CENTER_TOPIC_NAME = "my-topic";

        public static final String BUILD_ACCESS_API_FLOW = "build.access.api.flow";

        public static final String BUILD_EXEC_SCRIPT_FLOW = "build.exec.script.flow";

        /**
         * build.sftpfile.copy.flow
         */
        public static final String BUILD_SFTP_FILE_COPY_FLOW = "build.sftpFile.copy";
        /**
         *powerbi刷新数据集任务
         */
        public static final String BUILD_POWERBI_DATA_SET_REFRESH_FLOW = "build.powerbi.data.set.refresh.flow";
        /**
         * task.build.task.over
         */
        public static final String BUILD_TASK_OVER_FLOW = "task.build.task.over";

        /**
         * task.build.task.publish
         */
        public static final String BUILD_TASK_PUBLISH_FLOW = "task.build.task.publish";

    }

    /**
     * 任务发布中心,流组件触发topic所在地
     */
    public static final class FlowQueueConstants {
//






    }


    public static final class RouterConstants {
        public static final String INCREMENT_RESULT = "task.data.increment.#";
        public static final String TASK_BUILD_NIFI_ROUTER = "task.build.nifi.#";
        public static final String TASK_BUILD_ATLAS_INSTANCE_ROUTER = "task.build.atlas.instance.#";
        public static final String TASK_BUILD_ATLAS_TABLECOLUMN_ROUTER = "task.build.atlas.tablecolumn.#";
        public static final String TASK_BUILD_ATLAS_ENTITYDELETE_ROUTER = "task.build.atlas.entitydelete.#";
        public static final String TASK_BUILD_DORIS_ROUTER = "task.build.doris.#";
        public static final String TASK_BUILD_DATAMODEL_DORIS_TABLE_ROUTER = "task.build.datamodel.doris.table.#";
        public static final String TASK_BUILD_DORIS_INCREMENTAL_ROUTER = "task.build.doris.incremental.#";
        public static final String TASK_BUILD_DATAINPUT_PGSQL_TABLE_ROUTER = "task.build.datainput.pgsql.table.#";
        public static final String TASK_BUILD_DATAINPUT_PGSQL_STGTOODS_ROUTER = "task.build.datainput.pgsql.stgtoods.#";
        public static final String TASK_BUILD_DATAINPUT_DELETE_PGSQL_TABLE_ROUTER = "task.build.datainput.delete.pgsql.table.#";
        public static final String TASK_BUILD_OLAP_CREATEMODEL_ROUTER = "task.build.olap.createmodel.#";
        public static final String TASK_BUILD_CUSTOMWORK_ROUTER = "task.build.customwork.#";
    }
}