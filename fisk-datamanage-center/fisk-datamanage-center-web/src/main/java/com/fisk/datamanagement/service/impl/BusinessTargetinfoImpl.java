package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.chartvisual.dto.chartvisual.ChartQueryObject;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.office.excel.ExcelUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategoryTreeDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDefsDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDefsDTO;
import com.fisk.datamanagement.entity.BusinessCategoryPO;
import com.fisk.datamanagement.entity.BusinessSynchronousPO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;
import com.fisk.datamanagement.mapper.BusinessCategoryMapper;
import com.fisk.datamanagement.mapper.BusinessTargetinfoMapper;
import com.fisk.datamanagement.service.BusinessCategoryService;
import com.fisk.datamanagement.service.BusinessTargetinfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xgf
 * @date 2023年11月20日 10:44
 */
@Service
public class BusinessTargetinfoImpl implements BusinessTargetinfoService {
    @Resource
    BusinessTargetinfoMapper businessTargetinfoMapper;

    /**
     * 查询指标明细数据
     * @param pid
     * @return
     */
    @Override
    public List<BusinessTargetinfoPO> SelectClassification(String  pid) {
        List<BusinessTargetinfoPO>  list = businessTargetinfoMapper.selectClassification(pid);
        return list;
    }

    /**
     * 查询数据类型范围的数据
     * @return
     */
    @Override
    public JSONObject SelecttypeClassification() {
        List<BusinessSynchronousPO>  list = businessTargetinfoMapper.selecttypeClassification();
        JSONArray statisticalcycle =new JSONArray();
        JSONArray indicatorlevel = new JSONArray();
        for (int i=0;i<list.size();i++ ){
            String typeData= list.get(i).typeData;
            if("1".equals(typeData)){
                JSONObject  statisticalcyclejson = new JSONObject();
                statisticalcyclejson.put("statisticalcyclejson", list.get(i).typeName);
                statisticalcycle.add(statisticalcyclejson);
            }else if ("2".equals(typeData)){
                JSONObject  indicatorleveljson = new JSONObject();
                indicatorleveljson.put("indicatorleveljson", list.get(i).typeName);
                indicatorlevel.add(indicatorleveljson);
            }
        }
     JSONObject json = new JSONObject();
        json.put("indicatorlevel",indicatorlevel.toJSONString());
        json.put("statisticalcycle",statisticalcycle.toJSONString());
        return json;
    }

    /**
     *向数据库中添加指标明细数据
     * @param
     */
    @Override
    public ResultEnum addTargetinfo(BusinessTargetinfoDefsDTO dto)
    {
        List<BusinessTargetinfoDTO> classificationDefList = dto.businessTargetinfoDefs;
        for (BusinessTargetinfoDTO item : classificationDefList){
            if (StringUtils.isEmpty(item.indicatorName)){
                throw new FkException(ResultEnum.ERROR, "指标主题名称不能为空");
            }
            // 查询数据
            QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
            qw.eq("indicator_name", item.indicatorName).eq("del_flag", 1).eq("pid",item.pid);
            BusinessTargetinfoPO bcPO = businessTargetinfoMapper.selectOne(qw);
            if (bcPO != null){
                throw new FkException(ResultEnum.ERROR, "指标主题名称已经存在");
            }
            // 添加数据
            BusinessTargetinfoPO model = new BusinessTargetinfoPO();
            model.setIndicatorName(item.indicatorName);
            model.setIndicatorDescription(item.indicatorDescription);
            model.setPid(item.pid);
            model.setAttributesNumber(item.attributesNumber);
            model.setDataGranularity(item.dataGranularity);
            model.setFilteringCriteria(item.filteringCriteria);
            model.setIndicatorCode(item.indicatorCode);
            model.setIndicatorformula(item.indicatorformula);
            model.setIndicatorLevel(item.indicatorLevel);
            model.setIndicatorStatus(item.indicatorStatus);
            model.setLargeScreenLink(item.largeScreenLink);
            model.setOperationalAttributes(item.operationalAttributes);
            model.setOrderChannel(item.orderChannel);
            model.setSourceDataTable(item.sourceDataTable);
            model.setSourceIndicators(item.sourceIndicators);
            model.setStatisticalCycle(item.statisticalCycle);
            model.setResponsibleDept(item.responsibleDept);
            model.setSourceSystem(item.sourceSystem);
            model.setUnitMeasurement(item.unitMeasurement);
            model.setIndicatorType(item.indicatorType);
            model.setName(item.name);
            int flag = businessTargetinfoMapper.insert(model);
            if (flag < 0){
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 根据指标id删除指标数据
     * @param Id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteTargetinfo(long Id)
    {
        // 查询数据
        QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
        qw.eq("id", Id);
        BusinessTargetinfoPO po = businessTargetinfoMapper.selectOne(qw);
        if (po == null){
            throw new FkException(ResultEnum.ERROR, "指标数据不存在");
        }

        List<Long> idList = new ArrayList<>();
        qw.eq("id", po.getId());
        idList.add(po.getId());
        if (businessTargetinfoMapper.deleteBatchIds(idList) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "删除指标数据失败");
        }
    }

    /**
     * 更改指标明细数据
     * @param dto
     * @return
     */
    @Override
    public ResultEnum updateTargetinfo(BusinessTargetinfoDefsDTO dto) {
        // 参数校验
        if (CollectionUtils.isEmpty(dto.getBusinessTargetinfoDefs())){
            throw new FkException(ResultEnum.ERROR, "修改指标明细数据错误");
        }
        BusinessTargetinfoDTO item = dto.getBusinessTargetinfoDefs().get(0);

        // 查询是否存在重复数据
        List<String> nameList = businessTargetinfoMapper.selectNameList(item.pid, DelFlagEnum.NORMAL_FLAG.getValue());
        if (nameList.contains(item.indicatorName)){
            throw new FkException(ResultEnum.ERROR, "同一分类下指标名称已存在");
        }

        // 查询当前业务分类
        QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
        qw.eq("id", item.id).eq("del_flag", 1);
        BusinessTargetinfoPO model = businessTargetinfoMapper.selectOne(qw);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.ERROR, "指标明细数据不存在");
        }
        model.setIndicatorName(item.indicatorName);
        model.setIndicatorDescription(item.indicatorDescription);
        model.setPid(item.pid);
        model.setAttributesNumber(item.attributesNumber);
        model.setDataGranularity(item.dataGranularity);
        model.setFilteringCriteria(item.filteringCriteria);
        model.setIndicatorCode(item.indicatorCode);
        model.setIndicatorformula(item.indicatorformula);
        model.setIndicatorLevel(item.indicatorLevel);
        model.setIndicatorStatus(item.indicatorStatus);
        model.setLargeScreenLink(item.largeScreenLink);
        model.setOperationalAttributes(item.operationalAttributes);
        model.setOrderChannel(item.orderChannel);
        model.setSourceDataTable(item.sourceDataTable);
        model.setSourceIndicators(item.sourceIndicators);
        model.setStatisticalCycle(item.statisticalCycle);
        model.setResponsibleDept(item.responsibleDept);
        model.setSourceSystem(item.sourceSystem);
        model.setUnitMeasurement(item.unitMeasurement);
        model.setIndicatorType(item.indicatorType);
        model.setName(item.name);
        if (businessTargetinfoMapper.updateById(model) <= 0){
            throw new FkException(ResultEnum.ERROR, "修改指标明细数据失败");
        }
        return ResultEnum.SUCCESS;
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public void downLoad(Integer id, HttpServletResponse response) {
        // 查询数据
        QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
        qw.eq("id", id);
        BusinessTargetinfoPO po = businessTargetinfoMapper.selectOne(qw);
        List<Map<String, Object>> list = businessTargetinfoMapper.selectClassification1(String.valueOf(id));
        ExcelUtil.uploadExcelAboutUser(response, "test.xlsx", list);
    }




}
