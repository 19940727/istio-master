package istio.example.master.client;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Map;
import java.util.Set;

public class CloudNativeClientDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry,
                                        BeanNameGenerator importBeanNameGenerator) {


        CloudNativeClientScanner cloudNativeClientScanner = new CloudNativeClientScanner(registry);
        cloudNativeClientScanner.addIncludeFilter((metadataReader, metadataReaderFactory) ->
                metadataReader.getAnnotationMetadata().hasAnnotation("istio.example.master.client.CloudNativeExchange")
        );

        Set<BeanDefinition> candidateComponents = cloudNativeClientScanner.findCandidateComponents("istio.example.master.client");

        for (BeanDefinition candidateComponent : candidateComponents) {

            if (candidateComponent instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
                AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
                Map<String, Object> attributes = metadata
                        .getAnnotationAttributes(CloudNativeExchange.class.getCanonicalName());

                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(candidateComponent.getBeanClassName());
                AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();

                String baseUrl = "http://%s.%s.svc.%s.local:%d";

                WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
                //根据web客户端去构建服http服务的代理工厂
                HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
                //根据代理工厂创建代理类
                Class<?> aClass = null;
                try {
                    aClass = Class.forName(beanDefinition.getBeanClassName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Object client = factory.createClient(aClass);

                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HttpServiceProxyFactory.class);
                builder.addConstructorArgValue(beanDefinition.getBeanClassName());
                AbstractBeanDefinition factoryBeanDefinition = builder.getBeanDefinition();

                registry.registerBeanDefinition(candidateComponent.getBeanClassName(),factoryBeanDefinition);
            }
        }
    }
}
