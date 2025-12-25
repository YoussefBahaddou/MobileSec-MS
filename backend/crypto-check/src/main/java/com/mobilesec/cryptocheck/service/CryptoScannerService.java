package com.mobilesec.cryptocheck.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.mobilesec.cryptocheck.model.CryptoFinding;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class CryptoScannerService {

    public List<CryptoFinding> scanFile(File file, String fileName) {
        List<CryptoFinding> findings = new ArrayList<>();
        long fileSize = file.length();

        // Strategy:
        // < 1MB: Load to memory, use full AST parsing (Most accurate)
        // > 1MB: Stream line-by-line, use Regex only (Prevents OOM)
        if (fileSize < 1024 * 1024) {
            try {
                String sourceCode = java.nio.file.Files.readString(file.toPath());
                return scanCode(sourceCode, fileName);
            } catch (Exception e) {
                // formatting/encoding error? fallthrough to regex
            }
        }

        // Large File or Read Error -> Streaming Regex
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            int lineNum = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNum++;
                scanLineWithRegex(line, fileName, lineNum, findings);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return findings;
    }

    public List<CryptoFinding> scanCode(String sourceCode, String fileName) {
        List<CryptoFinding> findings = new ArrayList<>();
        try {
            // Try explicit Java parsing first
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            cu.accept(new CryptoVisitor(findings, fileName), null);
        } catch (Exception e) {
            // Fallback to Regex Scan (e.g. for APK strings or incomplete code)
            scanWithRegex(sourceCode, fileName, findings);
        }
        return findings;
    }

    private void scanWithRegex(String source, String fileName, List<CryptoFinding> findings) {
        String[] lines = source.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            scanLineWithRegex(lines[i], fileName, i + 1, findings);
        }
    }

    private void scanLineWithRegex(String line, String fileName, int lineNum, List<CryptoFinding> findings) {
        // Simple heuristic patterns for common weak crypto
        if (line.contains("MD5")) {
            findings.add(createFinding("CWE-327-REGEX", "Weak Hashing (MD5)", "HIGH", fileName, lineNum, line.trim()));
        }
        if (line.contains("SHA-1") || line.contains("SHA1")) {
            findings.add(
                    createFinding("CWE-327-REGEX", "Weak Hashing (SHA-1)", "HIGH", fileName, lineNum, line.trim()));
        }
        if (line.contains("AES") && line.contains("ECB")) {
            findings.add(createFinding("CWE-327-REGEX", "Insecure Cipher Mode (ECB)", "HIGH", fileName, lineNum,
                    line.trim()));
        }
        if (line.contains("DES") || line.contains("Blowfish")) {
            findings.add(createFinding("CWE-327-REGEX", "Weak Encryption Algorithm", "HIGH", fileName, lineNum,
                    line.trim()));
        }
    }

    private CryptoFinding createFinding(String ruleId, String desc, String severity, String fileName, int lineNum,
            String snippet) {
        return CryptoFinding.builder()
                .ruleId(ruleId)
                .description(desc)
                .severity(severity)
                .fileName(fileName)
                .lineNumber(lineNum)
                .snippet(snippet)
                .build();
    }

    private static class CryptoVisitor extends VoidVisitorAdapter<Void> {
        private final List<CryptoFinding> findings;
        private final String fileName;

        public CryptoVisitor(List<CryptoFinding> findings, String fileName) {
            this.findings = findings;
            this.fileName = fileName;
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);

            // Rule 1: Weak Hashing (MD5, SHA-1)
            // MessageDigest.getInstance("MD5")
            if (n.getNameAsString().equals("getInstance")) {
                if (n.getArguments().size() > 0 && n.getArgument(0).isStringLiteralExpr()) {
                    String algo = n.getArgument(0).asStringLiteralExpr().asString().toUpperCase();
                    if (algo.equals("MD5") || algo.equals("SHA-1") || algo.equals("SHA1")) {
                        addFinding("CWE-327", "Weak Hashing Algorithm detected: " + algo, "HIGH", n);
                    }
                    // Rule 2: AES/ECB Mode
                    // Cipher.getInstance("AES/ECB/PKCS5Padding") (or just AES which defaults to ECB
                    // in some providers, but explicit is worse)
                    if (algo.contains("AES") && algo.contains("ECB")) {
                        addFinding("CWE-327", "Insecure Cipher Mode (ECB) detected: " + algo, "HIGH", n);
                    }
                    // Rule 3: DES/Blowfish
                    if (algo.startsWith("DES") || algo.startsWith("Blowfish")) {
                        addFinding("CWE-327", "Weak Encryption Algorithm detected: " + algo, "HIGH", n);
                    }
                }
            }
        }

        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            super.visit(n, arg);

            // Rule 4: Insecure Random
            // new Random() instead of SecureRandom
            String typeName = n.getType().asString();
            if (typeName.equals("Random") || typeName.endsWith(".Random")) {
                // Check if it's java.util.Random (simple heuristic)
                addFinding("CWE-330", "Insecure Random Number Generator (java.util.Random). Use SecureRandom.",
                        "MEDIUM", n);
            }
        }

        private void addFinding(String ruleId, String desc, String severity, com.github.javaparser.ast.Node node) {
            findings.add(CryptoFinding.builder()
                    .ruleId(ruleId)
                    .description(desc)
                    .severity(severity)
                    .fileName(fileName)
                    .lineNumber(node.getBegin().map(p -> p.line).orElse(-1))
                    .snippet(node.toString())
                    .build());
        }
    }
}
