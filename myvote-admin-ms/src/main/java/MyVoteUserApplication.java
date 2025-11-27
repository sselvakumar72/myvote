import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.lvt.apps.myvote.admin.ms", "com.lvt.apps.common"})
public class MyVoteUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyVoteUserApplication.class, args);
    }

}
