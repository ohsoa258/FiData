package com.fisk.dataaccess.dto.api.doc;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API 版本
 * @date 2022/2/3 14:11
 */
@Data
public class ApiVersionDTO {
    /**
     * 文档版本
     */
    public String version;

    /**
     * 开始时间
     */
    public String startDate;

    /**
     * 结束时间
     */
    public String endDate;

    /**
     * 修改人
     */
    public String modifier;

    /**
     * 修改说明
     */
    public String explain;

    /**
     * 状态
     * 初稿
     * 更新
     */
    public String state;
}
