package com.heladeria.heladeria.security;

import com.heladeria.heladeria.model.Branch;
import com.heladeria.heladeria.model.Role;
import com.heladeria.heladeria.model.User;
import com.heladeria.heladeria.repository.BranchRepository;
import com.heladeria.heladeria.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryAccessService {
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    public InventoryAccessService(UserRepository userRepository, BranchRepository branchRepository) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
    }

    public User actor(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public Long visibleBranchId(User actor) {
        if (actor.getRole() == Role.ADMIN_GLOBAL) return null;
        if (actor.getRole() == Role.EMPLOYEE && !actor.isCanViewInventory()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para consultar inventarios");
        }
        return requireAssignedBranch(actor).getId();
    }

    public void requireInventoryManager(User actor) {
        if (actor.getRole() != Role.ADMIN_GLOBAL && actor.getRole() != Role.ADMIN_BRANCH) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden modificar inventarios");
        }
    }

    public Branch resolveWriteBranch(User actor, Long requestedBranchId) {
        requireInventoryManager(actor);
        if (actor.getRole() == Role.ADMIN_BRANCH) {
            Branch assignedBranch = requireAssignedBranch(actor);
            if (requestedBranchId != null && !assignedBranch.getId().equals(requestedBranchId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asignar inventario a otra sucursal");
            }
            return assignedBranch;
        }
        if (requestedBranchId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una sucursal");
        }
        return branchRepository.findById(requestedBranchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal no encontrada"));
    }

    public void ensureCanManageBranch(User actor, Branch targetBranch) {
        requireInventoryManager(actor);
        if (actor.getRole() == Role.ADMIN_BRANCH) {
            Branch assignedBranch = requireAssignedBranch(actor);
            if (targetBranch == null || !assignedBranch.getId().equals(targetBranch.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar inventario de otra sucursal");
            }
        }
    }

    private Branch requireAssignedBranch(User actor) {
        if (actor.getBranch() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu usuario no tiene sucursal asignada");
        }
        return actor.getBranch();
    }
}
