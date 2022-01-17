package com.fisk.dataaccess.dto.api;

import lombok.Data;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Data
public class ApiConfigDTO {

    /**
     * 主键
     */
    public long id;

    /**
     * api名称
     */
    public String apiName;

    /**
     * api描述
     */
    public String apiDes;

}
