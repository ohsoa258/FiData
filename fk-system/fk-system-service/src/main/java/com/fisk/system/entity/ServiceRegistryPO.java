package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 * @data: 2021/7/06 13:52
 */
@TableName("tb_service_registry")
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceRegistryPO extends BasePO {

    /**
     *服务code
     */
    public  String serveCode;

    /**
     *上一级服务code
     */
    public  String parentServeCode;

    /**
     *服务中文名称
     */
    public  String serveCnName;

    /**
     *服务英文名称
     */
    public  String serveEnName;

    /**
     *服务url
     */
    public  String serveUrl;

    /**
     *服务图标
     */
    public  String icon;

    /**
     *排序号
     */
    public  int sequenceNo;


}
