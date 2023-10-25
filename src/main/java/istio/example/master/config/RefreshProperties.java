package istio.example.master.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
public class RefreshProperties {

    @Value("${test.dev.value}")
    private String devValue;

    @Value("${test.application.value}")
    private String applicationValue;

}
