package com.fisk.common.filter.method;

import com.fisk.common.filter.dto.FilterQueryDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author JianWenYang
 */
@Component
public class GenerateCondition {

    /**
     * 根据筛选器条件,拼接where条件
     * @param filterList
     * @return
     */
    public String getCondition(List<FilterQueryDTO> filterList)
    {
        StringBuilder str = new StringBuilder();
        for (FilterQueryDTO model:filterList)
        {
            switch (model.queryType)
            {
                case "大于":
                    str.append(" and " +model.columnName+" > '"+model.columnValue+"' ");
                    break;
                case "小于":
                    str.append(" and " +model.columnName+" < '" +model.columnValue+"' ");
                    break;
                case "等于":
                    str.append(" and " +model.columnName+" = '"+model.columnValue+"' ");
                    break;
                case "包含":
                    str.append(" and " +model.columnName+" like concat('%'," + "'" + model.columnValue+"'" + ", '%') " );
                    break;
                default:
                    break;
            }
        }
        return  str.toString();
    }

}
