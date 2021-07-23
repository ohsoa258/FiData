package fk.atlas.api;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/16 11:49
 * Description:
 */
public class test {
    public static void main(String[] args) {
        AtlasClient ac=new AtlasClient("http://192.168.1.250:21000", "admin", "admin");

        System.out.println(ac.GetEntity());
    }
}
