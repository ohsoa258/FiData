package com.fisk.chartvisual.util.dbhelper.whitePondbuidsql;

import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.dataservice.dto.IndicatorDTO;
import com.fisk.dataservice.enums.IndicatorTypeEnum;
import org.apache.commons.lang3.StringUtils;

import static com.fisk.dataservice.enums.IndicatorTypeEnum.DERIVED_INDICATORS;

/**
 * @author WangYan
 * @date 2021/12/23 11:17
 */
public class DeriveHelper {

    public static final String DERIVE = "b";
    public static final String DERIVE_ALIAS = "a1";

    /**
     * 判断是否是派生指标,追加括号变成子查询
     * @param stringBuilder
     * @param type
     */
    public static void isDerive(StringBuilder stringBuilder, IndicatorTypeEnum type){
        if (type == DERIVED_INDICATORS){
            stringBuilder.insert(0,"(");
            stringBuilder.insert(stringBuilder.length(),")" + " AS " + DERIVE_ALIAS);
        }
    }

    /**
     * 派生指标追加业务限定
     * @param stringBuilder
     * @param dto
     * @param type
     */
    public static void filter(StringBuilder stringBuilder, IndicatorDTO dto, IndicatorTypeEnum type){
        if (type == DERIVED_INDICATORS && StringUtils.isNotBlank(dto.getWhereTimeLogic())){
            stringBuilder.append(" WHERE " + dto.getWhereTimeLogic());
        }
    }

    /**
     * 派生指标追加 over 函数
     * @param indicator
     * @param derive
     * @param dto
     * @return
     */
    public static StringBuilder deriveOver(IndicatorDTO indicator,String derive, DimensionTimePeriodDTO dto){
        String timePeriod = null;
        String yearAccumulate = "YTD";
        String monthAccumulate = "MTD";
        String quarterlyAccumulate = "QTD";
        if (yearAccumulate.equals(indicator.getTimePeriod())){
            timePeriod = "YEAR";
        }else if (monthAccumulate.equals(indicator.getTimePeriod())){
            timePeriod = "MONTH";
        }else if (quarterlyAccumulate.equals(indicator.getTimePeriod())){
            timePeriod = "QUARTER";
        }

        String dateKey = DERIVE_ALIAS + "."+ dto.getDimensionAttributeField();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("over(PARTITION BY YEAR("+ dateKey + ")");
        stringBuilder.append(","+ timePeriod + "(" + dateKey + ")" + " ORDER BY " + dateKey);
        stringBuilder.append(" rows BETWEEN UNBOUNDED preceding AND CURRENT ROW) AS " + indicator.getDeriveName());
        if (StringUtils.isNotBlank(derive)){
            stringBuilder.append("," + "row_number() over(PARTITION BY "+ derive  +" ORDER BY "+ dateKey +" DESC) AS id");
        }
        return stringBuilder;
    }
}
