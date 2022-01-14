package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.entity.ChartImagePO;
import com.fisk.chartvisual.map.VisualizationMap;
import com.fisk.chartvisual.mapper.ChartImageMapper;
import com.fisk.chartvisual.service.BuildSqlService;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.service.VisualizationService;
import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

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
                return db.query(VisualizationMap.INSTANCES.dataDoObject(objectVO));
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
        long currentTimeMillis = System.currentTimeMillis();
        if (originalFilename.endsWith(".jpg")) {
            fileName = String.format("%s.jpg", currentTimeMillis);
        } else if (originalFilename.endsWith(".png")) {
            fileName = String.format("%s.jpg", currentTimeMillis);
        } else if (originalFilename.endsWith(".jpeg")) {
            fileName = String.format("%s.jpeg", currentTimeMillis);
        } else if (originalFilename.endsWith(".bmp")) {
            fileName = String.format("%s.bmp", currentTimeMillis);
        } else {
            throw new FkException(ResultEnum.VISUAL_IMAGE_ERROR);
        }

        try {
            file.transferTo(new File(fileP, fileName));
        } catch (IOException e) {
            throw new FkException(ResultEnum.ERROR);
        }

        // 图片完整路径
        String imagePath = fileP + "/" + fileName;

        // 保存到数据库
        ChartImagePO chartImage = new ChartImagePO();
        chartImage.setImagePath(imagePath);
        chartImageMapper.insert(chartImage);
        return imagePath;
    }
}
