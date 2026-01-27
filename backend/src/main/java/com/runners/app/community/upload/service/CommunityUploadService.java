package com.runners.app.community.upload.service;

import com.runners.app.community.upload.dto.request.PresignCommunityImageUploadFileRequest;
import com.runners.app.community.upload.dto.request.PresignCommunityImageUploadRequest;
import com.runners.app.community.upload.dto.response.PresignCommunityImageUploadResponse;
import com.runners.app.community.upload.dto.response.PresignedCommunityUploadItem;
import com.runners.app.community.upload.exception.UploadDomainException;
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
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

    @Value("${app.s3.post-key-prefix:community/posts}")
    private String communityPostKeyPrefix;

    @Value("${app.s3.profile-key-prefix:users/profile}")
    private String userProfileKeyPrefix;

    @Value("${app.s3.presign-exp-minutes:10}")
    private long presignExpirationMinutes;

    @Value("${app.s3.max-upload-bytes:10485760}")
    private long maxUploadBytes;

    public PresignCommunityImageUploadResponse presignCommunityPostImageUploads(
            Long userId,
            PresignCommunityImageUploadRequest request
    ) {
        return presignUploads(userId, request, communityPostKeyPrefix, 10);
    }

    public PresignCommunityImageUploadResponse presignUserProfileImageUpload(
            Long userId,
            PresignCommunityImageUploadRequest request
    ) {
        return presignUploads(userId, request, userProfileKeyPrefix, 1);
    }

    public boolean isUserProfileImageKey(Long userId, String key) {
        String normalizedKey = normalizeKey(key);
        String normalizedPrefix = normalizePrefix(userProfileKeyPrefix);
        String userPrefix = normalizedPrefix + "/" + userId + "/";
        return !normalizedKey.isBlank() && normalizedKey.startsWith(userPrefix);
    }

    public void deleteUserProfileImageObject(Long userId, String key) {
        validateS3Config(userProfileKeyPrefix);

        String trimmedKey = key == null ? "" : key.trim();
        if (trimmedKey.isBlank()) return;
        if (!isUserProfileImageKey(userId, trimmedKey)) {
            throw UploadDomainException.invalidKey();
        }

        String normalizedKey = normalizeKey(trimmedKey);
        try (S3Client client = newS3Client()) {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(normalizedKey)
                    .build());
        } catch (Exception e) {
            throw UploadDomainException.deleteProfileImageFailed();
        }
    }

    private PresignCommunityImageUploadResponse presignUploads(
            Long userId,
            PresignCommunityImageUploadRequest request,
            String keyPrefix,
            int maxFiles
    ) {
        validateS3Config(keyPrefix);

        if (request == null || request.files() == null || request.files().isEmpty()) {
            throw UploadDomainException.filesRequired();
        }
        if (request.files().size() > maxFiles) {
            throw UploadDomainException.tooManyFiles();
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(presignExpirationMinutes);
        List<PresignedCommunityUploadItem> items = new ArrayList<>(request.files().size());

        try (S3Presigner presigner = newPresigner()) {
            for (PresignCommunityImageUploadFileRequest file : request.files()) {
                validateFile(file);

                String objectKey = buildObjectKey(keyPrefix, userId, file.fileName(), file.contentType());
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

    public String toPublicFileUrl(String key) {
        String normalizedKey = normalizeKey(key);
        String safeKey = urlEncodePath(normalizedKey);
        return requiredPublicBaseUrl() + "/" + safeKey;
    }

    private void validateS3Config(String keyPrefix) {
        if (region == null || region.isBlank()) {
            throw UploadDomainException.s3ConfigInvalid("app.s3.region is not configured");
        }
        if (bucket == null || bucket.isBlank()) {
            throw UploadDomainException.s3ConfigInvalid("app.s3.bucket is not configured");
        }
        if (keyPrefix == null || keyPrefix.isBlank()) {
            throw UploadDomainException.s3ConfigInvalid("S3 key prefix is not configured");
        }
        if (presignExpirationMinutes <= 0 || presignExpirationMinutes > 60) {
            throw UploadDomainException.s3ConfigInvalid("app.s3.presign-exp-minutes must be between 1 and 60");
        }
        if (maxUploadBytes <= 0) {
            throw UploadDomainException.s3ConfigInvalid("app.s3.max-upload-bytes must be positive");
        }

        requiredPublicBaseUrl();
    }

    private String requiredPublicBaseUrl() {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw UploadDomainException.s3ConfigInvalid("app.s3.public-base-url is not configured");
        }

        String trimmed = publicBaseUrl.trim().replaceAll("/+$", "");
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            throw UploadDomainException.s3ConfigInvalid("app.s3.public-base-url must start with http:// or https://");
        }
        return trimmed;
    }

    private void validateFile(PresignCommunityImageUploadFileRequest file) {
        if (file == null) {
            throw UploadDomainException.fileRequired();
        }
        String contentType = file.contentType();
        if (contentType == null || contentType.isBlank()) {
            throw UploadDomainException.contentTypeRequired();
        }
        if (!contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw UploadDomainException.contentTypeNotAllowed();
        }
        if (file.contentLength() <= 0) {
            throw UploadDomainException.contentLengthInvalid();
        }
        if (file.contentLength() > maxUploadBytes) {
            throw UploadDomainException.fileTooLarge();
        }
    }

    private S3Presigner newPresigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private S3Client newS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private String buildObjectKey(String keyPrefix, Long userId, String fileName, String contentType) {
        String normalizedPrefix = normalizePrefix(keyPrefix);
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

    private String normalizePrefix(String prefix) {
        if (prefix == null) return "";
        return prefix.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        String trimmed = key.trim();
        if (trimmed.isBlank()) return "";

        if (trimmed.startsWith("arn:aws:s3:::")) {
            String withoutArn = trimmed.substring("arn:aws:s3:::".length());
            String prefix = bucket + "/";
            if (withoutArn.startsWith(prefix)) {
                return withoutArn.substring(prefix.length());
            }
            int firstSlash = withoutArn.indexOf('/');
            return firstSlash >= 0 ? withoutArn.substring(firstSlash + 1) : withoutArn;
        }

        String bucketPrefix = bucket + "/";
        if (trimmed.startsWith(bucketPrefix)) {
            return trimmed.substring(bucketPrefix.length());
        }

        return trimmed.replaceAll("^/+", "");
    }
}
