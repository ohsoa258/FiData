package com.fisk.common.constants;


public abstract class MQConstants {

    public static final class ExchangeConstants {

        public static final String TASK_EXCHANGE_NAME = "fk.task.exchange";

    }

    public static final class QueueConstants{

        public static final String BUILD_NIFI_FLOW = "task.build.nifi.flow";

        public static final String BUILD_ATLAS_FLOW = "task.build.atlas.flow";

    }

    public static final class RouterConstants{

        public static final String TASK_BUILD_NIFI_ROUTER = "task.build.nifi.#";
        public static final String TASK_BUILD_ATLAS_ROUTER = "task.build.atlas.#";

    }
}