package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsListDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsQueryDTO;
import com.fisk.datamodel.entity.DerivedIndicatorsAttributePO;
import com.fisk.datamodel.entity.DerivedIndicatorsPO;
import com.fisk.datamodel.map.DerivedIndicatorsMap;
import com.fisk.datamodel.mapper.DerivedIndicatorsMapper;
import com.fisk.datamodel.mapper.DerivedIndicatorsAttributeMapper;
import com.fisk.datamodel.service.IDerivedIndicators;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DerivedIndicatorsImpl
        extends ServiceImpl<DerivedIndicatorsAttributeMapper,DerivedIndicatorsAttributePO>
        implements IDerivedIndicators {

    @Resource
    DerivedIndicatorsMapper mapper;
    private static SqlSessionFactory sqlSessionFactory;

    @Override
    public Page<DerivedIndicatorsListDTO> getDerivedIndicatorsList(DerivedIndicatorsQueryDTO dto)
    {
        return mapper.queryList(dto.dto,dto);
    }

    @Override
    public ResultEnum deleteDerivedIndicators(long id)
    {
        DerivedIndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum addDerivedIndicators(DerivedIndicatorsDTO dto)
    {
        SqlSession session = sqlSessionFactory.openSession(false);
        boolean result=false;
        try
        {
            //判断是否重复
            QueryWrapper<DerivedIndicatorsPO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(DerivedIndicatorsPO::getDerivedName,dto.derivedName)
                    .eq(DerivedIndicatorsPO::getFactId,dto.factId);
            DerivedIndicatorsPO po=mapper.selectOne(queryWrapper);
            if (po!=null)
            {
                return ResultEnum.DATA_EXISTS;
            }
            int flat= mapper.insert(DerivedIndicatorsMap.INSTANCES.dtoToPo(dto));
            if (flat==0)
            {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            //派生指标聚合字段集合
            List<DerivedIndicatorsAttributePO> ids=new ArrayList<>();
            for (Integer item:dto.ids)
            {
                DerivedIndicatorsAttributePO model=new DerivedIndicatorsAttributePO();
                model.factAttributeId=item;
            }
            result= this.saveBatch(ids);
            if (!result)
            {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        catch (Exception ex)
        {
            log.error("{}方法执行失败: ", ex);
            //回滚
            session.rollback();
        }
        finally {
            // 关闭会话，释放资源
            session.close();
        }
        return result?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

}
