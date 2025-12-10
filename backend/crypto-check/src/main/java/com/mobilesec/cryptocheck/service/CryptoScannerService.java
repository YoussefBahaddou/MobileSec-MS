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

    public List<CryptoFinding> scanCode(String sourceCode, String fileName) {
        List<CryptoFinding> findings = new ArrayList<>();
        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            cu.accept(new CryptoVisitor(findings, fileName), null);
        } catch (Exception e) {
            // If parsing fails (e.g. incomplete code), return error finding or log it
            System.err.println("Parse error for " + fileName + ": " + e.getMessage());
        }
        return findings;
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
