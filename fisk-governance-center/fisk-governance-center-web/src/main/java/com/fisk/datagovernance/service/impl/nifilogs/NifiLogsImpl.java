package com.fisk.datagovernance.service.impl.nifilogs;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelTreeDTO;
import com.fisk.common.service.accessAndModel.LogPageQueryDTO;
import com.fisk.common.service.accessAndModel.NifiLogResultDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.datagovernance.service.nifilogs.INifiLogs;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.mdm.client.MdmClient;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class NifiLogsImpl implements INifiLogs {

    @Resource
    private DataAccessClient accessClient;

    @Resource
    private DataModelClient modelClient;

    @Resource
    private MdmClient mdmClient;

    @Resource
    private PublishTaskClient taskClient;

    /**
     * 同步日志页面获取数接和数仓的 应用--表 树形结构
     *
     * @return
     */
    @Override
    public AccessAndModelTreeDTO getAccessAndModelTree() {
        AccessAndModelTreeDTO accessAndModelTreeDTO = new AccessAndModelTreeDTO();
        try {
            //获取数据接入 应用-表树结构
            ResultEntity<List<AccessAndModelAppDTO>> allAppAndTables = accessClient.getAllAppAndTables();
            if (allAppAndTables.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.GET_ACCESS_TREE_FAILURE);
            }
            accessAndModelTreeDTO.setAccessTree(allAppAndTables.getData());

            //获取数仓建模 业务域-表树结构
            ResultEntity<List<AccessAndModelAppDTO>> allAreaAndTables = modelClient.getAllAreaAndTables();
            if (allAreaAndTables.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.GET_MODEL_TREE_FAILURE);
            }

            //获取主数据 模型实体-表树结构
            ResultEntity<List<AccessAndModelAppDTO>> allModelAndEntitys = mdmClient.getAllModelAndEntitys();
            if (allModelAndEntitys.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.GET_MODEL_TREE_FAILURE);
            }
            accessAndModelTreeDTO.setMdmTree(allModelAndEntitys.getData());
        } catch (Exception e) {
            log.error("同步日志页面获取数接数仓主数据树形结构失败：" + e);
            throw new FkException(ResultEnum.ERROR);
        }
        return accessAndModelTreeDTO;
    }

    /**
     * 同步日志页面获取数接/数仓的指定表的nifi同步日志
     *
     * @param dto
     * @return
     */
    @Override
    public Page<NifiLogResultDTO> getTableNifiLogs(LogPageQueryDTO dto) {
        //数接物理表表名前面要拼上架构名
        if (dto.getTableType() == 3) {
            Integer tblId = dto.getTblId();
            ResultEntity<AppRegistrationDTO> app = accessClient.getAppByTableAccessId(tblId);
            if (app.getCode() != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.GET_ACCESS_APP_ERROR);
            }
            String appAbbreviation = app.getData().getAppAbbreviation();
            dto.setTableName(appAbbreviation + "." + dto.getTableName());
        }
        return taskClient.getDwAndAccessTblNifiLog(dto);
    }
}
