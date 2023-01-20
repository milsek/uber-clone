package com.example.springbackend.controller;

import com.example.springbackend.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/image")
public class PhotoController {
    @Autowired
    PhotoService photoService;

    @PostMapping(value="/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> storeImage(@RequestParam("file") MultipartFile multipartFile, Authentication auth) {
        String filename = photoService.storeImage(multipartFile, auth);
        return new ResponseEntity<>(filename, filename != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/load/{photoName}")
    public ResponseEntity<String> loadImage(@PathVariable String photoName) {
        return new ResponseEntity<>(photoService.loadImage(photoName), HttpStatus.OK);
    }
}
