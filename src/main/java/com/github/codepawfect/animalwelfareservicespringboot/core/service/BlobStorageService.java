package com.github.codepawfect.animalwelfareservicespringboot.core.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.azure.storage.blob.BlobServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class BlobStorageService {

  private final BlobServiceClient blobServiceClient;

  /**
   * Upload files to blob storage
   *
   * @param containerName container name
   * @param blobName blob name
   * @param data data
   */
  public Mono<String> uploadToBlob(String containerName, String blobName, InputStream data) {
    return Mono.fromCallable(
            () -> {
              var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
              var blobClient = blobContainerClient.getBlobClient(blobName);
              blobClient.upload(data, data.available(), true);
              return blobClient.getBlobUrl();
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * Read from blob storage
   *
   * @param containerName container name
   * @param blobName blob name
   * @return data
   */
  public Mono<byte[]> readFromBlob(String containerName, String blobName) {
    return Mono.fromCallable(
            () -> {
              var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
              var blobClient = blobContainerClient.getBlobClient(blobName);

              try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                blobClient.downloadStream(outputStream);
                return outputStream.toByteArray();
              }
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  /**
   * Delete a blob from blob storage
   *
   * @param containerName container name
   * @param blobName blob name
   */
  public Mono<Void> deleteBlob(String containerName, String blobName) {
    return Mono.fromRunnable(
            () -> {
              var blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
              var blobClient = blobContainerClient.getBlobClient(blobName);
              blobClient.delete();
            })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  /**
   * Delete blobs from blob storage
   *
   * @param containerName container name
   * @param blobNames blob name
   */
  public Mono<Void> deleteBlobs(String containerName, Flux<String> blobNames) {
    return blobNames.flatMap(blobName -> deleteBlob(containerName, blobName)).then();
  }
}
