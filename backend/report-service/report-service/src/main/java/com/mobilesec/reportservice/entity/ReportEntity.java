package com.mobilesec.reportservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String scanId;

    private String packageName;

    // Store JSON blobs for flexible schema of microservice results
    @Lob
    @Column(columnDefinition = "CLOB")
    private String manifestResultJson;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String secretResultJson;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String cryptoResultJson;

    private int riskScore;

    @CreationTimestamp
    private Instant createdAt;
}
