package com.runners.app.community.upload.service;

import com.runners.app.community.upload.dto.request.PresignCommunityImageUploadFileRequest;
import com.runners.app.community.upload.dto.request.PresignCommunityImageUploadRequest;
import com.runners.app.community.upload.dto.response.PresignCommunityImageUploadResponse;
import com.runners.app.community.upload.dto.response.PresignedCommunityUploadItem;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class CommunityUploadService {

    @Value("${app.s3.region:}")
    private String region;

    @Value("${app.s3.bucket:}")
    private String bucket;

    @Value("${app.s3.public-base-url:}")
    private String publicBaseUrl;

    @Value("${app.s3.key-prefix:community/posts}")
    private String keyPrefix;

    @Value("${app.s3.presign-exp-minutes:10}")
    private long presignExpirationMinutes;

    @Value("${app.s3.max-upload-bytes:10485760}")
    private long maxUploadBytes;

    public PresignCommunityImageUploadResponse presignCommunityPostImageUploads(
            Long userId,
            PresignCommunityImageUploadRequest request
    ) {
        validateS3Config();

        if (request == null || request.files() == null || request.files().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files is required");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(presignExpirationMinutes);
        List<PresignedCommunityUploadItem> items = new ArrayList<>(request.files().size());

        try (S3Presigner presigner = newPresigner()) {
            for (PresignCommunityImageUploadFileRequest file : request.files()) {
                validateFile(file);

                String objectKey = buildObjectKey(userId, file.fileName(), file.contentType());
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(file.contentType())
                        .contentLength(file.contentLength())
                        .build();

                PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignExpirationMinutes))
                        .putObjectRequest(putObjectRequest)
                        .build();

                PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);

                items.add(new PresignedCommunityUploadItem(
                        objectKey,
                        presigned.url().toString(),
                        toPublicFileUrl(objectKey),
                        file.contentType()
                ));
            }
        }

        return new PresignCommunityImageUploadResponse(items, expiresAt);
    }

    public String toPublicFileUrl(String s3Key) {
        String safeKey = urlEncodePath(s3Key);

        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + safeKey;
        }

        if (region == null || region.isBlank() || bucket == null || bucket.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 public URL is not configured");
        }

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + safeKey;
    }

    private void validateS3Config() {
        if (region == null || region.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "app.s3.region is not configured");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "app.s3.bucket is not configured");
        }
        if (keyPrefix == null || keyPrefix.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "app.s3.key-prefix is not configured");
        }
        if (presignExpirationMinutes <= 0 || presignExpirationMinutes > 60) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "app.s3.presign-exp-minutes must be between 1 and 60");
        }
        if (maxUploadBytes <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "app.s3.max-upload-bytes must be positive");
        }
    }

    private void validateFile(PresignCommunityImageUploadFileRequest file) {
        if (file == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }
        String contentType = file.contentType();
        if (contentType == null || contentType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentType is required");
        }
        if (!contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image/* contentType is allowed");
        }
        if (file.contentLength() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentLength must be positive");
        }
        if (file.contentLength() > maxUploadBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is too large");
        }
    }

    private S3Presigner newPresigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private String buildObjectKey(Long userId, String fileName, String contentType) {
        String normalizedPrefix = keyPrefix.replaceAll("^/+", "").replaceAll("/+$", "");
        String ext = resolveExtension(fileName, contentType);
        String date = LocalDate.now().toString().replace("-", "");
        String random = UUID.randomUUID().toString().replace("-", "");
        return normalizedPrefix + "/" + userId + "/" + date + "/" + random + ext;
    }

    private String resolveExtension(String fileName, String contentType) {
        String fromContentType = Map.of(
                "image/jpeg", ".jpg",
                "image/jpg", ".jpg",
                "image/png", ".png",
                "image/webp", ".webp",
                "image/heic", ".heic",
                "image/heif", ".heif"
        ).getOrDefault(contentType.toLowerCase(Locale.ROOT), "");

        String fromFileName = "";
        if (fileName != null && !fileName.isBlank()) {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < fileName.length() - 1) {
                String candidate = fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
                if (candidate.matches("[a-z0-9]{1,8}")) {
                    fromFileName = "." + candidate;
                }
            }
        }

        return !fromFileName.isBlank() ? fromFileName : fromContentType;
    }

    private String urlEncodePath(String key) {
        if (key == null) return "";
        String[] parts = key.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}

