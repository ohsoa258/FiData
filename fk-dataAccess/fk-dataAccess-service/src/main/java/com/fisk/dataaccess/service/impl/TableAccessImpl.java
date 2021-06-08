package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.mapper.TableFieldsMapper;
import com.fisk.dataaccess.service.ITableAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: Lock
 */
@Service
public class TableAccessImpl extends ServiceImpl<TableAccessMapper, TableAccessPO> implements ITableAccess {

    @Autowired
    private TableFieldsImpl tableFieldsImpl;

//    @Resource
//    private TableFieldsMapper tableFieldsMapper;

    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO
     * @return
     */
    @Override
    @Transactional
    public ResultEnum addRTData(TableAccessDTO tableAccessDTO) {

        // 1.dto->po
        TableAccessPO tableAccessPO = tableAccessDTO.toEntity(TableAccessPO.class);
        // 0是实时物理表，1是非实时物理表
        tableAccessPO.setIsRealtime(0);
        // 实时物理表，需要提供数据同步地址
        tableAccessPO.setSyncSrc("");
        tableAccessPO.setDelFlag(1);

        // 2.保存tb_table_access数据
//        tableAccessPO.setDelFlag(1);
        boolean save1 = this.save(tableAccessPO);

        if (!save1) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }

        // 保存tb_table_fields数据
        TableFieldsDTO tableFieldsDTO = tableAccessDTO.getTableFieldsDTO();
        TableFieldsPO tableFieldsPO = tableFieldsDTO.toEntity(TableFieldsPO.class);
        tableFieldsPO.setTableAccessId(tableAccessPO.getId());
        // 1是业务时间，0非业务时间
        tableFieldsPO.setIsBusinesstime(0);
        // 1是时间戳，0非时间戳
        tableFieldsPO.setIsTimestamp(0);
        // 1是实时物理表的字段，0是非实时物理表的字段
        tableFieldsPO.setIsRealtime(1);
        tableFieldsPO.setDelFlag(1);

        boolean save2 = tableFieldsImpl.save(tableFieldsPO);

        return save2 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 删除数据
     *
     * @param id
     * @return
     */
    @Override
    public ResultEnum deleteData(long id) {

        // 1.删除tb_table_access数据
        TableAccessPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 假删除
        model.setDelFlag(0);
        boolean update = this.updateById(model);
        if (!update) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.删除tb_table_fields数据
        TableFieldsPO po = tableFieldsImpl.query().eq("table_access_id", id).one();
        po.setDelFlag(0);
        boolean success = tableFieldsImpl.updateById(po);

        return success ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 添加物理表(非实时)
     * @param tableAccessNDTO
     * @return
     */
    @Override
    public ResultEnum addNRTData(TableAccessNDTO tableAccessNDTO) {
        return null;
    }
}
