package com.fisk.dataaccess.dto.output.datatarget;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class DataTargetPageResultDTO extends BaseUserInfoVO {
    public Long id;
    /**
     * 名称
     */
    public String name;
    /**
     * 负责人
     */
    public String principal;
    /**
     * 描述
     */
    public String description;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;
    /**
     * 主机地址
     */
    public String host;
    /**
     * ip请求接口
     */
    public String apiAddress;

}
