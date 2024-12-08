package com.example.demo.dto;

import org.springframework.core.io.ByteArrayResource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DownloadFileData {

	private String filename;
	private ByteArrayResource resource;
}
