package dev.warhammster.jmix.miniofs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.FileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {
    CoreConfiguration.class,
    MinioFileStorageConfiguration.class,
    MinioFileStorageTestConfiguration.class
})
class MinioFileStorageTest {

  @Autowired
  private FileStorage fileStorage;

  @Test
  public void miniofs_isInitialized_afterStartup() {
    assertTrue(fileStorage instanceof MinioFileStorage);
  }

}
