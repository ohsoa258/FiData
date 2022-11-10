package com.fisk.license.test;

import com.fisk.common.core.utils.LicenseEnCryptUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;

/**
 * @author dick
 * @version 1.0
 * @description License测试类
 * @date 2022/11/9 13:20
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LicenseTest {

    /**
     * @return void
     * @description 加密明文字符串
     * @author dick
     * @date 2022/11/10 17:58
     * @version v1.0
     * @params
     */
    @Test
    public void testEncrypt() {
        String content = "红网时刻5月17日讯（记者 汪衡 通讯员 颜雨彬 汪丹）“场面壮观，气势磅礴，简直就是一部抗洪抢险水域救援大片！”抗洪抢险水域救援实战训练刚一结束，现场围观的群众纷纷发出了感叹。5月16日，湖南省消防救援总队在长沙市望城区千龙湖举办舟艇操作员培训，长沙、株洲、湘潭等40余名消防指战员参加，开展了一场抗洪抢险水域救援实战训练。" +
                "夏以来，随着湖南防汛救援任务的日趋繁重。为提高全省消防救援队伍水域救援能力，充分发挥应急救援“主力军”和“国家队”的作用，湖南省消防救援总队组织了这次为期35天的培训。依托长沙、岳阳、常德3支省级水域救援队设立了3个培训点，全省653名学员分17期参加培训。此次培训采用课堂授课、实地讲解、实操训练等多种形式，培训内容贴近实战，主要包括OSO驾驶、快速出入库、翻船自救、故障排除、200米游泳、受限控艇等项目。省消防救援总队相关负责人介绍，为了加快适应“全灾种、大应急”的职能定位，全面做好“防大汛、抢大险、救大灾”的准备工作，湖南消防救援队伍未雨绸缪，组建了1支省级抗洪抢险救援队、13支市级抗洪抢险救援队，有水域救援任务的消防救援站均成立了1支站级抗洪抢险救援队，总人数2300余人。此外，省消防救援总队还把重型工程机械救援大队、航空救援大队作为抢险救援机动力量纳入调度体系，充分发挥消防救援队伍装备技术优势，最大限度地挽救生命，减少灾害损失。";
        //生成密钥的 key 与 生成算法参数规范的 key 加解密必须一致，它就像是一把钥匙或者口令，不能忘记.
        //AES 加密算法时必须是 16 个字节，DES 时必须是 8 字节.
        try{
            String encrypt =LicenseEnCryptUtils.encrypt(content);
            System.out.println("源内容：\n" + content);
            System.out.println("加密后：\n" + encrypt);
            System.out.println("解密后：\n" + LicenseEnCryptUtils.decrypt(encrypt));
        }catch (Exception ex){

        }

    }

    /**
     * @return void
     * @description 解密明文字符串
     * @author dick
     * @date 2022/11/10 17:58
     * @version v1.0
     * @params
     */
    @Test
    public void testDecrypt() {
    }

}
