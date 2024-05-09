package com.fisk.datagovernance.util;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.mapstruct.EnumTypeConversionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.enums.dataquality.RuleCheckTypeEnum;
import com.fisk.datagovernance.enums.dataquality.RuleExecuteNodeTypeEnum;
import com.fisk.datagovernance.enums.dataquality.RuleStateEnum;
import com.fisk.datagovernance.enums.dataquality.TableTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @Author Wangjian
 * @Date 2024/1/30 11:32
 * @Version 1.0
 * 类型转换器
 */
@Component
public class TypeConversionUtils extends EnumTypeConversionUtils {

    /**
     * TableTypeEnum枚举类型转换
     *
     * @param number 参数
     * @return {@link TableTypeEnum}
     */
    public TableTypeEnum intToTableTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return TableTypeEnum.NONE;
            case 1:
                return TableTypeEnum.TABLE;
            case 2:
                return TableTypeEnum.VIEW;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * TableBusinessTypeEnum枚举类型转换
     *
     * @param number 参数
     * @return {@link TableBusinessTypeEnum}
     */
    public TableBusinessTypeEnum intToTableBusinessTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return TableBusinessTypeEnum.NONE;
            case 1:
                return TableBusinessTypeEnum.DW_DIMENSION;
            case 2:
                return TableBusinessTypeEnum.DW_FACT;
            case 3:
                return TableBusinessTypeEnum.DORIS_DIMENSION;
            case 4:
                return TableBusinessTypeEnum.DORIS_FACT;
            case 5:
                return TableBusinessTypeEnum.WIDE_TABLE;
            case 6:
                return TableBusinessTypeEnum.ENTITY_TABLR;
            case 7:
                return TableBusinessTypeEnum.DATA_SERVICE_TABLE;
            case 8:
                return TableBusinessTypeEnum.DATA_SERVICE_API;
            case 9:
                return TableBusinessTypeEnum.STANDARD_DATABASE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * RuleCheckTypeEnum枚举类型转换
     *
     * @param number 参数
     * @return {@link RuleCheckTypeEnum}
     */
    public RuleCheckTypeEnum intToRuleCheckTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return RuleCheckTypeEnum.NONE;
            case 1:
                return RuleCheckTypeEnum.STRONG_RULE;
            case 2:
                return RuleCheckTypeEnum.WEAK_RULE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * RuleExecuteNodeTypeEnum枚举类型转换
     *
     * @param number 参数
     * @return {@link RuleExecuteNodeTypeEnum}
     */
    public RuleExecuteNodeTypeEnum intToRuleExecuteNodeTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return RuleExecuteNodeTypeEnum.NONE;
            case 1:
                return RuleExecuteNodeTypeEnum.BEFORE_SYNCHRONIZATION;
            case 2:
                return RuleExecuteNodeTypeEnum.SYNCHRONIZATION;
            case 3:
                return RuleExecuteNodeTypeEnum.AFTER_SYNCHRONIZATION;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * RuleStateEnum枚举类型转换
     *
     * @param number 参数
     * @return {@link RuleStateEnum}
     */
    public RuleStateEnum intToRuleStateEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return RuleStateEnum.Disable;
            case 1:
                return RuleStateEnum.Enable;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}