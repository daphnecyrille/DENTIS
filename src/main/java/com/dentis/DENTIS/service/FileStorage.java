package com.dentis.DENTIS.service;

import com.dentis.DENTIS.config.WebMvcConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;

/**
 * Where uploaded files live.
 * - Hosted (Vercel): Supabase Storage, in a private bucket. Set SUPABASE_URL and SUPABASE_SERVICE_KEY.
 *   The browser uploads straight to Supabase with a one-time link, and viewing a file
 *   goes through the app, which checks the login and hands out a 5-minute link.
 * - Local (no Supabase settings): files are saved under the uploads folder as before.
 * A file's key looks like "prosthodontics/12/ocr_<uuid>_photo.jpg"; the app shows it at "/attachments/<key>".
 */
@Service
public class FileStorage {

    private static final Logger log = LoggerFactory.getLogger(FileStorage.class);

    private final String url = trim(System.getenv("SUPABASE_URL"));
    private final String key = System.getenv("SUPABASE_SERVICE_KEY");
    private final String bucket = System.getenv().getOrDefault("SUPABASE_BUCKET", "dentis-files");
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json = new ObjectMapper();

    private static String trim(String s) {
        if (s == null || s.isBlank()) return null;
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    public boolean isRemote() { return url != null && key != null && !key.isBlank(); }

    /** Creates the private bucket the first time (does nothing if it already exists). */
    @PostConstruct
    void ensureBucket() {
        if (!isRemote()) return;
        try {
            String body = json.writeValueAsString(Map.of(
                    "id", bucket, "name", bucket, "public", false, "file_size_limit", 26214400));
            HttpResponse<String> r = send(HttpRequest.newBuilder(URI.create(url + "/storage/v1/bucket"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)));
            if (r.statusCode() >= 300 && !r.body().contains("already exists"))
                log.warn("Supabase bucket check: {} {}", r.statusCode(), r.body());
        } catch (Exception e) {
            log.warn("Supabase bucket check failed: {}", e.getMessage());
        }
    }

    /** A link the browser can PUT the file to. */
    public String uploadUrl(String fileKey) throws IOException {
        if (!isRemote()) return "/attachments/upload?key=" + fileKey;
        HttpResponse<String> r = send(HttpRequest.newBuilder(URI.create(url + "/storage/v1/object/upload/sign/" + bucket + "/" + fileKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}")));
        if (r.statusCode() >= 300) throw new IOException("Supabase upload link failed: " + r.statusCode() + " " + r.body());
        return url + "/storage/v1" + json.readTree(r.body()).path("url").asText();
    }

    /** A 5-minute link for viewing the file (remote only). */
    public String readUrl(String fileKey) throws IOException {
        HttpResponse<String> r = send(HttpRequest.newBuilder(URI.create(url + "/storage/v1/object/sign/" + bucket + "/" + fileKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"expiresIn\":300}")));
        if (r.statusCode() >= 300) throw new IOException("Supabase view link failed: " + r.statusCode() + " " + r.body());
        JsonNode n = json.readTree(r.body());
        String signed = n.hasNonNull("signedURL") ? n.get("signedURL").asText() : n.path("signedUrl").asText();
        return url + "/storage/v1" + signed;
    }

    /** Local mode: save an uploaded body. */
    public void saveLocal(String fileKey, InputStream in) throws IOException {
        Path p = localPath(fileKey);
        Files.createDirectories(p.getParent());
        Files.copy(in, p, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Local mode: the file on disk, kept inside the uploads folder. */
    public Path localPath(String fileKey) {
        Path root = Paths.get(WebMvcConfig.UPLOAD_FS_PATH).toAbsolutePath().normalize();
        Path p = root.resolve(fileKey).normalize();
        if (!p.startsWith(root)) throw new IllegalArgumentException("Bad file key");
        return p;
    }

    /** Only letters, digits, dot, dash, underscore and slash; no "..". */
    public static boolean isSafeKey(String fileKey) {
        return fileKey != null && fileKey.matches("[A-Za-z0-9._/-]+") && !fileKey.contains("..") && !fileKey.startsWith("/");
    }

    private HttpResponse<String> send(HttpRequest.Builder b) throws IOException {
        try {
            return http.send(b.header("Authorization", "Bearer " + key).header("apikey", key)
                    .timeout(Duration.ofSeconds(20)).build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", e);
        }
    }
}
