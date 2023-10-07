package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataservice.entity.TableApiLogPO;
import com.fisk.dataservice.vo.tableapi.ConsumeServerVO;
import com.fisk.dataservice.vo.tableapi.TopFrequencyVO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-10-07 10:54:30
 */
public interface ITableApiLogService extends IService<TableApiLogPO> {


    ConsumeServerVO getConsumeServer();

    List<TopFrequencyVO> getTopFrequency();
}

