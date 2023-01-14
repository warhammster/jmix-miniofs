# Jmix MinIO File Storage

This repository contains [MinIO](https://min.io/) File Storage addon of the [Jmix](https://jmix.io) framework.


## Installation

Add to your project's `build.gradle` dependencies:

```groovy
implementation 'dev.warhammster.jmix:jmix-miniofs-starter:1.4.0'
```

## Quick start

After installation, you need to configure file storage properties in `application.properties` file. 
Here's an example for a local MinIO storage:

```text
jmix.miniofs.endpointUrl = http://localhost:9000
jmix.miniofs.accessKey = minioadmin
jmix.miniofs.secretKey = minioadmin
jmix.miniofs.bucket = my-bucket
```

## Configuration

#### jmix.miniofs.endpointUrl

* **Description:** Endpoint URL of MinIO or Amazon S3 storage.
* **Examples:** http://localhost:9000, https://s3.amazonaws.com

#### jmix.miniofs.accessKey

* **Description:** MinIO or Amazon S3 access key. If not set `MINIO_ACCESS_KEY` environment variable will be used.

#### jmix.miniofs.secretKey

* **Description:** MinIO or Amazon S3 secret key. If not set `MINIO_SECRET_KEY` environment variable will be used.

#### jmix.miniofs.bucket

* **Description:** MinIO or Amazon S3 bucket name.

#### jmix.miniofs.region

* **Description:** Amazon S3 region. Must be specified if Amazon S3 storage is used.

#### jmix.miniofs.partSize

* **Description:** Upload part size in bytes. A valid part size is between 5MiB to 5GiB (both limits inclusive).
* **Default value:** 5242880
