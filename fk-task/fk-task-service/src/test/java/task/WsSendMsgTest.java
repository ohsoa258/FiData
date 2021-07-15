package task;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class WsSendMsgTest {

    @Test
    public void MockSendMsg() {
        try {
            String token = URLDecoder.decode("Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIzMGFmNjYwMmExNGU0MzJkOWQ0NjI0MGFkZWUzNDVhMSIsInVzZXIiOiJ7XCJpZFwiOjM3LFwidXNlcm5hbWVcIjpcImd1b3l1XCJ9IiwiaWQiOiIzNyJ9.8swyDu5fqJj70H7ACIjx4ubKGJPzgrpx7tBpfLdjIRI", "UTF-8");
            System.out.println(token);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
