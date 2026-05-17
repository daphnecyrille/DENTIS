package com.dentis.DENTIS.controller;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
public class SysAdminController {

    private final UserRepository userRepository;

    public SysAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/dashboard-sysadmin")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("currentUser", getCurrentUser(authentication));

        List<User> users = userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getLastName))
                .toList();

        long totalUsers    = users.size();
        long activeUsers   = users.stream().filter(u -> u.getStatus() == AccountStatus.ACTIVE).count();
        long clinicians    = users.stream().filter(u -> u.getRole() == Role.CLINICIAN).count();
        long faculty       = users.stream().filter(u -> u.getRole() == Role.FACULTY).count();
        long managers      = users.stream().filter(u -> u.getRole() == Role.CLINIC_MANAGER).count();

        model.addAttribute("users",          users);
        model.addAttribute("statTotalUsers", totalUsers);
        model.addAttribute("statActiveUsers",activeUsers);
        model.addAttribute("statClinicians", clinicians);
        model.addAttribute("statFaculty",    faculty);
        model.addAttribute("statManagers",   managers);

        return "dashboard-sysadmin";
    }

    @PostMapping("/create-user-sysadmin")
    public String createUser(@RequestParam String email,
                             @RequestParam String role,
                             @RequestParam(required = false) String firstName,
                             @RequestParam(required = false) String lastName,
                             @RequestParam(required = false) String section) {
        User user = new User();
        user.setFirstName(firstName != null && !firstName.isBlank() ? firstName.trim() : "");
        user.setLastName(lastName != null && !lastName.isBlank() ? lastName.trim() : "");
        user.setEmail(email.trim().toLowerCase());
        user.setRole(Role.valueOf(role));
        user.setStatus(AccountStatus.ACTIVE);
        if (section != null && !section.isBlank()) user.setSection(section.trim());
        userRepository.save(user);
        return "redirect:/dashboard-sysadmin";
    }

    @PostMapping("/edit-user-sysadmin")
    public String editUser(@RequestParam Long userId,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String email,
                           @RequestParam String role,
                           @RequestParam(required = false) String section,
                           @RequestParam(required = false) String yearLevel,
                           @RequestParam(required = false) String studentNumber) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setRole(Role.valueOf(role));
        user.setSection((section != null && !section.isBlank()) ? section.trim() : null);
        user.setYearLevel((yearLevel != null && !yearLevel.isBlank()) ? yearLevel.trim() : null);
        user.setStudentNumber((studentNumber != null && !studentNumber.isBlank()) ? studentNumber.trim() : null);
        userRepository.save(user);
        return "redirect:/dashboard-sysadmin";
    }

    @PostMapping("/toggle-status-sysadmin")
    public String toggleStatus(@RequestParam Long userId,
                               @RequestParam String action) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setStatus("activate".equals(action) ? AccountStatus.ACTIVE : AccountStatus.INACTIVE);
        userRepository.save(user);
        return "redirect:/dashboard-sysadmin";
    }
}
