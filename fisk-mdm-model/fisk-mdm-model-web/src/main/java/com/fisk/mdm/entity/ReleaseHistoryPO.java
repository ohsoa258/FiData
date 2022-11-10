package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/22 17:14
 * @Version 1.0
 */
@TableName("tb_release_history")
@Data
public class ReleaseHistoryPO {

    /**
     * 实体id
     */
    private Integer entityId;

    /**
     * 发布描述
     */
    private String desc;
}
