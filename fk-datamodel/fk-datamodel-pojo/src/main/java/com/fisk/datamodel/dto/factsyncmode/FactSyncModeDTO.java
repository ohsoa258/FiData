package com.fisk.datamodel.dto.factsyncmode;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactSyncModeDTO {
    public int id;
    /**
     * 同步方式；1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；
     *
     * 全量：每次都全量覆盖整张表；
     *
     * 时间戳增量：根据表字段中配置的业务主键和时间戳字段进行增量覆盖；
     *
     * 业务时间覆盖：根据配置的业务时间字段进行业务时间覆盖：
     */
    public int syncMode;
    /**
     * 同步事实表id
     */
    public int syncFactId;
    /**
     * 同步事实字段id
     */
    public int syncFactFieldId;
    /**
     * 1:  取上一个月数据,覆盖上一个月数据
     * 2:  取当月数据,覆盖当月数据
     * 3:  当月
     * 4:  取上一年数据,覆盖上一年
     * 5:  取当年数据,覆盖当年
     */
    public int businessFlag;
    /**
     * 当月具体几号
     */
    public int businessDay;
    /**
     * 自定义插入条件：定义删除之后获取插入条件的数据进行插入
     */
    public String customInsertCondition;
    /**
     * 自定义删除条件：定义每次同步的时候删除我们已有的数据条件：
     */
    public String customDeleteCondition;
}
