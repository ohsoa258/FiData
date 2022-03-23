package com.fisk.system.dto;

import com.fisk.system.enums.serverModuleTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/4 16:24
 */
@Data
public class DataViewAddDTO {

    @NotNull
    private String viewName;
    private serverModuleTypeEnum serverModule;
    private Integer userId;
    private String viewType;
    private List<DataViewFilterDTO> filterDTO;
}
