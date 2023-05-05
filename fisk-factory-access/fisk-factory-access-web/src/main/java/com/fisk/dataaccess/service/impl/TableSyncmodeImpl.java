package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.dto.table.TableSyncmodeDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.entity.TableSyncmodePO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.dataaccess.map.TableSyncModeMap;
import com.fisk.dataaccess.mapper.TableSyncmodeMapper;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.service.ITableSyncmode;
import com.fisk.dataaccess.vo.datafactory.SyncTableCountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableSyncmodeImpl extends ServiceImpl<TableSyncmodeMapper, TableSyncmodePO> implements ITableSyncmode {

    @Resource
    private ITableAccess tableAccess;

    @Override
    public SyncTableCountVO mainPageCheck(Long appid) {
        //查询当前应用下的所有物理表
        LambdaQueryWrapper<TableAccessPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(TableAccessPO::getId).eq(TableAccessPO::getAppId, appid);
        //获取到当前应用下的物理表id集合
        List<TableAccessPO> tableAccessPOIdList = tableAccess.list(lambdaQueryWrapper);
        //判断当前应用下有无物理表
        if (CollectionUtils.isEmpty(tableAccessPOIdList)) {
            log.info("当前应用下不存在物理表");
            return getSyncTableCountVO(0, 0, 0, 0, 0);
        }
        //取出集合内的表id
        List<Long> tabbleIds = new ArrayList<>();
        for (TableAccessPO tableAccessPO: tableAccessPOIdList) {
            tabbleIds.add(tableAccessPO.getId());
        }

        //查询当前应用对应物理表的同步类型
        LambdaQueryWrapper<TableSyncmodePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(TableSyncmodePO::getSyncMode).in(TableSyncmodePO::getId, tabbleIds);
        List<TableSyncmodePO> tableSyncmodePOList = list(wrapper);
        List<TableSyncmodeDTO> tableSyncmodeDTOList = TableSyncModeMap.INSTANCES.listPoToDto(tableSyncmodePOList);

        //预装载表同步类型
        //追加 1
        int appendCoverCount = 0;
        //全量 2
        int fullCoverCount = 0;
        //业务覆盖 3
        int businessKeyCoverCount = 0;
        //业务时间 4
        int businessTimeCoverCount = 0;

        for (TableSyncmodeDTO t : tableSyncmodeDTOList) {
            //这里不要关心枚举类型的对错，关心数值即可
            if (t.getSyncMode() == syncModeTypeEnum.FULL_VOLUME.getValue()) {
                //追加
                appendCoverCount++;
            } else if (t.getSyncMode() == syncModeTypeEnum.ADD.getValue()) {
                //全量
                fullCoverCount++;
            } else if (t.getSyncMode() == syncModeTypeEnum.INCREMENT_MERGE.getValue() || t.getSyncMode() == syncModeTypeEnum.INCREMENT_DELINSERT.getValue()) {
                //业务主键覆盖
                businessKeyCoverCount++;
            } else if (t.getSyncMode() == syncModeTypeEnum.TIME_INCREMENT.getValue()) {
                //业务时间覆盖
                businessTimeCoverCount++;
            }
        }
        //当前应用下的已同步的表总数
        int totalCount = appendCoverCount+fullCoverCount+businessKeyCoverCount+businessTimeCoverCount;
        return getSyncTableCountVO(appendCoverCount, fullCoverCount, businessKeyCoverCount, businessTimeCoverCount, totalCount);
    }

    /**
     * 获取SyncTableCountVO对象
     *
     * @return
     */
    public static SyncTableCountVO getSyncTableCountVO(Integer appendCoverCount, Integer fullCoverCount,
                                                       Integer businessKeyCoverCount, Integer businessTimeCoverCount, Integer count) {
        SyncTableCountVO syncTableCountVO = new SyncTableCountVO();
        syncTableCountVO.setAppendCoverCount(appendCoverCount);
        syncTableCountVO.setFullCoverCount(fullCoverCount);
        syncTableCountVO.setBusinessKeyCoverCount(businessKeyCoverCount);
        syncTableCountVO.setBusinessTimeCoverCount(businessTimeCoverCount);
        syncTableCountVO.setTotalCount(count);
        return syncTableCountVO;
    }

}
