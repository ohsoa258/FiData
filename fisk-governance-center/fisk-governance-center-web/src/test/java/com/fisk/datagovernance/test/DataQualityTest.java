package com.fisk.datagovernance.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.utils.similarity.CosineSimilarity;
import com.fisk.datagovernance.service.impl.dataops.DataOpsDataSourceManageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量测试类
 * @date 2022/4/25 13:37
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DataQualityTest {
    /**
     * @return void
     * @description 相似度规则测试
     * @author dick
     * @date 2022/4/25 13:42
     * @version v1.0
     * @params
     */
    @Test
    public void similarityTest() {
        double similarity = CosineSimilarity.getSimilarity("产品销售渠道", "上海农产品销售渠道");
        System.out.println("相似度i计算比例：" + similarity);
        similarity = CosineSimilarity.getSimilarity("上海", "上海农产品销售渠道");
        System.out.println("相似度i计算比例：" + similarity);
        similarity = CosineSimilarity.getSimilarity("上海农产品销售渠道", "上海农产品销售渠道");
        System.out.println("相似度i计算比例：" + similarity);
    }

    @Test
    public void testSendEmail(){
//        JavaMailSenderImpl sender = new JavaMailSenderImpl();
//        sender.setHost("192.168.1.45");
//        sender.setPort(25);
//        //sender.setUsername("dick@fisksoft.com");
//        //sender.setPassword("Lijiayun@0424...");
//        Properties properties = sender.getJavaMailProperties();
//        properties.put("mail.smtp.auth", false);
//        //properties.put("mail.smtp.timeout", "25000");
//        //properties.put("mail.smtp.starttls.enable", "true");
//
//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//        simpleMailMessage.setFrom("dick@fisksoft.com");
//
//        simpleMailMessage.setTo("jianwen@fisksoft.com.cn");
//        simpleMailMessage.setSubject("test");
//        String text = "email.getText()";
//        simpleMailMessage.setText(text);
//
//        try{
//            sender.send(simpleMailMessage);
//        } catch (Exception e) {
//            throw e;
//        }
        Properties  mailpro = new Properties();

        mailpro.setProperty("mail.smtp.host", "192.168.1.45");
        mailpro.setProperty("mail.smtp.port", "25");
        mailpro.setProperty("mail.smtp.auth", "false");
        mailpro.setProperty("mail.smtp.starttls.enable", "false");
        mailpro.setProperty("mail.transport.protocol", "smtp");

        Session session = Session.getDefaultInstance(mailpro);

        session.setDebug(true);

        Message msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress("fiskcolud@fisksoft.com"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("dick@fisksoft.com"));

            msg.setSubject("测试免认证方式发送邮件！！！");

            msg.setText("测试一下，邮件来自 http://www.donews.net/lizongbo ");

            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return void
     * @description pg数据库信息写入redis
     * @author dick
     * @date 2022/4/25 13:42
     * @version v1.0
     * @params
     */
    @Test
    public void setDataOpsDataSource() {
        DataOpsDataSourceManageImpl dataOpsDataSourceManage = new DataOpsDataSourceManageImpl();
        dataOpsDataSourceManage.setDataOpsDataSource();
    }

    /**
     * @return void
     * @description json读取测试
     * @author dick
     * @date 2022/4/25 13:42
     * @version v1.0
     * @params
     */
    @Test
    public void getJsonArray() {
        JSONArray jSONArray = new JSONArray();
        JSONObject jb = new JSONObject();
        jb.put("id", 1);
        jb.put("name", "s");
        jb.put("nameM", 1.894);
        jSONArray.add(jb);
        JSONObject j1 = new JSONObject();
        j1.put("id", null);
        j1.put("name", "s");
        j1.put("nameM", 1.894);
        jSONArray.add(j1);
        for (int i = 0; i < jSONArray.size(); i++) {
            JSONObject jsonObject = jSONArray.getJSONObject(i);
            String a1= jsonObject.getString("id");
            String a2= jsonObject.getString("name");
            String a3= jsonObject.getString("nameM");
            System.out.println(a1+","+a2+","+a3);
        }
    }
}
