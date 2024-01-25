package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONPOJOBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.office.excel.ExcelUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.classification.BusinessExtendedfieldsDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDefsDTO;
import com.fisk.datamanagement.dto.classification.FacttreeListDTOs;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.entity.BusinessSynchronousPO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.mapper.BusinessExtendedfieldsMapper;
import com.fisk.datamanagement.mapper.BusinessTargetinfoMapper;
import com.fisk.datamanagement.mapper.FactTreeListMapper;
import com.fisk.datamanagement.service.BusinessTargetinfoService;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xgf
 * @date 2023年11月20日 10:44
 */
@Slf4j
@Service
public class BusinessTargetinfoImpl implements BusinessTargetinfoService {
    @Resource
    BusinessTargetinfoMapper businessTargetinfoMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    FactTreeListMapper factTreeListMapper;
    @Resource
    BusinessExtendedfieldsMapper businessExtendedfieldsMapper;

    private static final String[] parentTargetinfoHeaders = {"一级分类", "二级分类", "负责部门", "指标编码", "指标类型", "指标名称", "指标描述/口径", "指标范围",
            "计量单位", "统计周期", "指标公式", "指标脚本", "指标来源", "数据筛选条件", "来源系统", "来源数据表", "指标状态", "应用", "订单渠道","数据粒度"};


    /**
     * 查询指标明细数据
     *
     * @param pid
     * @return
     */
    @Override
        public JSONArray SelectClassification(String pid) {

        JSONArray array1 =new JSONArray();
        List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectClassification(pid);
        String indexid= pid;
        //List<FactTreePOs> list2 = factTreeListMapper.selectParentpIds(pid);
        for(int i=0;i<list.size();i++){
            List<BusinessExtendedfieldsPO> list1= businessExtendedfieldsMapper.selectParentpId(list.get(i).getId()+"");
            List<FactTreePOs> list2 = factTreeListMapper.selectParentpIds(list.get(i).getId()+"");
            JSONObject jsonObject1 =  new JSONObject();
            jsonObject1.put("id",list.get(i).getId());
            jsonObject1.put("createTime",list.get(i).getCreateTime());
            jsonObject1.put("createUser",list.get(i).getCreateUser());
            jsonObject1.put("updateTime",list.get(i).getUpdateTime());
            jsonObject1.put("updateUser",list.get(i).getUpdateUser());
            jsonObject1.put("delFlag",list.get(i).getDelFlag());
            jsonObject1.put("pid",list.get(i).getPid());
            jsonObject1.put("responsibleDept",list.get(i).getResponsibleDept());
            jsonObject1.put("indicatorCode",list.get(i).getIndicatorCode());
            jsonObject1.put("indicatorName",list.get(i).getIndicatorName());
            jsonObject1.put("indicatorDescription",list.get(i).getIndicatorDescription());
            jsonObject1.put("indicatorLevel",list.get(i).getIndicatorLevel());
            jsonObject1.put("unitMeasurement",list.get(i).getUnitMeasurement());
            jsonObject1.put("statisticalCycle",list.get(i).getStatisticalCycle());
            jsonObject1.put("indicatorformula",list.get(i).getIndicatorformula());
            jsonObject1.put("indicatorStatus",list.get(i).getIndicatorStatus());
            jsonObject1.put("filteringCriteria",list.get(i).getFilteringCriteria());
            jsonObject1.put("dataGranularity",list.get(i).getDataGranularity());
            jsonObject1.put("operationalAttributes",list.get(i).getOperationalAttributes());
            jsonObject1.put("sourceDataTable",list.get(i).getSourceDataTable());
            jsonObject1.put("sourceIndicators",list.get(i).getSourceIndicators());
            jsonObject1.put("orderChannel",list.get(i).getOrderChannel());
            jsonObject1.put("indicatorType",list.get(i).getIndicatorType());
            jsonObject1.put("name",list.get(i).getName());
            jsonObject1.put("sqlScript",list.get(i).getSqlScript());
            jsonObject1.put("dimensionData",list1);
            jsonObject1.put("facttreeListData",list2);
            array1.set(i,jsonObject1);
        }

        return array1;
    }


    @Override
    public JSONArray SelectClassifications(String factTabName, String factFieldEnName) {
        JSONArray array1 =new JSONArray();
        List<String> list2 = factTreeListMapper.selectsParentpIds(factTabName,factFieldEnName);
        for (int n = 0; n<list2.size(); n++){
            String id= list2.get(n);
            List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectClassificationss(id);
            //List<FactTreePOs> list2 = factTreeListMapper.selectParentpIds(pid);
            for(int i=0;i<list.size();i++){
                List<BusinessExtendedfieldsPO> list1= businessExtendedfieldsMapper.selectParentpId(list.get(i).getId()+"");
                JSONObject jsonObject1 =  new JSONObject();
                jsonObject1.put("id",list.get(i).getId());
                jsonObject1.put("createTime",list.get(i).getCreateTime());
                jsonObject1.put("createUser",list.get(i).getCreateUser());
                jsonObject1.put("updateTime",list.get(i).getUpdateTime());
                jsonObject1.put("updateUser",list.get(i).getUpdateUser());
                jsonObject1.put("delFlag",list.get(i).getDelFlag());
                jsonObject1.put("pid",list.get(i).getPid());
                jsonObject1.put("responsibleDept",list.get(i).getResponsibleDept());
                jsonObject1.put("indicatorCode",list.get(i).getIndicatorCode());
                jsonObject1.put("indicatorName",list.get(i).getIndicatorName());
                jsonObject1.put("indicatorDescription",list.get(i).getIndicatorDescription());
                jsonObject1.put("indicatorLevel",list.get(i).getIndicatorLevel());
                jsonObject1.put("unitMeasurement",list.get(i).getUnitMeasurement());
                jsonObject1.put("statisticalCycle",list.get(i).getStatisticalCycle());
                jsonObject1.put("indicatorformula",list.get(i).getIndicatorformula());
                jsonObject1.put("indicatorStatus",list.get(i).getIndicatorStatus());
                jsonObject1.put("filteringCriteria",list.get(i).getFilteringCriteria());
                jsonObject1.put("dataGranularity",list.get(i).getDataGranularity());
                jsonObject1.put("operationalAttributes",list.get(i).getOperationalAttributes());
                jsonObject1.put("sourceDataTable",list.get(i).getSourceDataTable());
                jsonObject1.put("sourceIndicators",list.get(i).getSourceIndicators());
                jsonObject1.put("orderChannel",list.get(i).getOrderChannel());
                jsonObject1.put("indicatorType",list.get(i).getIndicatorType());
                jsonObject1.put("name",list.get(i).getName());
                jsonObject1.put("sqlScript",list.get(i).getSqlScript());
                jsonObject1.put("dimensionData",list1);
                if (jsonObject1 != null){array1.set(n,jsonObject1);}

            }

        }

        return array1;
    }





    /**
     * 查询数据类型范围的数据
     *
     * @return
     */
    @Override
    public JSONObject SelecttypeClassification() {
        List<BusinessSynchronousPO> list = businessTargetinfoMapper.selecttypeClassification();
        JSONArray statisticalcycle = new JSONArray();
        JSONArray indicatorlevel = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            String typeData = list.get(i).typeData;
            if ("1".equals(typeData)) {
                JSONObject statisticalcyclejson = new JSONObject();
                statisticalcyclejson.put("statisticalcyclejson", list.get(i).typeName);
                statisticalcycle.add(statisticalcyclejson);
            } else if ("2".equals(typeData)) {
                JSONObject indicatorleveljson = new JSONObject();
                indicatorleveljson.put("indicatorleveljson", list.get(i).typeName);
                indicatorlevel.add(indicatorleveljson);
            }
        }
        JSONObject json = new JSONObject();
        json.put("indicatorlevel", indicatorlevel.toJSONString());
        json.put("statisticalcycle", statisticalcycle.toJSONString());
        return json;
    }

    /**
     * 向数据库中添加指标明细数据
     *
     * @param
     */
    @Override
    public ResultEnum addTargetinfo(BusinessTargetinfoDefsDTO dto) {
        List<BusinessTargetinfoDTO> classificationDefList = dto.businessTargetinfoDefs;
        for (BusinessTargetinfoDTO item : classificationDefList) {
            if (StringUtils.isEmpty(item.indicatorName)) {
                throw new FkException(ResultEnum.ERROR, "指标主题名称不能为空");
            }
            // 查询数据
            QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
            qw.eq("indicator_name", item.indicatorName).eq("del_flag", 1).eq("pid", item.pid);
            BusinessTargetinfoPO bcPO = businessTargetinfoMapper.selectOne(qw);
            if (bcPO != null) {
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
            model.setSqlScript(item.sqlScript);
            int flag = businessTargetinfoMapper.insert(model);
            if (flag < 0) {
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
            QueryWrapper<BusinessTargetinfoPO> qwnew = new QueryWrapper<>();
            qw.eq("indicator_name", item.indicatorName).eq("del_flag", 1).eq("pid", item.pid);
            BusinessTargetinfoPO bcPOnew = businessTargetinfoMapper.selectOne(qw);
            for (int j=0;j<item.getDimensionData().size();j++){
               BusinessExtendedfieldsDTO model2 = item.getDimensionData().get(j);
               BusinessExtendedfieldsPO model1 = new BusinessExtendedfieldsPO();
               model1.setAttribute(model2.attribute);
               model1.setAttributeid(model2.attributeid);
               model1.setDimdomain(model2.dimdomain);
               model1.setDimdomainid(model2.dimdomainid);
               model1.setDimdomaintype(model2.dimdomaintype);
               model1.setDimtable(model2.dimtable);
               model1.setDimtableid(model2.dimtableid);
               model1.setIndexid(bcPOnew.id+"");
               model1.setCreatedUser(userHelper.getLoginUserInfo().username);
               DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               model1.setCreatedTime(format.format(new Date()));
               model1.setDelFlag(1);
               int flag1 = businessExtendedfieldsMapper.insert(model1);
               if (flag1 < 0) {
                   throw new FkException(ResultEnum.ERROR, "保存失败");
               }
           }
            for (int m=0;m<item.getFacttreeListData().size();m++){
                FacttreeListDTOs model4= item.getFacttreeListData().get(m);
                FactTreePOs model3 = new FactTreePOs();
                model3.setPid(bcPOnew.id+"");
                model3.setBusinessNameId(model4.businessNameId);
                model3.setBusinessName(model4.businessName);
                model3.setFactTabNameId(model4.factTabNameId);
                model3.setFactTabName(model4.factTabName);
                model3.setFactFieldEnNameId(model4.factFieldEnNameId);
                model3.setFactFieldEnName(model4.factFieldEnName);
                model3.setCreateUser(userHelper.getLoginUserInfo().username);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                model3.setCreateTime(LocalDateTime.now());
                model3.setDelFlag(1);
                int flag2= factTreeListMapper.insert(model3);
                if (flag2 < 0) {
                    throw new FkException(ResultEnum.ERROR, "保存失败");
                }
            }
        }
        return ResultEnum.SUCCESS;
    }


    /**
     * 根据指标id删除指标数据
     *
     * @param Id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteTargetinfo(long Id) {
        // 查询数据
        QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
        qw.eq("id", Id);
        BusinessTargetinfoPO po = businessTargetinfoMapper.selectOne(qw);
        if (po == null) {
            throw new FkException(ResultEnum.ERROR, "指标数据不存在");
        }

        List<Long> idList = new ArrayList<>();
        qw.eq("id", po.getId());
        idList.add(po.getId());
        if (businessTargetinfoMapper.deleteBatchIds(idList) > 0) {
            int flag1 = businessExtendedfieldsMapper.updateByName(po.getId()+"");
            return ResultEnum.SUCCESS;
        } else {
            throw new FkException(ResultEnum.ERROR, "删除指标数据失败");
        }
    }

    /**
     * 更改指标明细数据
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum updateTargetinfo(BusinessTargetinfoDefsDTO dto) {
        // 参数校验
        if (CollectionUtils.isEmpty(dto.getBusinessTargetinfoDefs())) {
            throw new FkException(ResultEnum.ERROR, "修改指标明细数据错误");
        }
        BusinessTargetinfoDTO item = dto.getBusinessTargetinfoDefs().get(0);

        // 查询是否存在重复数据
        List<String> nameList = businessTargetinfoMapper.selectNameList(item.pid, DelFlagEnum.NORMAL_FLAG.getValue());
        if (nameList.contains(item.indicatorName)) {
            throw new FkException(ResultEnum.ERROR, "同一分类下指标名称已存在");
        }

        // 查询当前业务分类
        QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
        qw.eq("id", item.id).eq("del_flag", 1);
        BusinessTargetinfoPO model = businessTargetinfoMapper.selectOne(qw);
        if (Objects.isNull(model)) {
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
        model.setSqlScript(item.sqlScript);
        if (businessTargetinfoMapper.updateById(model) <= 0) {
            throw new FkException(ResultEnum.ERROR, "修改指标明细数据失败");
        }
        QueryWrapper<BusinessTargetinfoPO> qwnew = new QueryWrapper<>();
        qw.eq("indicator_name", item.indicatorName).eq("del_flag", 1).eq("pid", item.pid);
        BusinessTargetinfoPO bcPOnew = businessTargetinfoMapper.selectOne(qw);
        int flag1 = businessExtendedfieldsMapper.updateByName(bcPOnew.id+"");
        for (int j=0;j<item.getDimensionData().size();j++){
            BusinessExtendedfieldsDTO model2 = item.getDimensionData().get(j);
            BusinessExtendedfieldsPO model1 = new BusinessExtendedfieldsPO();
            model1.setAttribute(model2.attribute);
            model1.setAttributeid(model2.attributeid);
            model1.setDimdomain(model2.dimdomain);
            model1.setDimdomainid(model2.dimdomainid);
            model1.setDimdomaintype(model2.dimdomaintype);
            model1.setDimtable(model2.dimtable);
            model1.setDimtableid(model2.dimtableid);
            model1.setIndexid(bcPOnew.id+"");
            model1.setCreatedUser(userHelper.getLoginUserInfo().username);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            model1.setCreatedTime(format.format(new Date()));
            model1.setDelFlag(1);
            int flag2 = businessExtendedfieldsMapper.insert(model1);
            if (flag2 < 0) {
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
        }
        int flag3= factTreeListMapper.updateByName(bcPOnew.id+"");
        for (int n=0; n<item.getFacttreeListData().size();n++){
            FacttreeListDTOs model4= item.getFacttreeListData().get(n);
            FactTreePOs model3 = new FactTreePOs();
            model3.setPid(bcPOnew.id+"");
            model3.setBusinessNameId(model4.businessNameId);
            model3.setBusinessName(model4.businessName);
            model3.setFactTabNameId(model4.businessNameId);
            model3.setFactTabName(model4.factTabName);
            model3.setFactFieldEnNameId(model4.factFieldEnNameId);
            model3.setFactFieldEnName(model4.factFieldEnName);
            model3.setCreateUser(userHelper.getLoginUserInfo().username);
            model3.setCreateTime(LocalDateTime.now());
            model3.setDelFlag(1);
            int flag2= factTreeListMapper.insert(model3);
            if (flag2 < 0) {
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
        }
        return ResultEnum.SUCCESS;
    }
    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public void downLoad(String id,String indicatorname, HttpServletResponse response) {
        // 查询数据
        List<Map<String, Object>> list =null;
        if(id ==null && indicatorname==null){
            list = businessTargetinfoMapper.selectClassification2();
        }
        if(id !=null && indicatorname!=null){
            list = businessTargetinfoMapper.selectClassification1(id,indicatorname);
        }
        if(id !=null && indicatorname==null){
            list = businessTargetinfoMapper.selectClassification3(id);
        }
        uploadExcelAboutUser(response, "TargetinfoDetailData.xlsx", list);
    }


    /**
     * 用户信息导出类
     *
     * @param response 响应
     * @param fileName 文件名
     * @param dataList 导出的数据
     */
    public static void uploadExcelAboutUser(HttpServletResponse response, String fileName,
                                            List<Map<String, Object>> dataList) {
        //声明输出流
        OutputStream os = null;
        try {
            //设置响应头
            setResponseHeader(response, fileName);
            //获取输出流
            os = response.getOutputStream();
            //内存中保留1000条数据，以免内存溢出，其余写入硬盘
            SXSSFWorkbook wb = new SXSSFWorkbook(1000);
            //获取该工作区的第一个sheet
            Sheet sheet1 = wb.createSheet("sheet1");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<LinkedHashMap<String, Object>> data = new ArrayList<>();
            for (Map<String, Object> aa : dataList) {
                if (!aa.containsKey("namepid")) {
                    aa.put("namepid", "");
                }
                if (!aa.containsKey("name")) {
                    aa.put("name", "");
                }
                if (!aa.containsKey("large_screen_link")) {
                    aa.put("large_screen_link", "");
                }
                if (!aa.containsKey("responsible_dept")) {
                    aa.put("responsible_dept", "");
                }
                if (!aa.containsKey("indicator_code")) {
                    aa.put("indicator_code", "");
                }
                if (!aa.containsKey("statistical_cycle")) {
                    aa.put("statistical_cycle", "");
                }
                if (!aa.containsKey("source_system")) {
                    aa.put("source_system", "");
                }
                if (!aa.containsKey("indicator_name")) {
                    aa.put("indicator_name", "");
                }
                if (!aa.containsKey("unit_measurement")) {
                    aa.put("unit_measurement", "");
                }
                if (!aa.containsKey("source_indicators")) {
                    aa.put("source_indicators", "");
                }
                if (!aa.containsKey("indicator_type")) {
                    aa.put("indicator_type", "");
                }
                if (!aa.containsKey("indicator_status")) {
                    aa.put("indicator_status", "");
                }
                if (!aa.containsKey("indicator_level")) {
                    aa.put("indicator_level", "");
                }
                if (!aa.containsKey("source_data_table")) {
                    aa.put("source_data_table", "");
                }
                if (!aa.containsKey("indicatorformula")) {
                    aa.put("indicatorformula", "");
                }
                if (!aa.containsKey("sql_script")) {
                    aa.put("sql_script", "");
                }
                if (!aa.containsKey("filtering_criteria")) {
                    aa.put("filtering_criteria", "");
                }
                if (!aa.containsKey("order_channel")) {
                    aa.put("order_channel", "");
                }
                if (!aa.containsKey("indicator_description")) {
                    aa.put("indicator_description", "");
                }
                if (!aa.containsKey("data_granularity")) {
                    aa.put("data_granularity", "");
                }
            }
            for (Map<String, Object> stringObjectMap : dataList) {
                LinkedHashMap<String, Object> datamap = new LinkedHashMap<>();
                for (String key : stringObjectMap.keySet()) {
                    if ("large_screen_link".equals(key)) {
                        datamap.put("namepid", stringObjectMap.get("namepid"));
                    }
                    if ("responsible_dept".equals(key)) {
                        datamap.put("name", stringObjectMap.get("name"));

                    }
                    if ("namepid".equals(key)) {
                        datamap.put("responsible_dept", stringObjectMap.get("responsible_dept"));

                    }
                    if ("indicator_code".equals(key)) {
                        datamap.put("indicator_code", stringObjectMap.get("indicator_code"));

                    }
                    if ("source_indicators".equals(key)) {
                        datamap.put("indicator_type", stringObjectMap.get("indicator_type"));

                    }
                    if ("indicator_type".equals(key)) {
                        datamap.put("indicator_name", stringObjectMap.get("indicator_name"));

                    }
                    if ("indicator_level".equals(key)) {
                        datamap.put("indicator_description", stringObjectMap.get("indicator_description"));

                    }
                    if ("source_data_table".equals(key)) {
                        datamap.put("indicator_level", stringObjectMap.get("indicator_level"));

                    }
                    if ("indicatorformula".equals(key)) {
                        datamap.put("unit_measurement", stringObjectMap.get("unit_measurement"));

                    }
                    if ("sql_script".equals(key)) {
                        datamap.put("statistical_cycle", stringObjectMap.get("statistical_cycle"));

                    }
                    if ("filtering_criteria".equals(key)) {
                        datamap.put("indicatorformula", stringObjectMap.get("indicatorformula"));

                    }
                    if ("order_channel".equals(key)) {
                        datamap.put("sql_script", stringObjectMap.get("sql_script"));

                    }
                    if ("data_granularity".equals(key)) {
                        datamap.put("source_indicators", stringObjectMap.get("source_indicators"));

                    }
                    if ("statistical_cycle".equals(key)) {
                        datamap.put("filtering_criteria", stringObjectMap.get("filtering_criteria"));

                    }
                    if ("source_system".equals(key)) {
                        datamap.put("source_system", stringObjectMap.get("source_system"));

                    }
                    if ("indicator_name".equals(key)) {
                        datamap.put("source_data_table", stringObjectMap.get("source_data_table"));

                    }
                    if ("unit_measurement".equals(key)) {
                        datamap.put("indicator_status", stringObjectMap.get("indicator_status"));

                    }
                    if ("indicator_status".equals(key)) {
                        datamap.put("large_screen_link", stringObjectMap.get("large_screen_link"));

                    }
                    if ("name”".equals(key)) {
                        datamap.put("order_channel", stringObjectMap.get("order_channel"));
                    }
                    if ("indicator_description”".equals(key)) {
                        datamap.put("data_granularity", stringObjectMap.get("data_granularity"));
                    }
                }
                data.add(datamap);
            }


            if (data != null && data.size() > 0) {
                int excelRow = 0;
                //获取字段信息
                Map<String, Class<?>> columnType = ExcelUtil.getColumnType(data.get(0));
                // 设置通用样式
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
                XSSFCellStyle style = xssfWorkbook.createCellStyle();
                style.setAlignment(HorizontalAlignment.CENTER); //居中
                style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
                style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
                style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
                style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
                style.setWrapText(true); //自动换行
                style.setHidden(true);//高度自动
                style.setFillBackgroundColor(HSSFColor.PALE_BLUE.index); //背景颜色
                sheet1.setDefaultColumnWidth(10); //设置宽度
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                //创建标题行
                Row titleRow = sheet1.createRow(excelRow++);
                int index = 0;
                for (String item : parentTargetinfoHeaders) {
                    //创建该行下的每一列，并写入标题数据
                    Cell cell = titleRow.createCell(index);
                    cell.setCellStyle(style);
                    cell.setCellValue(item);
                    index++;
                }
                index = 0;

                //设置内容行
                for (Map<String, Object> row : data) {
                    Row dataRow = sheet1.createRow(excelRow++);
                    //内层for循环创建每行对应的列，并赋值
                    int columnIndex = 0;
                    for (Map.Entry<String, Object> item : row.entrySet()) {
                        Cell cell = dataRow.createCell(columnIndex);
                        columnIndex++;
                        if (item.getValue() == null) {
                            continue;
                        }
                        Class<?> type = columnType.get(item.getKey());
                        if (Integer.class.equals(type)) {
                            cell.setCellValue(((Integer) item.getValue()).doubleValue());
                        } else if (Long.class.equals(type)) {
                            cell.setCellValue(new Double((Long) item.getValue()));
                        } else if (String.class.equals(type)) {
                            cell.setCellValue((String) item.getValue());
                        } else if (Date.class.equals(type)) {
                            cell.setCellValue((Date) item.getValue());
                        } else if (Timestamp.class.equals(type)) {
                            cell.setCellValue(sdf.format((Timestamp) item.getValue()));
                        } else if (BigDecimal.class.equals(type)) {
                            cell.setCellValue(((BigDecimal) item.getValue()).doubleValue());
                        } else if (Double.class.equals(type)) {
                            cell.setCellValue((Double) item.getValue());
                        }

                    }
                }
                //将整理好的excel数据写入流中
                wb.write(os);
            }
        } catch (IOException e) {
            log.error("Excel导出失败，ex", e);
        } finally {
            try {
                // 关闭输出流
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("Excel导出 流关闭失败，ex", e);
            }
        }
    }


    /**
     * 设置浏览器下载响应头
     */
    public static void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            fileName = new String(fileName.getBytes(),"GBK");
//            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1"));
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
        } catch (Exception ex) {
            log.error("设置响应对象失败，ex", ex);
        }
    }


    @Override
    public List<BusinessTargetinfoPO> getDimensionList(String  name) {
        String sql ="select * from tb_business_targetinfo where indicator_name like '%"+name+"%' and del_flag = 1 ";
        List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectDimensionList(sql);
        return list;
    }
}
