package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.classification.ClassificationTreeDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.service.IGlobalSearch;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class GlobalSearchImpl implements IGlobalSearch {

    @Resource
    AtlasClient atlasClient;
    @Resource
    ClassificationImpl classification;

    @Value("${atlas.searchQuick}")
    private String searchQuick;
    @Value("${atlas.searchSuggestions}")
    private String searchSuggestions;

    @Override
    public JSONObject searchQuick(String query, int limit, int offset) {
        ResultDataDTO<String> result = atlasClient.get(searchQuick + "?query=" + query + "&limit=" + limit + "&offset=" + offset);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            JSONObject msg = JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

    @Override
    public JSONObject searchSuggestions(String prefixString) {
        ResultDataDTO<String> result = atlasClient.get(searchSuggestions + "?prefixString=" + prefixString);
        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            JSONObject msg = JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }

    @Override
    public List<ClassificationTreeDTO> searchClassification(String keyword) {
        List<ClassificationTreeDTO> classificationTree = classification.getClassificationTree();
        filterClassificationTree(classificationTree, keyword);
        return classificationTree;
    }

    /**
     * 模糊查询tree
     *
     * @param classificationList
     * @param keyword
     */
    public void filterClassificationTree(List<ClassificationTreeDTO> classificationList, String keyword) {
        Iterator<ClassificationTreeDTO> iter = classificationList.iterator();
        while (iter.hasNext()) {
            // 获取当前遍历到的目录
            ClassificationTreeDTO classification = iter.next();
            // 如果当前目录名称包含关键字，则什么也不做（不移除），否则就看下一级
            if (!classification.getName().contains(keyword)) {
                // 取出下一级目录集合
                List<ClassificationTreeDTO> childrenCategoryList = classification.getChild();
                // 递归
                if (!CollectionUtils.isEmpty(childrenCategoryList)) {
                    filterClassificationTree(childrenCategoryList, keyword);
                }
                // 下一级目录看完了，如果下一级目录全部被移除，则移除当前目录
                if (CollectionUtils.isEmpty(classification.getChild())) {
                    iter.remove();
                }
            }
        }
    }

}
