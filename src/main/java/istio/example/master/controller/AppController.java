package istio.example.master.controller;

import istio.example.master.client.MinionClient;
import istio.example.master.service.AppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.kubernetes.commons.discovery.DefaultKubernetesServiceInstance;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@RestController
@RequestMapping("/api")
public class AppController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/services")
    public List<String> services() {
        return discoveryClient.getServices();
    }

    @GetMapping("/discoveryClient")
    public DiscoveryClient discoveryClient() {
        return discoveryClient;
    }

    @GetMapping("/instance")
    public List<ServiceInstance> instance() {
        List<ServiceInstance> list = new ArrayList<>();
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            list.addAll(discoveryClient.getInstances(service));
        }
        return list;
    }

    @GetMapping("/minionCall")
    public String minionCall() {
        //构建一个web客户端

        List<ServiceInstance> instances = discoveryClient.getInstances("minion");

        // {service-name}.{namespace}.svc.{cluster}.local:{service-port}
        String baseUrl = "http://%s.%s.svc.%s.local:%d";
        for (ServiceInstance instance : instances) {
            if (instance instanceof DefaultKubernetesServiceInstance k8sInstance) {
                String cluster = "cluster";

                if (k8sInstance.getCluster() != null) {
                    cluster = k8sInstance.getCluster();
                }

                baseUrl = String.format(baseUrl, instance.getServiceId(), ((DefaultKubernetesServiceInstance) instance).getNamespace(), cluster, instance.getPort());
            }
        }

        log.info("BaseURL：{}", baseUrl);

        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        //根据web客户端去构建服http服务的代理工厂
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
        //根据代理工厂创建代理类
        MinionClient minionClient = factory.createClient(MinionClient.class);
        return minionClient.application();
    }

    @Autowired
    private AppService appService;

    @GetMapping("/applicationValue")
    public String applicationValue() {
        return appService.getApplicationValue();
    }

    @GetMapping("/devValue")
    public String devValue() {
        return appService.getDevValue();
    }

    @GetMapping("/k8sValue")
    public String k8sValue() {
        return appService.getValue();
    }

    @Autowired
    private Environment environment;

    @GetMapping("/environment")
    public String environment() {
        return environment.getProperty("test.dev.value");
    }

}
