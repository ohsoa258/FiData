package com.fisk.dataservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author WangYan
 * @date 2021/8/3 16:51
 */
@Data
public class ApiConfigureDTO {
    @NotNull(message = "id不可为null")
    public Integer id;
    private String apiName;
    private String apiRoute;
    private String tableName;
    private String apiInfo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    public String createUser;
}
