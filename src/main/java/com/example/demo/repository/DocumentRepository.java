package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Document;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>{
    List<Document> findByOrderByName();
    List<Document> findByOrderByUploadDate();
    List<Document> findByOrderBySize();
}
