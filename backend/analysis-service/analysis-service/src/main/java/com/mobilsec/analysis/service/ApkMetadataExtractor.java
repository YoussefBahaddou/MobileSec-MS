package com.mobilsec.analysis.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.Permission;

@Component
public class ApkMetadataExtractor {

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    private final SecretScannerService secretScanner;
    private final CryptoScannerService cryptoScanner;

    public ApkMetadataExtractor(SecretScannerService secretScanner, CryptoScannerService cryptoScanner) {
        this.secretScanner = secretScanner;
        this.cryptoScanner = cryptoScanner;
    }

    public ApkMetadata extract(Path apkPath) throws IOException {
        try (ApkFile apkFile = new ApkFile(apkPath.toFile())) {
            ApkMeta apkMeta = apkFile.getApkMeta();

            List<String> permissions = apkMeta.getUsesPermissions() == null
                    ? List.of()
                    : List.copyOf(apkMeta.getUsesPermissions());

            String manifestXml = apkFile.getManifestXml();
            Document manifestDoc = parseManifest(manifestXml);

            ManifestFlags manifestFlags = extractManifestFlags(manifestDoc);
            List<ExportedComponent> components = extractExportedComponents(manifestDoc);

            // Scan Manifest for secrets and crypto issues
            List<String> secrets = secretScanner.scan(manifestXml);
            List<String> cryptoIssues = cryptoScanner.scan(manifestXml);

            return new ApkMetadata(
                    apkMeta.getPackageName(),
                    apkMeta.getVersionName(),
                    permissions,
                    manifestFlags,
                    components,
                    secrets,
                    cryptoIssues);
        } catch (ParserConfigurationException | SAXException ex) {
            throw new IOException("Failed to parse AndroidManifest.xml", ex);
        }
    }

    private Document parseManifest(String manifestXml)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (StringReader reader = new StringReader(manifestXml)) {
            return builder.parse(new InputSource(reader));
        }
    }

    private ManifestFlags extractManifestFlags(Document manifestDoc) {
        Element application = (Element) manifestDoc.getElementsByTagName("application").item(0);
        if (application == null) {
            return new ManifestFlags(false, true, false);
        }

        boolean debuggable = getBooleanAttribute(application, "debuggable", false);
        boolean allowBackup = getBooleanAttribute(application, "allowBackup", true);
        boolean cleartext = getBooleanAttribute(application, "usesCleartextTraffic", false);

        return new ManifestFlags(debuggable, allowBackup, cleartext);
    }

    private List<ExportedComponent> extractExportedComponents(Document manifestDoc) {
        List<ExportedComponent> components = new ArrayList<>();
        for (String tagName : List.of("activity", "activity-alias", "service", "receiver", "provider")) {
            NodeList nodes = manifestDoc.getElementsByTagName(tagName);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element element = (Element) node;
                String exportedAttr = element.getAttributeNS(ANDROID_NS, "exported");
                if (exportedAttr == null || exportedAttr.isBlank()) {
                    continue;
                }

                if (Boolean.parseBoolean(exportedAttr)) {
                    String name = element.getAttributeNS(ANDROID_NS, "name");
                    components.add(new ExportedComponent(name, tagName));
                }
            }
        }

        return components;
    }

    private boolean getBooleanAttribute(Element element, String attribute, boolean defaultValue) {
        String value = element.getAttributeNS(ANDROID_NS, attribute);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
