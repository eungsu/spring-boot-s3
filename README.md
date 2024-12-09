# spring boot와 AWS의 S3을 이용한 파입 업로드/다운로드

## 필요한 의존성

- 아래의 의존성은 **Amazon S3 (Simple Storage Service)**에 접근하고 작업을 수행할 수 있도록 돕는 AWS SDK for Java v2의 일부다.
- 아래의 의존성을 통해 Java 애플리케이션에서 AWS S3 버킷에 파일을 업로드, 다운로드, 삭제하거나 버킷의 목록을 가져오는 등의 다양한 작업을 수행할 수 있다.

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.29.29</version>
</dependency>
```

## application.properties 설정

- AWS의 자격 증명 및 버킷 정보를 설정한다

```properties
cloud.aws.credentials.access-key=AWS 엑세스 키
cloud.aws.credentials.secret-key=AWS 시크릿 키
cloud.aws.s3.bucket=S3 버킷 이름
```

## AWS S3 클라이언트 설정

- Spring Boot에서 S3 클라이언트를 설정한다.

```java
package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    /*
     * S3Client
     *  - S3Client 객체는 AWS SDK for Java v2에서 제공하는 Amazon Simple Storage Service (S3)와 상호작용하기 위한 클라이언트다.
     *  - S3Client 객체는 S3 버킷에 파일을 업로드, 다운로드, 삭제, 목록 조회 등의 작업을 수행할 수 있도록 지원한다.
     *  
     *  StaticCredentialsProvider
     *   - 정적 자격 증명(Access Key, Secret Key)을 제공하는 자격 증명 제공자다.
     *   - AWS 서비스 접근을 위해 필요한 자격 증명을 설정한다.
     *  
     *  AwsBasicCredentials
     *   - AWS의 액세스 키와 시크릿 키를 담고 있는 객체다.
     *   - AwsBasicCredentials.create(accessKey, secretKey)는 제공된 액세스 키와 시크릿 키를 기반으로 자격 증명 객체를 생성한다.
     */
    @Bean
    S3Client s3Client() {
        return S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    }
}
```

## S3에 파일업로드/다운로드 작업을 수행하는 서비스 클래스 작성

```java
package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    
    /**
     * S3 버킷에 파일을 업로드 한다.
     * @param multipartFile 업로드할 파일 데이터를 담고 있는 Spring의 MultipartFile 객체다.
     * @param bucketName 업로드할 S3 버킷의 이름이다.
     * @param folder 파일을 저장할 S3 버킷 내의 폴더 경로다.
     * @param filename S3에 저장될 파일 이름이다.
     */
    public void uploadFile(MultipartFile multipartFile, String bucketName, String folder, String filename) {
        
        try {
            // 파일이 S3에 저장될 경로와 파일 이름을 결합한 문자열입니다.
            String s3Filename = folder + "/" + filename;
            
            /*
             * PutObjectRequest
             *     - S3에 객체를 업로드할 때 필요한 요청 정보를 담는 객체다.
             *     - 주요 메소드
             *         - .bucket(String bucketName) : 업로드할 대상 버킷의 이름을 설정한다.
             *         - .key(String s3Filename) : S3 버킷 내에서 파일이 저장될 전체 경로다.
             *         - .contentType(String contentType) : 파일의 MIME 타입을 설정한다.
             *         - .contentLength(long size) : 파일의 크기를 설정한다.
             */
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Filename)
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();
            
            /*
             * .putObject(PutObjectRequest putObjectRequest, RequestBody requestBody)
             *     - S3에 객체를 업로드하는 메서드다.
             *     - 매개변수
             *         - PutObjectRequest : 업로드 요청 정보를 담은 객체다.
             *         - RequestBody : 파일의 데이터를 바이트 배열로 변환하여 요청 본문에 담는다.
             */
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }        
    }
    
    /**
     * AWS S3에서 파일을 다운로드하는 메서드다.
     * @param bucketName 다운로드할 S3 버킷의 이름이다.
     * @param folder S3 버킷 내에서 파일이 위치한 폴더 경로다.
     * @param filename 다운로드할 파일의 이름이다.
     * @return 파일 데이터를 바이트 배열 형태로 감싼 ByteArrayResource 객체
     */
    public ByteArrayResource downloadFile(String bucketName, String folder, String filename) {
        // 다운로드할 S3 객체의 전체 경로다.
        String s3Filename = folder + "/" + filename;
        
        /*
         * GetObjectRequest
         *     - S3에서 객체를 가져올 때 필요한 요청 정보를 담는 객체다.
         *     - 주요 메소드
         *         - .bucket(String bucketName): S3 버킷 이름을 설정한다.
         *         - .key(String s3Filename): 다운로드할 파일의 S3 경로를 설정한다.
         */
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Filename)
            .build();
        
        /*
         * ResponseInputStream<GetObjectResponse> getObject(GetObjectRequest getObjectRequest)
         *     - S3에서 파일을 가져와 입력 스트림(InputStream)으로 반환한다.
         */
        try (InputStream inputStream = s3Client.getObject(getObjectRequest);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 스트림을 읽고 바이트 배열로 변환
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // 바이트 배열을 감싸는 Spring의 리소스 객체로, HTTP 응답 등에서 파일을 반환할 때 사용한다.
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     *  AWS S3 버킷에서 파일을 삭제하는 메서드다.
     * @param bucketName 파일이 저장된 S3 버킷의 이름이다.
     * @param folder S3 버킷 내에서 파일이 위치한 폴더다.
     * @param filename 삭제할 파일의 이름이다.
     */
    public void deleteFile(String bucketName, String folder, String filename) {
        // S3에서 파일을 찾기 위한 전체 경로다.
        String s3Filename = folder + "/" + filename;
        
        /*
         * DeleteObjectRequest
         *     - S3에 저장된 객체를 삭제할 때 필요한 요청정보를 담는 객체다.
         *     - 주요 메소드
         *         - .bucket(String bucketName): S3 버킷 이름을 설정한다.
         *         - .key(String s3Filename): 삭제할 파일의 S3 경로를 설정한다.
         */
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Filename)
            .build();
        
        try {
            /*
             * DeleteObjectResponse deleteObject(DeleteObjectRequest deleteObjectRequest)
             *     - AWS S3에서 지정된 파일을 삭제한다.
             *     - 매개변수
             *         - DeleteObjectRequest: 삭제에 필요한 정보가 포함된 객체다.
             */
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }        
        
    }
}
```

## 컨트롤러와 서비스 클래스 작성하기

### 컨트롤러 클래스

```java
package com.example.demo.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.DownloadFileData;
import com.example.demo.dto.SaveFileForm;
import com.example.demo.service.FileService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("files", fileService.getAllFiles());
        
        return "file/list";
    }

    @GetMapping("/form")
    public String form() {
        
        return "file/form";
    }
    
    @PostMapping("/save")
    public String save(SaveFileForm form) {
        fileService.saveFile(form);
        return "file/form";
    }
    
     @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam("id") Long id) {
         DownloadFileData fileData = fileService.downloadFile(id);

        try {
            String filename = fileData.getFilename();
            String encodedFileName = URLEncoder.encode(filename.substring(filename.lastIndexOf("-") + 1), "UTF-8");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileData.getResource().contentLength())
                    .body(fileData.getResource());

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("파일 이름 인코딩 실패", e);
        }
    }
}
```

### 서비스 클래스

```java
package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.DownloadFileData;
import com.example.demo.dto.SaveFileForm;
import com.example.demo.entity.FileEntity;
import com.example.demo.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    private String folder = "폴더명";
    
    private final S3Service s3Service;
    private final FileRepository fileRepository;
    
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }
    
    public FileEntity getFile(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
    }
    
    public void saveFile(SaveFileForm form) {
        MultipartFile upfile = form.getUpfile();
        String filename = System.currentTimeMillis() + "-" + upfile.getOriginalFilename();
        
        s3Service.uploadFile(form.getUpfile(), bucketName, folder, filename);
        
        FileEntity entity = FileEntity.builder()
            .title(form.getTitle())
            .description(form.getDescription())
            .folder(folder)
            .filename(filename)
            .build();
        
        fileRepository.save(entity);
    }

    public DownloadFileData downloadFile(Long id) {
        FileEntity entity = getFile(id);
        ByteArrayResource resource = s3Service.downloadFile(bucketName, folder, entity.getFilename());
        
        return new DownloadFileData(entity.getFilename(), resource);
    }
}
```
