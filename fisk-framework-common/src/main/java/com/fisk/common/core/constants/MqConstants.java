package com.fisk.common.core.constants;

/**
 * @author Lock
 */
public class MqConstants {

    public static final class ExchangeConstants {

        public static final String TASK_EXCHANGE_NAME = "fk.task.exchange";

    }

    public static final class QueueConstants {

        public static final String INCREMENT_RESULT = "task.data.increment.result";

        public static final String BUILD_NIFI_FLOW = "task.build.nifi.flow";

        public static final String BUILD_ATLAS_INSTANCE_FLOW = "task.build.atlas.instance.flow";

        public static final String BUILD_ATLAS_TABLECOLUMN_FLOW = "task.build.atlas.tablecolumn.flow";

        public static final String BUILD_DORIS_FLOW = "task.build.doris.flow";

        public static final String BUILD_DORIS_INCREMENTAL_FLOW = "task.build.doris.incremental.flow";

        public static final String BUILD_ATLAS_ENTITYDELETE_FLOW = "task.build.atlas.entitydelete.flow";

        public static final String BUILD_DATAMODEL_DORIS_TABLE = "task.build.datamodel.doris.table.flow";

        public static final String BUILD_DATAINPUT_PGSQL_TABLE_FLOW = "task.build.datainput.pgsql.table.flow";

        public static final String BUILD_DATAINPUT_PGSQL_STGTOODS_FLOW = "task.build.datainput.pgsql.stgtoods.flow";

        public static final String BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW = "task.build.datainput.delete.pgsql.table.flow";

        public static final String BUILD_OLAP_CREATEMODEL_FLOW = "task.build.olap.createmodel.flow";

        public static final String BUILD_OLAP_WIDE_TABLE_FLOW = "task.build.olap.wide.table.flow";

        public static final String BUILD_CUSTOMWORK_FLOW = "task.build.customwork.flow";

        public static final String BUILD_IMMEDIATELYSTART_FLOW = "task.build.immediatelyStart.flow";

        public static final String BUILD_TASK_BUILD_NIFI_DISPATCH_FLOW = "task.build.nifi.dispatch.flow";

        public static final String BUILD_GOVERNANCE_FIELD_STRONG_RULE_TEMPLATE_FLOW = "task.build.governance.fieldStrongRule.template.flow";

        public static final String BUILD_GOVERNANCE_FIELD_AGGREGATE_THRESHOLD_TEMPLATE_FLOW = "task.build.governance.fieldAggregateThreshold.template.flow";

        public static final String BUILD_GOVERNANCE_ROWCOUNT_THRESHOLD_TEMPLATE_FLOW = "task.build.governance.rowCountThreshold.template.flow";

        public static final String BUILD_GOVERNANCE_EMPTY_TABLE_CHECK_TEMPLATE_FLOW = "task.build.governance.emptyTableCheck.template.flow";

        public static final String BUILD_GOVERNANCE_UPDATE_TABLE_CHECK_TEMPLATE_FLOW = "task.build.governance.updateTableCheck.template.flow";

        public static final String BUILD_GOVERNANCE_TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE_FLOW = "task.build.governance.tableBloodKinshipCheck.template.flow";

        public static final String BUILD_GOVERNANCE_BUSINESS_CHECK_TEMPLATE_FLOW = "task.build.governance.businessCheck.template.flow";

        public static final String BUILD_GOVERNANCE_SIMILARITY_TEMPLATE_FLOW = "task.build.governance.similarity.template.flow";

        public static final String BUILD_GOVERNANCE_BUSINESS_FILTER_TEMPLATE_FLOW = "task.build.governance.businessFilter.template.flow";

        public static final String BUILD_GOVERNANCE_SPECIFY_TIME_RECYCLING_TEMPLATE_FLOW = "task.build.governance.specifyTimeRecycling.template.flow";

        public static final String BUILD_GOVERNANCE_EMPTY_TABLE_RECOVERY_TEMPLATE_FLOW = "task.build.governance.emptyTableRecovery.template.flow";

        public static final String BUILD_GOVERNANCE_NO_REFRESH_DATA_RECOVERY_TEMPLATE_FLOW = "task.build.governance.noRefreshDataRecovery.template.flow";

        public static final String BUILD_GOVERNANCE_DATA_BLOOD_KINSHIP_RECOVERY_TEMPLATE_FLOW = "task.build.governance.dataBloodKinshipRecovery.template.flow";

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