package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.dataSource.DataSourceDTO;
import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.core.response.ResultEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/1/12 14:29
 */
public interface VisualizationService {

    /**
     * 报表可视化生成Sql
     * @param objectVO
     * @return
     */
    DataServiceResult buildSql(ChartQueryObjectVO objectVO);

    /**
     * 将图片上传到服务器
     * @param file
     * @return
     */
    String upload(MultipartFile file);

    /**
     * 获取白泽、视图、CUDB数据源
     * @param dto
     * @return
     */
    ResultEntity<List<DataDomainVO>> listDataDomain(DataSourceDTO dto);
}
