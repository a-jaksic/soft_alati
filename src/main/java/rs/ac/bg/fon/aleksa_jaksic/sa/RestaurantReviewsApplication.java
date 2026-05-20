package rs.ac.bg.fon.aleksa_jaksic.sa;
import io.github.cdimascio.dotenv.Dotenv;
import rs.ac.bg.fon.aleksa_jaksic.sa.files.StorageConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageConfiguration.class)
public class RestaurantReviewsApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(RestaurantReviewsApplication.class, args);
    }

}
