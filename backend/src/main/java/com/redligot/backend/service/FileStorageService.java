package com.redligot.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for file storage operations (currently unused since images are stored in DB).
 */
@Service
public class FileStorageService {

	private final Path rootLocation;

	public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
		this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(this.rootLocation);
	}

	public String store(MultipartFile file) throws IOException {
		String original = file.getOriginalFilename();
		String ext = "";
		if (original != null && original.contains(".")) {
			ext = original.substring(original.lastIndexOf('.'));
		}
		String filename = UUID.randomUUID().toString() + ext;
		Path target = this.rootLocation.resolve(filename);
		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		return filename;
	}

	public Resource loadAsResource(String filename) throws MalformedURLException {
		Path file = rootLocation.resolve(filename).normalize();
		Resource resource = new UrlResource(file.toUri());
		if (resource.exists() && resource.isReadable()) {
			return resource;
		}
		throw new MalformedURLException("File not found: " + filename);
	}

	public void deleteAll() throws IOException {
		FileSystemUtils.deleteRecursively(this.rootLocation);
		Files.createDirectories(this.rootLocation);
	}
}
