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
     * nifi流程回写的应用组件id
     */
    public String appComponentId;
    /**
     * 应用注册下保存的atlasId
     */
    public String appAtlasId;
    /**
     * 物理表id
     */
    public List<Long> tableIdList;
    /**
     * 表数组
     */
    public List<TableListVO> tableList;
}
