package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.service.ICategory;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class CategoryImpl implements ICategory {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.glossary.category}")
    public String category;

    @Override
    public ResultEnum addCategory(CategoryDTO dto)
    {
       try {
           String jsonParameter= JSONArray.toJSON(dto).toString();
           JSONObject jsonObj = JSON.parseObject(jsonParameter);
           String parentCategory=jsonObj.getString("parentCategory");
           JSONObject parent = JSON.parseObject(parentCategory);
           String parentValue=parent.getString("categoryGuid");
           if ("".equals(parentValue)) {
               jsonObj.remove("parentCategory");
               jsonParameter = jsonObj.toJSONString();
           } else {
               jsonObj.remove("guid");
               jsonParameter = jsonObj.toJSONString();
           }
           ResultDataDTO<String> result = atlasClient.post(category, jsonParameter);
           return result.code == AtlasResultEnum.REQUEST_SUCCESS ? ResultEnum.SUCCESS : ResultEnum.BAD_REQUEST;
       } catch (Exception e) {
           log.error("addCategory ex:", e);
           return ResultEnum.PARAMTER_ERROR;
       }
    }

    @Override
    public ResultEnum deleteCategory(String guid)
    {
        ResultDataDTO<String> result = atlasClient.delete(category + "/" + guid);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public CategoryDTO getCategory(String guid)
    {
        ResultDataDTO<String> result = atlasClient.get(category + "/" + guid);
        return JSONObject.parseObject(result.data,CategoryDTO.class);
    }

    @Override
    public ResultEnum updateCategory(CategoryDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(category,jsonParameter);
        return result.code== AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }



}
