package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-08-01
 * @Description:
 */
@TableName("tb_code_collection")
@Data
public class CodeCollectionPO extends BasePO {

    private String collectionName;

    private String description;
}
