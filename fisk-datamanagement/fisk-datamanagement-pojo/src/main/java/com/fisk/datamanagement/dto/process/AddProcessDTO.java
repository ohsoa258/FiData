package com.fisk.datamanagement.dto.process;

import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AddProcessDTO {
    /**
     * 输入参数集合
     */
    public List<EntityIdAndTypeDTO> inputList;
    /**
     * process名称
     */
    public String processName;
    /**
     * 描述
     */
    public String description;
    /**
     * 输出类型
     */
    public EntityTypeEnum entityTypeEnum;
    /**
     * 输出参数guid
     */
    public String outGuid;
    /**
     * 联系人
     */
    public String contactInfo;

}
