package com.fisk.mdm.vo.masterdata;

import com.alibaba.fastjson.JSONObject;
import com.fisk.mdm.vo.attribute.AttributeVO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BathUploadMemberVO {

    public int addCount;

    public int count;

    public int entityId;

    public String entityName;

    public int errorCount;

    public int successCount;

    public int updateCount;

    public int versionId;

    public List<AttributeVO> attribute;

    public List<JSONObject> members;

    public String key;

}
