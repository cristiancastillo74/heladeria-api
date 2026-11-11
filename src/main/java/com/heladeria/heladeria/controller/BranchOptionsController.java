package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.BranchOptionDTO;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/branches")
public class BranchOptionsController {
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public BranchOptionsController(BranchRepository branchRepository, UserRepository userRepository) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<BranchOptionDTO> listBranches(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        var user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (user.getRole() == Role.ADMIN_GLOBAL) {
            return branchRepository.findAll().stream()
                    .map(branch -> new BranchOptionDTO(branch.getId(), branch.getName())).toList();
        }
        if (user.getBranch() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes una sucursal asignada");
        }
        return List.of(new BranchOptionDTO(user.getBranch().getId(), user.getBranch().getName()));
    }
}