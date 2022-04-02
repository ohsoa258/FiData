package com.fisk.task.service.doris;

import com.fisk.common.core.baseObject.entity.BusinessResult;

/**
 * @author yhxu
 * CreateTime: 2021/7/1 10:43
 * Description:
 */
public interface IDorisBuild {
     BusinessResult dorisBuildTable(String executsql);
}
