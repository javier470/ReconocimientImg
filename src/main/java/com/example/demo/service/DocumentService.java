package com.example.demo.service;

import com.example.demo.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.Document;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;
    
    public List<Document> getAllDocuments(){
        return documentRepository.findAll();
    }
    
    public List<Document> getAllDocumentsOrderedByName(){
        return documentRepository.findByOrderByName();
    }
    
    public List<Document> getAllDocumentsOrderedByDate(){
        return documentRepository.findByOrderByUploadDate();
    }
    
    public List<Document> getAllDocumentsOrderedBySize(){
        return documentRepository.findByOrderBySize();
    }
    
    public Optional<Document> getDocument(Long documentId){
        return documentRepository.findById(documentId);
    }
    
    public Document saveDocument(Document document){
        return documentRepository.save(document);
    }
}
