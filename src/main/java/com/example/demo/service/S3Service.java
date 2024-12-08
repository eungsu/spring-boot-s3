package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {
	
	private final S3Client s3Client;
	
	public void uploadFile(MultipartFile multipartFile, String bucketName, String folder, String filename) {
		
		try {
			String s3Filename = folder + "/" + filename;
			
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Filename)
				.contentType(multipartFile.getContentType())
				.contentLength(multipartFile.getSize())
				.build();
			
			s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}		
	}
	
	public ByteArrayResource downloadFile(String bucketName, String folder, String filename) {
		String s3Filename = folder + "/" + filename;
		
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucketName)
			.key(s3Filename)
			.build();
		
		try (InputStream inputStream = s3Client.getObject(getObjectRequest);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 스트림을 읽고 바이트 배열로 변환
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            return new ByteArrayResource(outputStream.toByteArray());

		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
}
