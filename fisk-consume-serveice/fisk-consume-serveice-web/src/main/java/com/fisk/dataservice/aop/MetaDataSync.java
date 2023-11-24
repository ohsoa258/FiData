package com.fisk.dataservice.aop;

import com.fisk.common.core.enums.datamanage.DataOperationTypeEnum;
import com.fisk.common.core.enums.datamanage.MetaDataSyncTypeEnum;
import com.fisk.common.framework.mdc.TraceTypeEnum;

import java.lang.annotation.*;

/**
 * @author jx
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MetaDataSync {
    TraceTypeEnum type() default TraceTypeEnum.UNKNOWN;


    /**
     * 元数据同步类型
     * @return
     */
    MetaDataSyncTypeEnum syncType() default MetaDataSyncTypeEnum.DATA_CONSUME_API;

    /**
     * 弹框:默认1添加,2修改,3删除
     * @return
     */
    DataOperationTypeEnum operationType() default DataOperationTypeEnum.ADD;
}