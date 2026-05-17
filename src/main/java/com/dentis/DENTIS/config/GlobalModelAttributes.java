package com.dentis.DENTIS.config;

import com.dentis.DENTIS.model.User;
import com.dentis.DENTIS.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    /** Redirect back to the form with an error flash instead of showing a blank 413 page. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileTooLarge(MaxUploadSizeExceededException ex,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        String cause = ex.getCause() != null ? ex.getCause().getMessage() : "none";
        redirectAttributes.addFlashAttribute("uploadError",
                "Upload error — " + ex.getMessage() + " | cause: " + cause);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
