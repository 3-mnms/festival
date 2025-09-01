package com.teckit.festival.controller;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/festival/s3")
@Slf4j
public class S3AccessController {

    private final AWSCredentialsProvider credentialsProvider;

    @Value("${cloud.aws.sts.role-arn}")
    private String roleArn;

    @Value("${cloud.aws.region.static}")
    private String region;

    // S3Config에서 생성된 CredentialsProvider 빈을 주입받습니다.
    public S3AccessController(AWSCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    @GetMapping("/credentials")
    public Map<String, String> getTemporaryCredentials() {
        log.info("S3 접근을 위한 임시 자격 증명 요청");

        // STS 클라이언트에 CredentialsProvider를 명시적으로 전달
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(region)
                .build();

        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                .withRoleArn(roleArn)
                .withRoleSessionName("festival-app-session")
                .withDurationSeconds(3600); // 임시 자격 증명 유효 시간 (1시간)

        AssumeRoleResult assumeRoleResult = stsClient.assumeRole(assumeRoleRequest);
        Credentials credentials = assumeRoleResult.getCredentials();

        Map<String, String> response = new HashMap<>();
        response.put("AccessKeyId", credentials.getAccessKeyId());
        response.put("SecretAccessKey", credentials.getSecretAccessKey());
        response.put("SessionToken", credentials.getSessionToken());
        response.put("Expiration", credentials.getExpiration().toString());

        log.info("임시 자격 증명 발급 완료: 만료 시각 = {}", LocalDateTime.now().plusHours(1));

        return response;
    }
}