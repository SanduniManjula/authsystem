package authsystem.controller;
import authsystem.entity.Role;
import authsystem.model.response.ApiResponse;
import authsystem.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getRoles() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Roles fetched successfully", roleService.getAllRoles()));
    }
    /*
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getRole(@PathVariable Long id) {
        Optional<Role> role = roleService.getRoleById(id);
        return role.map(value -> ResponseEntity.ok(new ApiResponse<>(true, "Role fetched successfully", value)))
                .orElseGet(() -> ResponseEntity.ok(new ApiResponse<>(false, "Role not found", null)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody Role role) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Role created successfully", roleService.createRole(role)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role deleted successfully", null));
    }

     */
}
