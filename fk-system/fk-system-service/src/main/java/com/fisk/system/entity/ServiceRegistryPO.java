package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: JianWenYang
 * @data: 2021/7/06 13:52
 */
@TableName("tb_service_registry")
@Data
@EqualsAndHashCode(callSuper = false)
public class ServiceRegistryPO extends BaseEntity {

    @TableId
    private int id;

    /**
     *服务code
     */
    private  String serveCode;

    /**
     *上一级服务code
     */
    private  String parentServeCode;

    /**
     *服务中文名称
     */
    private  String serveCnName;

    /**
     *服务英文名称
     */
    private  String serveEnName;

    /**
     *服务url
     */
    private  String serveUrl;

    /**
     *服务图标
     */
    private  String icon;

    /**
     *排序号
     */
    private  int sequenceNo;

    /**
     *是否删除
     */
    private  int delFlag;

}
