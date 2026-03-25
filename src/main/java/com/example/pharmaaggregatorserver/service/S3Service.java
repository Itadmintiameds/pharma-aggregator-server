package com.example.pharmaaggregatorserver.service;

import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private final S3Client s3Client;

    /**
     * Uploads a file to AWS S3 and returns the public URL.
     *
     * @param key  the S3 object key (path in bucket)
     * @param file the file to upload
     * @return the public S3 URL of the uploaded file
     */
    public String uploadFile(String key, MultipartFile file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            log.error("Failed to upload file to S3. Key: {}", key, e);
            throw new IllegalArgumentException("Failed to upload file to S3", e);
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /**
     * Deletes a file from AWS S3.
     *
     * @param key the S3 object key (path in bucket)
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            log.warn("Failed to delete file from S3. Key: {}. Error: {}", key, e.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Retrieves a file from AWS S3 as a Spring Resource.
     *
     * @param key the S3 object key (path in bucket)
     * @return the file as a Resource
     */
    public Resource getFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        try {
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return new InputStreamResource(s3Object);
        } catch (S3Exception e) {
            log.error("Failed to retrieve file from S3. Key: {}. Error: {}", key, e.awsErrorDetails().errorMessage());
            throw new IllegalArgumentException("Failed to retrieve file from S3", e);
        }
    }

    /**
     * Extracts the S3 object key from a full S3 URL.
     *
     * @param s3Url the full S3 URL
     * @return the object key (path in bucket)
     */
    public String extractKeyFromUrl(String s3Url) {
        // Example: https://bucket.s3.region.amazonaws.com/bills/RECEIPT123.png
        int idx = s3Url.indexOf(".amazonaws.com/");
        if (idx == -1) throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
        return s3Url.substring(idx + ".amazonaws.com/".length());
    }

    /**
     * Uploads content from a Spring Resource (InputStream) to S3.
     * Used when migrating files within S3 (e.g. tempsellers → sellers on approval).
     *
     * @param key      the destination S3 object key
     * @param resource the source file as a Spring Resource
     * @return the public S3 URL of the uploaded file
     */
    public String uploadFileFromResource(String key, Resource resource, String contentType) {
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentDisposition("inline")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            log.info("Uploaded resource to S3. Key: {}", key);

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);

        } catch (IOException e) {
            log.error("Failed to upload resource to S3. Key: {}", key, e);
            throw new IllegalArgumentException("Failed to upload resource to S3", e);
        }
    }

    /**
     * Copies an S3 object from sourceKey to targetKey within the same bucket.
     * Returns the public URL of the new object.
     * Used when moving files from pendingsellers/ → sellers/ on approval.
     *
     * @param sourceKey the existing S3 object key
     * @param targetKey the destination S3 object key
     * @return the public S3 URL of the copied object
     */
    public String copyFile(String sourceKey, String targetKey) {
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(targetKey)
                    .build());

            log.info("Copied S3 object from {} → {}", sourceKey, targetKey);
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, targetKey);

        } catch (S3Exception e) {
            log.error("Failed to copy S3 object. Source: {}, Target: {}. Error: {}", sourceKey, targetKey, e.awsErrorDetails().errorMessage());
            throw new IllegalArgumentException("Failed to copy file in S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
}
