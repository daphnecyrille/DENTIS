package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.ProsthodonticsChart;
import com.dentis.DENTIS.model.ProsthodonticsChartStatus;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import com.dentis.DENTIS.service.FileStorage;
import com.dentis.DENTIS.service.ProsthodonticsChartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Uploading and viewing attached files (served at /attachments/...) (Prosthodontics booklet). Every route here needs a login. */
@RestController
public class FileController {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "heic", "heif", "webp", "pdf");

    private final FileStorage storage;
    private final ProsthodonticsChartService prosthoService;
    private final UserRepository userRepository;

    public FileController(FileStorage storage, ProsthodonticsChartService prosthoService, UserRepository userRepository) {
        this.storage = storage;
        this.prosthoService = prosthoService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof OAuth2User u)
            return userRepository.findByEmail(u.getAttribute("email")).orElse(null);
        return null;
    }

    /** Step 1 of an upload: the chart's clinician asks for a one-time upload link. */
    @PostMapping("/attachments/sign")
    public ResponseEntity<?> sign(@RequestParam Long chartId, @RequestParam String field,
                                  @RequestParam String name, Authentication auth) throws IOException {
        User me = currentUser(auth);
        ProsthodonticsChart chart = prosthoService.getById(chartId);
        if (me == null || chart.getClinician() == null || !chart.getClinician().getId().equals(me.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only the chart's clinician can upload files."));
        if (chart.getStatus() == ProsthodonticsChartStatus.APPROVED)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "This chart is already approved."));
        if (!field.matches("[A-Za-z]{1,40}"))
            return ResponseEntity.badRequest().body(Map.of("error", "Bad field."));

        String clean = Path.of(name).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        int dot = clean.lastIndexOf('.');
        String ext = dot >= 0 ? clean.substring(dot + 1).toLowerCase() : "";
        if (!ALLOWED_EXT.contains(ext))
            return ResponseEntity.badRequest().body(Map.of("error", "Only photos (JPG, PNG, HEIC) and PDF files can be uploaded."));
        if (clean.length() > 80) clean = clean.substring(clean.length() - 80);

        String key = "prosthodontics/" + chartId + "/" + field + "_" + UUID.randomUUID() + "_" + clean;
        return ResponseEntity.ok(Map.of("uploadUrl", storage.uploadUrl(key), "path", "/attachments/" + key));
    }

    /** Local mode only: receives the file body (on Vercel the browser uploads straight to Supabase). */
    @PutMapping("/attachments/upload")
    public ResponseEntity<?> uploadLocal(@RequestParam String key, HttpServletRequest request, Authentication auth) throws IOException {
        if (storage.isRemote() || currentUser(auth) == null || !FileStorage.isSafeKey(key) || !key.startsWith("prosthodontics/"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        storage.saveLocal(key, request.getInputStream());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Viewing a file: logged-in users only. */
    @GetMapping("/attachments/**")
    public ResponseEntity<?> view(HttpServletRequest request, Authentication auth) throws IOException {
        if (currentUser(auth) == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        String key = request.getRequestURI().substring(request.getContextPath().length() + "/attachments/".length());
        if (!FileStorage.isSafeKey(key)) return ResponseEntity.notFound().build();

        if (storage.isRemote())
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(storage.readUrl(key))).build();

        Path p = storage.localPath(key);
        if (!Files.exists(p)) return ResponseEntity.notFound().build();
        Resource file = new FileSystemResource(p);
        MediaType type = MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(type).body(file);
    }
}
