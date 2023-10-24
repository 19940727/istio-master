package istio.example.master.client;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class CloudNativeClientFactoryBean implements FactoryBean {

    private final Class<?> clazz;

    private String service;

    private String namespace;

    private String cluster;

    public CloudNativeClientFactoryBean(Class<?> clazz, String service, String namespace, String cluster) {
        this.clazz = clazz;
        this.service = service;
        this.namespace = namespace;
        this.cluster = cluster;
    }

    @Override
    public Object getObject() throws Exception {
        String baseUrl = "http://%s.%s.svc.%s.local:%d";

        if (StringUtils.isBlank(namespace)) {
            namespace = "istio-example";
        }
        if (StringUtils.isBlank(cluster)) {
            cluster = "cluster";
        }
        String url = String.format(baseUrl, service, namespace, cluster, 8080);

        WebClient webClient = WebClient.builder().baseUrl(url).build();
        //根据web客户端去构建服http服务的代理工厂
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
        //根据代理工厂创建代理类
        return factory.createClient(clazz);
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }
}
