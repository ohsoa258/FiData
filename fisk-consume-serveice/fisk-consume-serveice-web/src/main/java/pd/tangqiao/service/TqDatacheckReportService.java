package pd.tangqiao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import pd.tangqiao.entity.TqDatacheckReportPO;
import pd.tangqiao.entity.TqDatacheckReportQueryDTO;
import pd.tangqiao.entity.TqDatacheckReportVO;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-29 10:56:49
 */
public interface TqDatacheckReportService extends IService<TqDatacheckReportPO> {

    Page<TqDatacheckReportVO> getAll(TqDatacheckReportQueryDTO po);

    ResultEnum addData(TqDatacheckReportPO po);
}

