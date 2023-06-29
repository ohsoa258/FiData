package com.fisk.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.system.dto.WeChatUserDTO;
import com.fisk.system.dto.emailserver.EmailServerDTO;
import com.fisk.system.dto.emailserver.EmailServerEditDTO;
import com.fisk.system.dto.emailserver.EmailServerQueryDTO;
import com.fisk.system.entity.EmailServerPO;
import com.fisk.system.enums.EmailServerTypeEnum;
import com.fisk.system.map.EmailServerMap;
import com.fisk.system.mapper.EmailServerMapper;
import com.fisk.system.service.IEmailServerManageService;
import com.fisk.system.vo.emailserver.EmailServerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 邮件配置实现类
 * @date 2022/3/23 12:56
 */
@Slf4j
@Service
public class EmailServerManageImpl extends ServiceImpl<EmailServerMapper, EmailServerPO> implements IEmailServerManageService {

    @Override
    public Page<EmailServerVO> getAll(EmailServerQueryDTO query) {
        Page<EmailServerVO> pageAll = baseMapper.getPageAll(query.page, query.keyword);
        if (pageAll != null && !CollectionUtils.isEmpty(pageAll.getOrders())) {
            pageAll.getRecords().forEach(t -> {
                t.setEmailServerType(EmailServerTypeEnum.getEnum(t.getEmailServerTypeValue()));
            });
        }
        return pageAll;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(EmailServerDTO dto) {
        //第一步：转换DTO对象为PO对象
        EmailServerPO emailServerPO = EmailServerMap.INSTANCES.dtoToPo(dto);
        if (emailServerPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        int insert = baseMapper.insert(emailServerPO);
        if (insert <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editData(EmailServerEditDTO dto) {
        EmailServerPO emailServerPO = baseMapper.selectById(dto.id);
        if (emailServerPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第一步：转换DTO对象为PO对象
        emailServerPO = EmailServerMap.INSTANCES.dtoToPo_Edit(dto);
        if (emailServerPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        int insert = baseMapper.updateById(emailServerPO);
        if (insert <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int id) {
        EmailServerPO emailServerPO = baseMapper.selectById(id);
        if (emailServerPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return baseMapper.deleteByIdWithFill(emailServerPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<EmailServerVO> getEmailServerList() {
        List<EmailServerVO> all = baseMapper.getAll();
        if (!CollectionUtils.isEmpty(all)) {
            all.forEach(t -> {
                t.setEmailServerType(EmailServerTypeEnum.getEnum(t.getEmailServerTypeValue()));
            });
        }
        return all;
    }

    @Override
    public EmailServerVO getEmailServerById(int id) {
        EmailServerVO byId = baseMapper.getById(id);
        if (byId != null) {
            byId.setEmailServerType(EmailServerTypeEnum.getEnum(byId.getEmailServerTypeValue()));
        }
        return byId;
    }

    @Override
    public EmailServerVO getDefaultEmailServer() {
        EmailServerVO emailServerVO = baseMapper.getDefaultEmailServer();
        if (emailServerVO != null) {
            emailServerVO.setEmailServerType(EmailServerTypeEnum.getEnum(emailServerVO.getEmailServerTypeValue()));
        }
        return emailServerVO;
    }

    public List<WeChatUserDTO> searchWechatUser(int serverConfigId, String recipients )
    {
        List<WeChatUserDTO> weChatUserList = new ArrayList<>();
        try{
            //获取配置信息
            EmailServerVO byId = baseMapper.getById(serverConfigId);
            //获取企业微信AccessToken
            String accessTokenUrl  = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + byId.wechatCorpId + "&corpsecret=" + byId.wechatAppSecret + "";
            String stringAccessToken = HttpGet(accessTokenUrl);
            log.info( "企业微信获取token结果集: "+stringAccessToken);
            JSONObject json = JSONObject.parseObject(stringAccessToken);
            String accessToken = json.getString("access_token");
            if (accessToken == null)
            {
                log.info(stringAccessToken);
            }
            //String  department ="https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+accessToken;
            //String StringDepartment= sendGetRequest(department);
            //JSONObject jsonDepartmentList = JSONObject.parseObject(StringDepartment);

            //获取部门用户 部门ID为1 表示整个部门
            //String userListUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token=" + accessToken + "&department_id=1&fetch_child=1";

            //通过手机号 获取用户Id
            String user = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserid?access_token="+accessToken;
            Map<String, Object> params = new HashMap<>();
            params.put("mobile", recipients);
            String stringUserList = HttpPost(user, JSON.toJSONString(params));
            log.info( "企业微信通过手机号获取用户Id结果集: "+stringUserList);
            JSONObject jsonUserList = JSONObject.parseObject(stringUserList);
            String userString = jsonUserList.getString("userid");
            log.info( "取出用户Id: "+stringUserList);

            if(userString != null &&  userString != "")
            {
                WeChatUserDTO wechat = new WeChatUserDTO();
                wechat.name = recipients;
                wechat.userid =  userString;
                weChatUserList.add(wechat);
            }
            else{
                log.info( "用户Id为空: " + userString);
                return null;
            }

        }
        catch (Exception e)
        {
            log.debug("【searchWechatUser】 ex：" + e);
            e.printStackTrace();
        }
        return  weChatUserList;
    }

    public static String HttpGet(String url) throws UnsupportedEncodingException {
        StringBuilder response = new StringBuilder();
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            //con.setRequestProperty("Content-Type", "charset=UTF-8");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String decodedResponse = URLDecoder.decode(response.toString(), "UTF-8");
        log.info("decodedResponse：" + decodedResponse);
        return decodedResponse;
    }


    public static String HttpPost(String url, String param) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] postData = param.getBytes(StandardCharsets.UTF_8);
            os.write(postData);
            os.flush();
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private String sendGetRequest(String url) throws IOException {
        String result = null;
        try {
            HttpClient client = new DefaultHttpClient();
            // get请求
            HttpGet request = new HttpGet(url);
            request.setHeader("Content-Type", "application/json; charset=utf-8");

//            // 页面自定义的请求头信息
//            if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(dto.headersParams)) {
//                dto.headersParams.forEach(request::setHeader);
//            }

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            //解析返回数据
            result = EntityUtils.toString(entity, "UTF-8");
            log.info("执行httpRequest方法成功,【返回信息为：】,{}", result);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            log.error("AE92: 执行get请求失败,失败原因为: " + e);
            throw new FkException(ResultEnum.SEND_GET_REQUEST_ERROR);
        }
        return result;
    }


}