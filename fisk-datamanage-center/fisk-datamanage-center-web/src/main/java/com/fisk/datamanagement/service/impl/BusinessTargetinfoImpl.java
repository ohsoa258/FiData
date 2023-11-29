package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.office.excel.ExcelUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDefsDTO;
import com.fisk.datamanagement.entity.BusinessSynchronousPO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;
import com.fisk.datamanagement.mapper.BusinessTargetinfoMapper;
import com.fisk.datamanagement.service.BusinessTargetinfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
    private static final String[] parentTargetinfoHeaders = {"一级分类", "二级分类", "负责部门", "指标编码", "指标类型", "指标名称", "指标描述/口径", "指标范围",
            "计量单位", "统计周期", "指标公式", "指标脚本", "指标来源", "数据筛选条件", "来源系统", "来源数据表", "指标状态", "应用", "订单渠道"};


    /**
     * 查询指标明细数据
     *
     * @param pid
     * @return
     */
    @Override
    public List<BusinessTargetinfoPO> SelectClassification(String pid) {
        List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectClassification(pid);
        return list;
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
        uploadExcelAboutUser(response, "test.xlsx", list);
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
            ExcelUtil.setResponseHeader(response, fileName);
            //获取输出流
            os = response.getOutputStream();
            //内存中保留1000条数据，以免内存溢出，其余写入硬盘
            SXSSFWorkbook wb = new SXSSFWorkbook(1000);
            //获取该工作区的第一个sheet
            Sheet sheet1 = wb.createSheet("sheet1");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (dataList != null && dataList.size() > 0) {
                int excelRow = 0;
                //获取字段信息
                Map<String, Class<?>> columnType = ExcelUtil.getColumnType(dataList.get(0));

                //创建标题行
                Row titleRow = sheet1.createRow(excelRow++);
                int index = 0;
                for (String item : parentTargetinfoHeaders) {
                    //创建该行下的每一列，并写入标题数据
                    Cell cell = titleRow.createCell(index);
                    cell.setCellValue(item);
                    index++;
                }
                index = 0;

                //设置内容行
                for (Map<String, Object> row : dataList) {
                    Row dataRow = sheet1.createRow(excelRow++);
                    //内层for循环创建每行对应的列，并赋值
                    int columnIndex = 2;
                    for (Map.Entry<String, Object> item : row.entrySet()) {
                        Cell cell = dataRow.createCell(columnIndex);
                        columnIndex++;
                        if (item.getValue() == null) {
                            continue;
                        }
                        Class<?> type = columnType.get(item.getKey());
                        if("pid".equals(item.getKey())){
                            continue;
                        }
                        if("pid".equals(item.getKey())){
                            continue;
                        }
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
}
