package com.hasnat.optimum.setup.entity;

import com.hasnat.optimum.common.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "stp_document_file")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false) private Long referenceId;
    @Column(length = 200)     private String documentCategory;
    @Column(length = 255)     private String fileName;
    @Column(length = 255)     private String originalFileName;
    @Column(length = 255)     private String filePath;
    @Column(length = 255)     private String fileType;
    private Long fileSize;
    @Column(length = 500)     private String remarks;
    @Column(length = 255)     private String uploadedBy;
    private LocalDateTime uploadedAt;
}
