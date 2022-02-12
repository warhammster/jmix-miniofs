package io.jmix.miniofs;

import io.jmix.core.annotation.JmixModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(MinioFileStorageConfiguration.class)
@JmixModule(id = "io.jmix.miniofs.test", dependsOn = MinioFileStorageConfiguration.class)
public class MiniofsTestConfiguration {

}
