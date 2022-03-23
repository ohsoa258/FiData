package com.fisk.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.system.enums.serverModuleTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/3 15:29
 */
@Data
public class DataViewDTO {

    public Integer id;
    public String viewName;
    private serverModuleTypeEnum serverModule;
    private Integer userId;
    private String viewType;
    private List<DataViewFilterDTO> filterList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    public String createUser;
}
