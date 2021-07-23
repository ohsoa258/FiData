package com.fisk.common.constants;

/**
 * @author Lock
 */
public class MqConstants {

    public static final class ExchangeConstants {

        public static final String TASK_EXCHANGE_NAME = "fk.task.exchange";

    }

    public static final class QueueConstants{

        public static final String BUILD_NIFI_FLOW = "task.build.nifi.flow";

        public static final String BUILD_ATLAS_INSTANCE_FLOW = "task.build.atlas.instance.flow";

        public static final String BUILD_ATLAS_TABLECOLUMN_FLOW = "task.build.atlas.tablecolumn.flow";

        public static final String BUILD_DORIS_FLOW= "task.build.doris.flow";

        public static final String BUILD_ATLAS_ENTITYDELETE_FLOW="task.build.atlas.entitydelete.flow";

    }

    public static final class RouterConstants{

        public static final String TASK_BUILD_NIFI_ROUTER = "task.build.nifi.#";
        public static final String TASK_BUILD_ATLAS_INSTANCE_ROUTER = "task.build.atlas.instance.#";
        public static final String TASK_BUILD_ATLAS_TABLECOLUMN_ROUTER = "task.build.atlas.tablecolumn.#";
        public static final String TASK_BUILD_ATLAS_ENTITYDELETE_ROUTER = "task.build.atlas.entitydelete.#";
        public static final String TASK_BUILD_DORIS_ROUTER = "task.build.doris.#";

    }
}