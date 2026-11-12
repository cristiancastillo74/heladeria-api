package com.heladeria.heladeria.controller;

import com.heladeria.heladeria.dto.UserManagementRequestDTO;
import com.heladeria.heladeria.dto.UserResponseDTO;
import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementController(
            UserRepository userRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserResponseDTO> listUsers(Authentication authentication) {
        User actor = getActor(authentication);
        List<User> users = isGlobalAdmin(actor)
                ? userRepository.findAll()
            : userRepository.findByBranch_IdAndRole(requireActorBranch(actor).getId(), Role.EMPLOYEE);
        return users.stream().map(this::toResponse).toList();
    }

    @GetMapping("/roles")
    public List<Role> listAssignableRoles(Authentication authentication) {
        User actor = getActor(authentication);
        return isGlobalAdmin(actor) ? Arrays.asList(Role.values()) : List.of(Role.EMPLOYEE);
    }

    @PostMapping
    public UserResponseDTO createUser(
            @Valid @RequestBody UserManagementRequestDTO request,
            Authentication authentication) {
        User actor = getActor(authentication);
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }
        if (!isGlobalAdmin(actor) && Boolean.TRUE.equals(request.canViewInventory())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN_GLOBAL puede habilitar acceso de lectura de inventarios");
        }

        Role role = parseRole(request.role());
        User user = new User();
        applyRequest(user, request, role, resolveBranch(actor, role, request.branchId()), actor, true);
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public UserResponseDTO updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserManagementRequestDTO request,
            Authentication authentication) {
        User actor = getActor(authentication);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        ensureManageable(actor, user);

        userRepository.findByUsername(request.username()).ifPresent(existing -> {
            if (!existing.getIdUser().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
            }
        });

        Role role = parseRole(request.role());
        if (user.getIdUser().equals(actor.getIdUser()) && role != user.getRole()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes cambiar tu propio rol");
        }
        ensureAssignableRole(actor, role);
        if (!isGlobalAdmin(actor) && Boolean.TRUE.equals(request.canViewInventory())
            && !user.isCanViewInventory()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN_GLOBAL puede habilitar acceso de lectura de inventarios");
        }
        applyRequest(user, request, role, resolveBranch(actor, role, request.branchId()), actor, false);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, Authentication authentication) {
        User actor = getActor(authentication);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (user.getIdUser().equals(actor.getIdUser())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes eliminar tu propio usuario");
        }
        ensureManageable(actor, user);
        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar este usuario porque tiene registros asociados."
            );
        }
    }

    private User getActor(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private boolean isGlobalAdmin(User user) {
        return user.getRole() == Role.ADMIN_GLOBAL;
    }

    private Branch requireActorBranch(User actor) {
        if (actor.getBranch() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario administrador no tiene sucursal asignada");
        }
        return actor.getBranch();
    }

    private void ensureManageable(User actor, User target) {
        if (isGlobalAdmin(actor)) return;
        Branch branch = requireActorBranch(actor);
        if (target.getRole() != Role.EMPLOYEE || target.getBranch() == null
                || !target.getBranch().getId().equals(branch.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes administrar empleados de tu sucursal");
        }
    }

    private void ensureAssignableRole(User actor, Role role) {
        if (!isGlobalAdmin(actor) && role != Role.EMPLOYEE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN_GLOBAL puede asignar roles administrativos");
        }
    }

    private Role parseRole(String roleName) {
        try {
            return Role.valueOf(roleName);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no válido");
        }
    }

    private Branch resolveBranch(User actor, Role role, Long requestedBranchId) {
        if (!isGlobalAdmin(actor)) {
            ensureAssignableRole(actor, role);
            return requireActorBranch(actor);
        }
        if (role == Role.ADMIN_GLOBAL && requestedBranchId == null) return null;
        if (requestedBranchId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una sucursal para este rol");
        }
        return branchRepository.findById(requestedBranchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal no encontrada"));
    }

    private void applyRequest(User user, UserManagementRequestDTO request, Role role, Branch branch,
                              User actor, boolean creating) {
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setDui(request.dui());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setSalary(request.salary());
        user.setWorkDays(request.workDays());
        user.setUsername(request.username().trim());
        user.setRole(role);
        user.setBranch(branch);
        if (role != Role.EMPLOYEE) {
            user.setCanViewInventory(false);
        } else if (isGlobalAdmin(actor)) {
            user.setCanViewInventory(Boolean.TRUE.equals(request.canViewInventory()));
        } else if (creating) {
            user.setCanViewInventory(false);
        }
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getIdUser(), user.getFirstName(), user.getLastName(), user.getDui(),
                user.getPhone(), user.getAddress(), user.getSalary(), user.getWorkDays(),
                user.getUsername(), user.getRole().name(),
                user.getBranch() == null ? null : user.getBranch().getId(),
                user.getBranch() == null ? null : user.getBranch().getName(),
                user.isCanViewInventory());
    }
}