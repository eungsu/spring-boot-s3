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
	
	@Value("${cloud.aws.s3.bucket.path.images}")
	private String folder;
	
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
