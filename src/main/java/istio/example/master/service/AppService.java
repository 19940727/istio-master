package istio.example.master.service;

import istio.example.master.config.KubernetesProperties;
import istio.example.master.config.RefreshProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class AppService {

    @Autowired
    private KubernetesProperties kubernetesProperties;

    @Autowired
    private RefreshProperties refreshProperties;

    @Autowired
    private Environment environment;

    public String getApplicationValue() {
        return refreshProperties.getApplicationValue();
    }

    public String getDevValue() {
        return refreshProperties.getDevValue();
    }

    public String getValue() {
        return kubernetesProperties.getValue();
    }
}
