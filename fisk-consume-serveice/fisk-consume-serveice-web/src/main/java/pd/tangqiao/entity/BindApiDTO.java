package pd.tangqiao.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-10-30
 * @Description:
 */
@Data
public class BindApiDTO {
    public Integer appId;
    public List<Integer> apiIds;
}
