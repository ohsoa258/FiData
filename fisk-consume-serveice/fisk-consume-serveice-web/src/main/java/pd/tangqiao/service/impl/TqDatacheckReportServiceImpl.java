package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqDatacheckReportPO;
import pd.tangqiao.entity.TqDatacheckReportQueryDTO;
import pd.tangqiao.entity.TqDatacheckReportVO;
import pd.tangqiao.mapper.TqDatacheckReportMapper;
import pd.tangqiao.service.TqDatacheckReportService;

import javax.annotation.Resource;

@Service("tqDatacheckReportService")
public class TqDatacheckReportServiceImpl extends ServiceImpl<TqDatacheckReportMapper, TqDatacheckReportPO> implements TqDatacheckReportService {

    @Resource
    TqDatacheckReportMapper mapper;
    @Override
    public Page<TqDatacheckReportVO> getAll(TqDatacheckReportQueryDTO dto) {
        return mapper.getAll(dto.page,dto);
    }

    @Override
    public ResultEnum addData(TqDatacheckReportPO po) {
        this.save(po);
        return ResultEnum.SUCCESS;
    }
}
