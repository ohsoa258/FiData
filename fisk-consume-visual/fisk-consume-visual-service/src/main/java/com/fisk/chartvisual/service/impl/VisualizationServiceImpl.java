package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.DataSourceDTO;
import com.fisk.chartvisual.entity.ChartImagePO;
import com.fisk.chartvisual.map.VisualizationMap;
import com.fisk.chartvisual.mapper.ChartImageMapper;
import com.fisk.chartvisual.service.BuildSqlService;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.service.IDataSourceConManageService;
import com.fisk.chartvisual.service.VisualizationService;
import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.client.DataModelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author WangYan
 * @date 2022/1/12 14:30
 */
@Service
public class VisualizationServiceImpl implements VisualizationService {

    @Resource
    BuildSqlService BuildSqlService;
    @Resource
    IDataService db;
    @Resource
    ChartImageMapper chartImageMapper;
    @Resource
    DataModelClient client;
    @Resource
    private IDataSourceConManageService service;

    @Value("${file.uploadurl}")
    private String uploadPath;

    @Override
    public DataServiceResult buildSql(ChartQueryObjectVO objectVO) {
        DataServiceResult dataServiceResult = new DataServiceResult();

        switch (objectVO.type) {
            case DMP:
                dataServiceResult.setData(BuildSqlService.query(VisualizationMap.INSTANCES.dataDoFields(objectVO.columnDetails), objectVO.id));
                return dataServiceResult;
            case VIEW:
                ChartQueryObject object = VisualizationMap.INSTANCES.dataDoObject(objectVO);
                object.setTableName(objectVO.columnDetails.get(1).fieldTableName);
                return db.query(object);
            case MDX:
                return db.querySsas(VisualizationMap.INSTANCES.dataToObjectSsas(objectVO));
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        //如果文件夹不存在，创建
        File fileP = new File(uploadPath);

        if (!fileP.isDirectory()) {
            //递归生成文件夹
            fileP.mkdirs();
        }
        String fileName = "";
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        if (originalFilename.endsWith(".jpg")) {
            fileName = String.format("%s.jpg", uuid);
        } else if (originalFilename.endsWith(".png")) {
            fileName = String.format("%s.jpg", uuid);
        } else if (originalFilename.endsWith(".jpeg")) {
            fileName = String.format("%s.jpeg", uuid);
        } else if (originalFilename.endsWith(".bmp")) {
            fileName = String.format("%s.bmp", uuid);
        } else {
            throw new FkException(ResultEnum.VISUAL_IMAGE_ERROR);
        }

        try {
            file.transferTo(new File(fileP, fileName));
        } catch (IOException e) {
            throw new FkException(ResultEnum.ERROR);
        }

        // 图片完整路径
        String imagePath = "/file/chartvisual/componentfile/" + fileName;

        // 保存到数据库
        ChartImagePO chartImage = new ChartImagePO();
        chartImage.setImagePath(imagePath);
        chartImageMapper.insert(chartImage);
        return imagePath;
    }

    @Override
    public ResultEntity<List<DataDomainVO>> listDataDomain(DataSourceDTO dto) {
        switch (dto.getType()){
            case DMP:
                return client.getAll();
            case VIEW:
                return service.listDataDomain(dto.getId());
            case MDX:
                return service.SSASDataStructure(dto.getId());
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
