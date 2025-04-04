package com.fisk.dataaccess.vo.pgsql;

import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class NifiVO {
    /**
     * 用户id
     */
    public Long userId;
    /**
     * 应用注册id
     */
    public String appId;

    /**
     * 应用类型
     */
    public Integer appType;
    /**
     * nifi流程回写的应用组件id
     */
    public String appComponentId;
    /**
     * 应用注册下保存的atlasId
     */
    public String appAtlasId;
    /**
     * delApp
     */
    public Boolean delApp;
    /**
     * 物理表id
     */
    public List<Long> tableIdList;
    /**
     * 表数组
     */
    public List<TableListVO> tableList;
    /**
     * 元数据限定名
     */
    public List<String> qualifiedNames;

    /**
     * atlas业务名称(应用名称_应用简称)
     */
    public String classifications;

    /**
     * 管道名称
     */
    public List<String> workFlowName;

    /**
     * 应用简称
     */
    public String appAbbreviation;

    /**
     * hudi:是否同步全部表 0否，1是
     */
    public Integer ifSyncAllTables;

}
