package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

@Data
@TableName("tb_bizfilter_process_assembly")
public class BusinessFilter_ProcessAssemblyPO extends BasePO {
    /**
     * 组件名称
     */
    public String assemblyName;

    /**
     * 组件描述
     */
    public String assemblyDescribe;

    /**
     * 组件ICON
     */
    public String assemblyIcon;
}
