package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/22 17:15
 * @Version 1.0
 */
@TableName("tb_release_details")
@Data
public class ReleaseDetails {

    /**
     * 发布id
     */
    private Integer releaseId;

    /**
     * 属性日志表id
     */
    private Integer attributeLogId;
}
