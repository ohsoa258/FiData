package com.fisk.datagovernance.vo.dataquality.businessfilter.filterresult;

import com.fisk.datagovernance.enums.dataquality.ProcessAssemblyTypeEnum;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗过程VO
 * @date 2023/4/3 13:40
 */
@Data
public class BusinessFilterProcessVO
{
    /**
     * 组件类型
     */
    public ProcessAssemblyTypeEnum processAssemblyTypeEnum;

    /**
     * 任务code
     */
    public String taskCode;

    /**
     * 清洗sql
     */
    public String sql;
}
