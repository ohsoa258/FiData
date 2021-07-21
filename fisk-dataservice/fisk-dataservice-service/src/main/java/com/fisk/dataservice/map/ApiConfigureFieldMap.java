package com.fisk.dataservice.map;


import com.fisk.dataservice.dto.ApiConfigureField;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/21 10:47
 */
public class ApiConfigureFieldMap {


    public static List<ApiConfigureFieldPO> apiConfigureFieldList(List<ApiConfigureField> dto){
        List<ApiConfigureFieldPO> fieldList = new ArrayList<>();
        for (ApiConfigureField apiConfigureField : dto) {
            ApiConfigureFieldPO apiConfigureFieldPO = new ApiConfigureFieldPO();
            apiConfigureFieldPO.setFieldId(apiConfigureField.getFieldId());
            apiConfigureFieldPO.setField(apiConfigureField.getField());
            apiConfigureFieldPO.setFieldType(apiConfigureField.getFieldType());
            apiConfigureFieldPO.setFieldConditionValue(apiConfigureField.getFieldConditionValue());
            apiConfigureFieldPO.setFieldValue(apiConfigureField.getFieldValue());
            fieldList.add(apiConfigureFieldPO);
        }
        return fieldList;
    }
}
