package com.fisk.datagovernance.util;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.mapstruct.EnumTypeConversionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.enums.dataquality.*;
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

    /**
     * RangeCheckTypeEnum枚举类型转换
     * @param number
     * @return RangeCheckTypeEnum
     */
    public RangeCheckTypeEnum intToRangeCheckTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return RangeCheckTypeEnum.NONE;
            case 1:
                return RangeCheckTypeEnum.SEQUENCE_RANGE;
            case 2:
                return RangeCheckTypeEnum.VALUE_RANGE;
            case 3:
                return RangeCheckTypeEnum.DATE_RANGE;
            case 4:
                return RangeCheckTypeEnum.KEYWORDS_INCLUDE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * StandardCheckTypeEnum枚举类型转换
     * @param number
     * @return StandardCheckTypeEnum
     */
    public StandardCheckTypeEnum intToStandardCheckTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return StandardCheckTypeEnum.NONE;
            case 1:
                return StandardCheckTypeEnum.DATE_FORMAT;
            case 2:
                return StandardCheckTypeEnum.CHARACTER_PRECISION_LENGTH_RANGE;
            case 3:
                return StandardCheckTypeEnum.URL_ADDRESS;
            case 4:
                return StandardCheckTypeEnum.BASE64_BYTE_STREAM;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * RangeCheckValueRangeTypeEnum枚举类型转换
     * @param number
     * @return RangeCheckValueRangeTypeEnum
     */
    public RangeCheckValueRangeTypeEnum intToRangeCheckValueRangeTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return RangeCheckValueRangeTypeEnum.NONE;
            case 1:
                return RangeCheckValueRangeTypeEnum.UNIDIRECTIONAL_VALUE;
            case 2:
                return RangeCheckValueRangeTypeEnum.INTERVAL_VALUE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * RangeCheckKeywordIncludeTypeEnum枚举类型转换
     * @param number
     * @return RangeCheckKeywordIncludeTypeEnum
     */
    public RangeCheckKeywordIncludeTypeEnum intToRangeCheckKeywordIncludeTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return RangeCheckKeywordIncludeTypeEnum.NONE;
            case 1:
                return RangeCheckKeywordIncludeTypeEnum.CONTAINS_KEYWORDS;
            case 2:
                return RangeCheckKeywordIncludeTypeEnum.INCLUDE_KEYWORDS_BEFORE;
            case 3:
                return RangeCheckKeywordIncludeTypeEnum.INCLUDE_KEYWORDS_AFTER;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * StandardCheckCharRangeTypeEnum枚举类型转换
     * @param number
     * @return StandardCheckCharRangeTypeEnum
     */
    public StandardCheckCharRangeTypeEnum intToStandardCheckCharRangeTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return StandardCheckCharRangeTypeEnum.NONE;
            case 1:
                return StandardCheckCharRangeTypeEnum.CHARACTER_PRECISION_RANGE;
            case 2:
                return StandardCheckCharRangeTypeEnum.CHARACTER_LENGTH_RANGE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * FluctuateCheckTypeEnum枚举类型转换
     * @param number
     * @return FluctuateCheckTypeEnum
     */
    public FluctuateCheckTypeEnum intToFluctuateCheckTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return FluctuateCheckTypeEnum.NONE;
            case 1:
                return FluctuateCheckTypeEnum.SUM;
            case 2:
                return FluctuateCheckTypeEnum.COUNT;
            case 3:
                return FluctuateCheckTypeEnum.AVG;
            case 4:
                return FluctuateCheckTypeEnum.MAX;
            case 5:
                return FluctuateCheckTypeEnum.MIN;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    /**
     * ParentageCheckTypeEnum枚举类型转换
     * @param number
     * @return ParentageCheckTypeEnum
     */
    public ParentageCheckTypeEnum intToParentageCheckTypeEnum(Integer number) {
        if (number == null) {
            return null;
        }
        switch (number) {
            case 0:
                return ParentageCheckTypeEnum.NONE;
            case 1:
                return ParentageCheckTypeEnum.CHECK_UPSTREAM_BLOODLINE;
            case 2:
                return ParentageCheckTypeEnum.CHECK_DOWNSTREAM_BLOODLINE;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}