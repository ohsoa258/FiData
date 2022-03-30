package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.atlas.IAtlasBuildInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/7 15:57
 * Description:
 */
@Component
@Slf4j
public class BuildAtlasInstanceTaskListener {

    @Resource
    IAtlasBuildInstance atlas;
    @Resource
    DataAccessClient dc;

    public void msg(String dataInfo, Acknowledgment acke) {
        log.info("data:" + dataInfo);
        AtlasEntityQueryDTO inpData = JSON.parseObject(dataInfo, AtlasEntityQueryDTO.class);
        ResultEntity<AtlasEntityDTO> queryRes = dc.getAtlasEntity(Long.parseLong(inpData.appId));
        log.info("query data :" + JSON.toJSONString(queryRes));
        AtlasWriteBackDataDTO awbd = new AtlasWriteBackDataDTO();
        AtlasEntityDTO ae = JSON.parseObject(JSON.toJSONString(queryRes.data), AtlasEntityDTO.class);
        //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //region  创建实例
        //endregion
        acke.acknowledge();
    }
}
