package com.example.demo.controller;

import com.example.demo.service.DocumentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.model.Document;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ViewController {
    
    @Autowired
    private DocumentService documentService;
    
    private final Path rootLocation = Paths.get("uploads");
    
    @GetMapping("/")
    public String index(Model model, @RequestParam(name = "sort", required = false) String sort) {
        List<Document> documents;
        if(sort != null && !sort.isEmpty()){
            switch(sort){
                case "name":
                    documents = documentService.getAllDocumentsOrderedByName();
                    break;
                case "uploadDate":
                    documents = documentService.getAllDocumentsOrderedByDate();
                    break;
                case "size":
                    documents = documentService.getAllDocumentsOrderedBySize();
                    break;
                default:
                    documents = documentService.getAllDocuments();
                    break;
            }
        }else{
            documents = documentService.getAllDocuments();
        }
        

        model.addAttribute("documents", documents);
        return "index";
    }
    
    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes){
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message_error", "Porfavor selecciona un archivo para subir");
            return "redirect:/";
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
            redirectAttributes.addFlashAttribute("message", "Se ha subido correctamente el archivo: '" + file.getOriginalFilename() + "'");
        }catch(IOException e){
            redirectAttributes.addFlashAttribute("message_error", "Ha ocurrido un error al intentar carga la imagen");
        }
        return "redirect:/";
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
