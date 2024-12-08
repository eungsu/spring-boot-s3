package com.example.demo.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SaveFileForm {

	private String title;
	private String description;
	private MultipartFile upfile;
}
