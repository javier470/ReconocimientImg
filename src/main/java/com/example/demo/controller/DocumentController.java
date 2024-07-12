package com.example.demo.controller;

import com.example.demo.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.model.Document;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    
    @GetMapping
    public List<Document> getDocuments(){
        return documentService.getAllDocuments();
    }
    
    @GetMapping("/{documentId}")
    public Optional<Document> getDocument(@PathVariable Long documentId){
        return documentService.getDocument(documentId);
    }
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes){
        if (file.isEmpty()) {
            return ResponseEntity.ok("Se necesita una imagen para guardar");
        }
        
        try{
            Path path = Paths.get("src/main/resources/static/" + file.getOriginalFilename());
            Files.write(path, file.getBytes());
            String ocrText = extractText(path);
   
            Document document = new Document();
            document.setName(file.getOriginalFilename());
            document.setOcrText(ocrText);
            document.setUploadDate(LocalDateTime.now());
            document.setSize(file.getSize());
            documentService.saveDocument(document);
            return ResponseEntity.ok("Imagen guardada exitosamente");
        }catch(IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    } 
    
    
    private String extractText(Path path){
        RestTemplate restTemplate = new RestTemplate();
        
        String url = "http://localhost:5000/getOCR";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(path.toFile()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
        return response.getBody().get("Response").toString();
        
    }
}
