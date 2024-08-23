package com.fisk.common.service.pageFilter.utils;

import com.fisk.common.service.pageFilter.dto.FilterEnum;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author JianWenYang
 */
@Component
public class GenerateCondition {

    /**
     * 根据筛选器条件,拼接where条件
     *
     * @param filterList
     * @return
     */
    public String getCondition(List<FilterQueryDTO> filterList) {
        StringBuilder str = new StringBuilder();
        for (FilterQueryDTO model : filterList) {
            //获取查询类型枚举值
            FilterEnum queryType = FilterEnum.getValue(model.queryType);
            if (!model.columnName.equals("app_type")) {
                switch (queryType) {
                    case GREATER_THAN:
                        str.append(" and " + model.columnName + " > N'" + model.columnValue + "' ");
                        break;
                    case LESS_THAN:
                        str.append(" and " + model.columnName + " < N'" + model.columnValue + "' ");
                        break;
                    case EQUAL:
                        str.append(" and " + model.columnName + " = N'" + model.columnValue + "' ");
                        break;
                    case CONTAINS:
                        str.append(" and " + model.columnName + " like concat('%'," + "N'" + model.columnValue + "'" + ", '%') ");
                        break;
                    default:
                        break;
                }
            } else {
                switch (queryType) {
                    case GREATER_THAN:
                        str.append(" and " + model.columnName + " > " + model.columnValue + " ");
                        break;
                    case LESS_THAN:
                        str.append(" and " + model.columnName + " < " + model.columnValue + " ");
                        break;
                    case EQUAL:
                        str.append(" and " + model.columnName + " = " + model.columnValue + " ");
                        break;
                    case CONTAINS:
                        str.append(" or " + model.columnName + " like '%" + "" + model.columnValue + "'");
                        break;
                    default:
                        break;
                }
            }
        }

        if (str.toString().contains("or")){
            str.replace(str.indexOf("or"), str.indexOf("or") + 2, "and");
        }

        return str.toString();
    }

}
