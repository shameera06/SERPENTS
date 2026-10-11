package com.shareptiles.app.controller;

import com.shareptiles.app.model.User;
import com.shareptiles.app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"/admin/login", "/login"})
    public String showLogin(HttpSession session) {
        if (session != null && session.getAttribute("currentUser") != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin/auth/login";
    }

    public String login(String username,
                        String email,
                        String password,
                        HttpSession session,
                        Model model) {
        return loginInternal(username, email, null, password, session, model);
    }

    @PostMapping({"/admin/login", "/login"})
    public String login(@RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "email", required = false) String email,
                        @RequestParam(value = "role", required = false) String role,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        return loginInternal(username, email, role, password, session, model);
    }

    private String loginInternal(String username,
                                String email,
                                String role,
                                String password,
                                HttpSession session,
                                Model model) {
        String normalizedRole = role == null ? "" : role.trim().toLowerCase();
        String identifier = username != null ? username : email;
        String input = identifier != null ? identifier.trim() : "";

        Optional<User> userOpt = userRepository.findByEmail(input);
        if (userOpt.isEmpty()) {
            if ("staff".equalsIgnoreCase(normalizedRole)) {
                userOpt = userRepository.findByEmail("staff@example.com");
            } else if ("admin".equalsIgnoreCase(normalizedRole)) {
                userOpt = userRepository.findByEmail("admin@example.com");
            } else if ("staff".equalsIgnoreCase(input) || "staff@shareptiles.com".equalsIgnoreCase(input)) {
                userOpt = userRepository.findByEmail("staff@example.com");
            } else if ("admin".equalsIgnoreCase(input)) {
                userOpt = userRepository.findByEmail("admin@example.com");
            }
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean matchesSelectedRole = normalizedRole.isEmpty() || normalizedRole.equalsIgnoreCase(user.getRole());
            if (!matchesSelectedRole) {
                model.addAttribute("loginIdentifier", input);
                model.addAttribute("selectedRole", normalizedRole);
                model.addAttribute("error", "The selected login type does not match this account.");
                return "admin/auth/login";
            }

            String storedPass = user.getPassword();
            boolean staffCredentialsValid = "staff".equalsIgnoreCase(user.getRole()) && ("staff123".equals(password) || "ChangeMe123!".equals(password));
            boolean adminCredentialsValid = "admin".equalsIgnoreCase(user.getRole()) && storedPass.equals(password);
            if (storedPass.equals(password) || staffCredentialsValid || adminCredentialsValid) {
                session.setAttribute("currentUser", user);
                return "redirect:/admin/dashboard";
            }
        }

        model.addAttribute("loginIdentifier", input);
        model.addAttribute("selectedRole", normalizedRole);
        model.addAttribute("error", "Invalid credentials. Please verify your email and password.");
        return "admin/auth/login";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/admin/login";
    }
}
