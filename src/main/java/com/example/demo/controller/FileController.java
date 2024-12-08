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
