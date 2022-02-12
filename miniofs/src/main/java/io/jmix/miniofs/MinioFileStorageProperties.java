package io.jmix.miniofs;

import javax.annotation.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.miniofs")
@ConstructorBinding
public class MinioFileStorageProperties {

  /**
   * MinIO or Amazon S3 access key.
   */
  private String accessKey;

  /**
   * MinIO or Amazon S3 secret key.
   */
  private String secretKey;

  /**
   * Amazon S3 region.
   */
  private String region;

  /**
   * MinIO or Amazon S3 bucket name.
   */
  private String bucket;

  /**
   * Upload part size (bytes).
   * A valid part size is between 5MiB to 5GiB (both limits inclusive).
   */
  private long partSize;

  /**
   * MinIO or Amazon S3 endpoint URL.
   */
  private String endpointUrl;

  public MinioFileStorageProperties(
      @Nullable String accessKey,
      @Nullable String secretKey,
      @Nullable String region,
      String bucket,
      @DefaultValue("5242880") long partSize,
      String endpointUrl) {

    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
    this.partSize = partSize;
    this.endpointUrl = endpointUrl;
  }

  /**
   * @see #accessKey
   */
  public String getAccessKey() {
    return accessKey;
  }

  /**
   * @see #secretKey
   */
  public String getSecretKey() {
    return secretKey;
  }

  /**
   * @see #region
   */
  public String getRegion() {
    return region;
  }

  /**
   * @see #bucket
   */
  public String getBucket() {
    return bucket;
  }

  /**
   * @see #partSize
   */
  public long getPartSize() {
    return partSize;
  }

  /**
   * @see #endpointUrl
   */
  public String getEndpointUrl() {
    return endpointUrl;
  }
}
