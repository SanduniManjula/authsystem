package authsystem.controller;
import authsystem.entity.Role;
import authsystem.model.RoleDto;
import authsystem.model.RoleSearchCriteria;
import authsystem.model.response.ApiResponse;
import authsystem.services.DualAuthSystemService;
import authsystem.services.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private DualAuthSystemService dualAuthSystemService;
    /*
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Role>>> getRoles(Pageable pageable) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Roles fetched successfully", roleService.getAllRoles(pageable)));
    }

    @PostMapping("/search-role")
    public ResponseEntity<ApiResponse<Page<Role>>> searchRoles(@ModelAttribute RoleSearchCriteria searchCriteria) {
        Pageable pageable = searchCriteria.toPageable();
        return ResponseEntity.ok(new ApiResponse<>(true, "Search results", roleService.searchRoles(searchCriteria, pageable)));
    }

     */
    @PostMapping("/create-role")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody RoleDto roleDto) {
        RoleDto createdRole = dualAuthSystemService.createPendingRole(roleDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role created and pending approval", createdRole));
    }

    @PutMapping("/update-role/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(@PathVariable Long id, @RequestBody RoleDto updatedRoleDto) {
        RoleDto updatedRole = dualAuthSystemService.updateRole(id, updatedRoleDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role updated and pending approval", updatedRole));
    }

    @PostMapping("/approve-role/{id}")
    public ResponseEntity<ApiResponse<String>> approveRole(@PathVariable Long id) {
        boolean result = dualAuthSystemService.approveRole(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "Role approved", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "Role not found or already processed", null));
    }

    @PostMapping("/reject-role/{id}")
    public ResponseEntity<ApiResponse<String>> rejectRole(@PathVariable Long id) {
        boolean result = dualAuthSystemService.rejectRole(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "Role rejected", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "Role not found or already processed", null));
    }

    @DeleteMapping("/delete-role/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
        boolean isDeleted = dualAuthSystemService.deleteRole(id);
        if (isDeleted) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Role deletion pending approval", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "Role not found or already processed", null));
        }
    }
    @PostMapping("/approveDeletion-role/{id}")
    public ResponseEntity<ApiResponse<String>> approveRoleDeletion(@PathVariable Long id) {
        boolean result = dualAuthSystemService.approveRoleDeletion(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "Role deletion approved", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "Role not found or already processed", null));
    }

    @PostMapping("/rejectDeletion-role/{id}")
    public ResponseEntity<ApiResponse<String>> rejectRoleDeletion(@PathVariable Long id) {
        boolean result = dualAuthSystemService.rejectRoleDeletion(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "Role deletion rejected", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "Role not found or already processed", null));
    }


    /*
    @PostMapping("/activate-role/{id}")
    public ResponseEntity<ApiResponse<String>> activateRole(@PathVariable Long id) {
        boolean activated = dualAuthSystemService.activateRole(id);
        if (activated) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Role activation request submitted successfully and is pending approval.", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "Role not found or activation request failed.", null));
        }
    }

    @PostMapping("/deactivate-role/{id}")
    public ResponseEntity<ApiResponse<String>> deactivateRole(@PathVariable Long id) {
        boolean deactivated = dualAuthSystemService.deactivateRole(id);
        if (deactivated) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Role deactivation request submitted successfully and is pending approval.", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "Role not found or deactivation request failed.", null));
        }
    }

    @PostMapping("/approveActivation-role/{id}")
    public ResponseEntity<ApiResponse<String>> approveRoleActivation(@PathVariable Long id) {
        boolean result = dualAuthSystemService.approveRoleActivation(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "Role activation approved", null))
                : ResponseEntity.ok(new ApiResponse<>(false, "Role is already activated or not found", null));
    }

    @PostMapping("/approveDeactivation-role/{id}")
    public ResponseEntity<ApiResponse<String>> approveRoleDeactivation(@PathVariable Long id) {
        boolean result = dualAuthSystemService.approveRoleDeactivation(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "Role deactivation approved", null))
                : ResponseEntity.ok(new ApiResponse<>(false, "Role is already deactivated or not found", null));
    }

     */
}
