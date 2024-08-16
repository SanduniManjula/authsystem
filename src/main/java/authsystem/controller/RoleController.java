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

}
