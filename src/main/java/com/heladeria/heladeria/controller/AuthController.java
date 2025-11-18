package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.LoginRequestDTO;
import com.heladeria.heladeria.dto.RegisterRequestDTO;
import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.UserRepository;
import com.heladeria.heladeria.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        Map<String, Object> branchData = null;
        if (user.getBranch() != null) {
            branchData = Map.of(
                    "id", user.getBranch().getId(),
                    "name", user.getBranch().getName()
            );
        }

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getIdUser(),
                        "username", user.getUsername(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "role", user.getRole().name(),
                        "branch", branchData
                )
        ));
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {

        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya existe");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDui(dto.getDui());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setWorkDays(dto.getWorkDays());
        user.setSalary(dto.getSalary());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.valueOf(dto.getRole()));

        // Asignar branch si se envía branchId
        if (dto.getBranchId() != null) {
            branchRepository.findById(dto.getBranchId()).ifPresent(user::setBranch);
        }

        userRepository.save(user);

        return ResponseEntity.ok("Usuario registrado correctamente");
    }



}
