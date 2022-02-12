/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.miniofs;

import static io.jmix.core.FileStorageException.Type.IO_EXCEPTION;
import static io.jmix.core.UuidProvider.createUuid;
import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;
import static java.lang.String.format;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.slf4j.LoggerFactory.getLogger;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.TimeSource;
import io.jmix.core.annotation.Internal;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.credentials.MinioEnvironmentProvider;
import io.minio.credentials.Provider;
import io.minio.credentials.StaticProvider;
import io.minio.errors.ErrorResponseException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Internal
@Component("miniofs_FileStorage")
public class MinioFileStorage implements FileStorage {

  private static final Logger log = getLogger(MinioFileStorage.class);

  private static final String DEFAULT_STORAGE_NAME = "minio";

  protected String storageName;

  @Autowired
  protected MinioFileStorageProperties properties;

  boolean useConfigurationProperties = true;

  protected String accessKey;
  protected String secretKey;
  protected String region;
  protected String bucket;
  protected long partSize;
  protected String endpointUrl;

  @Autowired
  protected TimeSource timeSource;

  protected AtomicReference<MinioClient> minioClientReference = new AtomicReference<>();

  public MinioFileStorage() {
    this(DEFAULT_STORAGE_NAME);
  }

  public MinioFileStorage(String storageName) {
    this.storageName = storageName;
  }

  /**
   * Optional constructor that allows you to override {@link MinioFileStorageProperties}.
   */
  public MinioFileStorage(
      String storageName,
      @Nullable String accessKey,
      @Nullable String secretKey,
      @Nullable String region,
      String bucket,
      long partSize,
      String endpointUrl) {
    this.useConfigurationProperties = false;
    this.storageName = storageName;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.region = region;
    this.bucket = bucket;
    this.partSize = partSize;
    this.endpointUrl = endpointUrl;
  }

  @EventListener
  protected void initMinioClient(ApplicationStartedEvent event) {
    refreshMinioClient();
  }

  protected void refreshProperties() {
    if (useConfigurationProperties) {
      this.accessKey = properties.getAccessKey();
      this.secretKey = properties.getSecretKey();
      this.region = properties.getRegion();
      this.bucket = properties.getBucket();
      this.partSize = properties.getPartSize();
      this.endpointUrl = properties.getEndpointUrl();
    }
  }

  protected Provider getMinioCredentialsProvider() {
    if (accessKey != null && secretKey != null) {
      return new StaticProvider(accessKey, secretKey, null);
    } else {
      return new MinioEnvironmentProvider();
    }
  }

  public void refreshMinioClient() {
    refreshProperties();

    checkNotEmptyString(endpointUrl, "endpointUrl must not be empty");
    checkNotEmptyString(bucket, "bucket must not be empty");

    Provider minioCredentialsProvider = getMinioCredentialsProvider();

    minioClientReference.set(MinioClient.builder()
        .endpoint(endpointUrl)
        .credentialsProvider(minioCredentialsProvider)
        .region(region)
        .build());
  }

  @Override
  public String getStorageName() {
    return storageName;
  }

  protected String createFileKey(String fileName) {
    return createDateDir() + "/" + createUuidFilename(fileName);
  }

  protected String createDateDir() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(timeSource.currentTimestamp());

    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);

    return format("%d/%s/%s", year,
        leftPad(String.valueOf(month), 2, '0'),
        leftPad(String.valueOf(day), 2, '0'));
  }

  protected String createUuidFilename(String fileName) {
    String extension = getExtension(fileName);

    if (isNotEmpty(extension)) {
      return createUuid() + "." + extension;
    } else {
      return createUuid().toString();
    }
  }

  @Override
  public FileRef saveStream(String fileName, InputStream inputStream) {
    String fileKey = createFileKey(fileName);

    try {
      MinioClient client = minioClientReference.get();

      PutObjectArgs args = PutObjectArgs.builder()
          .bucket(bucket)
          .stream(inputStream, -1, partSize)
          .object(fileKey)
          .build();

      client.putObject(args);

      return new FileRef(getStorageName(), fileKey, fileName);
    } catch (Exception e) {
      log.error("Error saving file to MinIO storage", e);

      throw new FileStorageException(
          IO_EXCEPTION,
          format("Could not save file %s.", fileName)
      );
    }
  }

  @Override
  public InputStream openStream(FileRef reference) {
    InputStream is;

    try {
      MinioClient client = minioClientReference.get();

      GetObjectArgs args = GetObjectArgs.builder()
          .bucket(bucket)
          .object(reference.getPath())
          .build();

      is = client.getObject(args);
    } catch (Exception e) {
      log.error("Error loading file from MinIO storage", e);

      throw new FileStorageException(
          IO_EXCEPTION,
          format("Could not load file %s.", reference.getFileName())
      );
    }

    return is;
  }

  @Override
  public void removeFile(FileRef reference) {
    try {
      MinioClient client = minioClientReference.get();

      RemoveObjectArgs args = RemoveObjectArgs.builder()
          .bucket(bucket)
          .object(reference.getPath())
          .build();

      client.removeObject(args);
    } catch (Exception e) {
      log.error("Error removing file from MinIO storage", e);

      throw new FileStorageException(
          IO_EXCEPTION,
          format("Could not delete file %s.", reference.getFileName())
      );
    }
  }

  @Override
  public boolean fileExists(FileRef reference) {
    try {
      StatObjectArgs args = StatObjectArgs.builder()
          .bucket(bucket)
          .object(reference.getPath())
          .build();

      MinioClient minioClient = minioClientReference.get();

      minioClient.statObject(args);

      return true;
    } catch (Exception e) {
      if (e instanceof ErrorResponseException) {
        String code = ((ErrorResponseException) e).errorResponse().code();

        if (Arrays.asList("NoSuchBucket", "NoSuchKey").contains(code)) {
          return false;
        }
      }

      log.error("Error checking file in MinIO storage", e);

      throw new FileStorageException(
          IO_EXCEPTION,
          format("Could not check file %s", reference.getFileName())
      );
    }
  }

  public void setAccessKey(@Nullable String accessKey) {
    this.accessKey = accessKey;
  }

  public void setSecretKey(@Nullable String secretKey) {
    this.secretKey = secretKey;
  }

  public void setRegion(@Nullable String region) {
    this.region = region;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public void setPartSize(long partSize) {
    this.partSize = partSize;
  }

  public void setEndpointUrl(String endpointUrl) {
    this.endpointUrl = endpointUrl;
  }
}
