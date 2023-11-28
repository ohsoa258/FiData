package com.fisk.common.server.metadata;

import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationInfoDTO {

    public String name;

    public String description;

    /**
     * 来源类型
     */
    public ClassificationTypeEnum sourceType;

    /**
     * 应用类型：来源类型数据接入时 (0:实时应用  1:非实时应用) ，
     */
    public Integer appType;
    /**
     * 是否删除
     */
    public boolean delete;

}
