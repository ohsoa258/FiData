package com.fisk.system.dto;

import com.fisk.system.enums.serverModuleTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/4 17:56
 */
@Data
public class DataViewEditDTO {

    @NotNull
    private Integer id;
    private String viewName;
    private serverModuleTypeEnum serverModule;
    private String viewType;
    private Integer userId;
    private List<DataViewFilterDTO> viewFilterDTOList;
}
