package istio.example.master.client;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

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

                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CloudNativeClientFactoryBean.class);

                builder.addConstructorArgValue(beanDefinition.getBeanClassName());
                builder.addConstructorArgValue(attributes.get("service"));
                builder.addConstructorArgValue(attributes.get("namespace"));
                builder.addConstructorArgValue(attributes.get("cluster"));
                AbstractBeanDefinition factoryBeanDefinition = builder.getBeanDefinition();

                registry.registerBeanDefinition(candidateComponent.getBeanClassName(), factoryBeanDefinition);
            }
        }
    }
}
