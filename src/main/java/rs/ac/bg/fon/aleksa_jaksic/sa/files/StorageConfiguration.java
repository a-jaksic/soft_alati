package rs.ac.bg.fon.aleksa_jaksic.sa.files;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "storage.local")
@Getter
@Setter
public class StorageConfiguration {

    private String baseDir;
    private List<String> allowedContentTypes;
    private long maxSizeBytes;

}
