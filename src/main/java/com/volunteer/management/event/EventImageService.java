package com.volunteer.management.event;

import com.volunteer.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class EventImageService {

    private final EventRepository eventRepository;
    private final Path uploadDir;

    public EventImageService(
            EventRepository eventRepository,
            @Value("${app.uploads.events-dir}") String uploadDirPath) {
        this.eventRepository = eventRepository;
        this.uploadDir = Path.of(uploadDirPath);
    }

    public String uploadImage(UUID eventId, MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        try {
            Files.createDirectories(uploadDir);

            String extension = getExtension(file.getOriginalFilename());
            // Never trust the client's filename directly — generate our own
            String filename = UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(filename);

            file.transferTo(target);

            String imageUrl = "/api/v1/events/" + eventId + "/image/" + filename;
            event.setImageUrl(imageUrl);
            eventRepository.save(event);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store event image", e);
        }
    }

    public Path resolveImagePath(String filename) {
        return uploadDir.resolve(filename).normalize();
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
