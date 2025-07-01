package NewAcceptanceTesting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fakezone.fakezone.FakezoneApplication;

import javax.sql.DataSource;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")
public class H2SanityCheckTest {
    // @Autowired
    // DataSource dataSource;

    // @Test
    // void testH2Connection() throws Exception {
    //     System.out.println("DB URL: " + dataSource.getConnection().getMetaData().getURL());
    //     Assertions.assertTrue(dataSource.getConnection().getMetaData().getURL().contains("h2"));
    // }
}
