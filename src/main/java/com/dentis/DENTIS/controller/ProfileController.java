package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/complete-profile")
    public String showForm(Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));
        return "complete-profile";
    }

    @PostMapping("/complete-profile")
    public String saveProfile(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam(required = false) String middleName,
                              @RequestParam(required = false) String studentNumber,
                              @RequestParam(required = false) String yearLevel,
                              Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) return "redirect:/login";

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        if (middleName != null && !middleName.isBlank()) user.setMiddleName(middleName.trim());
        if (studentNumber != null && !studentNumber.isBlank()) user.setStudentNumber(studentNumber.trim());
        if (yearLevel != null && !yearLevel.isBlank()) user.setYearLevel(yearLevel.trim());
        userRepository.save(user);

        return switch (user.getRole()) {
            case CLINICIAN -> "redirect:/clinician-dashboard";
            case FACULTY -> "redirect:/faculty-dashboard";
            case CLINIC_MANAGER -> "redirect:/dashboard-clinicmanager";
            case ADMIN -> "redirect:/dashboard-sysadmin";
        };
    }
}
