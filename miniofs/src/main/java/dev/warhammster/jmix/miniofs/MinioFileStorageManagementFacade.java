package dev.warhammster.jmix.miniofs;

import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Manages MinIO file storage client", objectName = "jmix.miniofs:type=MinioFileStorage")
@Component("miniofs_MinioFileStorageManagementFacade")
public class MinioFileStorageManagementFacade {

  @Autowired
  protected FileStorageLocator fileStorageLocator;

  @ManagedOperation(description = "Refresh MinIO file storage client")
  public String refreshMinioClient() {
    FileStorage fileStorage = fileStorageLocator.getDefault();
    if (fileStorage instanceof MinioFileStorage) {
      MinioFileStorage minioFileStorage = (MinioFileStorage) fileStorage;

      minioFileStorage.refreshMinioClient();

      return "Refreshed successfully";
    }

    return "Not a MinIO file storage - refresh attempt ignored";
  }

  @ManagedOperation(description = "Refresh MinIO file storage client by storage name")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "storageName", description = "Storage name"),
      @ManagedOperationParameter(name = "accessKey", description = "MinIO access key"),
      @ManagedOperationParameter(name = "secretKey", description = "MinIO secret key")})
  public String refreshMinioClient(String storageName, String accessKey, String secretKey) {
    FileStorage fileStorage = fileStorageLocator.getByName(storageName);

    if (fileStorage instanceof MinioFileStorage) {
      MinioFileStorage minioFileStorage = (MinioFileStorage) fileStorage;

      minioFileStorage.setAccessKey(accessKey);
      minioFileStorage.setSecretKey(secretKey);
      minioFileStorage.refreshMinioClient();

      return "Refreshed successfully";
    }

    return "Not a MinIO file storage - refresh attempt ignored";
  }

  @ManagedOperation(description = "Refresh MinIO file storage client by storage name")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "storageName", description = "Storage name"),
      @ManagedOperationParameter(name = "accessKey", description = "MinIO or Amazon S3 access key"),
      @ManagedOperationParameter(name = "secretAccessKey", description = "MinIO or Amazon S3 secret key"),
      @ManagedOperationParameter(name = "region", description = "Amazon S3 region"),
      @ManagedOperationParameter(name = "bucket", description = "MinIO or Amazon S3 bucket name"),
      @ManagedOperationParameter(name = "partSize", description = "Upload part size (bytes). Between 5MiB to 5GiB (inclusive)"),
      @ManagedOperationParameter(name = "endpointUrl", description = "MinIO or Amazon S3 storage endpoint URL")
  })

  public String refreshMinioClient(
      String storageName,
      @Nullable String accessKey,
      @Nullable String secretAccessKey,
      @Nullable String region,
      String bucket,
      int chunkSize,
      String endpointUrl) {

    FileStorage fileStorage = fileStorageLocator.getByName(storageName);

    if (fileStorage instanceof MinioFileStorage) {
      MinioFileStorage minioFileStorage = (MinioFileStorage) fileStorage;

      minioFileStorage.setAccessKey(accessKey);
      minioFileStorage.setSecretKey(secretAccessKey);
      minioFileStorage.setRegion(region);
      minioFileStorage.setBucket(bucket);
      minioFileStorage.setPartSize(chunkSize);
      minioFileStorage.setEndpointUrl(endpointUrl);
      minioFileStorage.refreshMinioClient();

      return "Refreshed successfully";
    }

    return "Not a Minio file storage - refresh attempt ignored";
  }
}
