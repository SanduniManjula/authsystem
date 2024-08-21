package authsystem.controller;
import authsystem.entity.User;
import authsystem.model.UserDto;
import authsystem.model.UserSearchCriteria;
import authsystem.model.response.ApiResponse;
import authsystem.services.DualAuthSystemService;
import authsystem.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DualAuthSystemService dualAuthSystemService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Users fetched successfully", userService.getAllUsers(pageable)));

    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<User>>> searchUsers(@ModelAttribute UserSearchCriteria searchCriteria) {
        Pageable pageable = searchCriteria.toPageable();
        return ResponseEntity.ok(new ApiResponse<>(true, "Search results", userService.searchUsers(searchCriteria, pageable)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody User user) {
        UserDto createdUser = dualAuthSystemService.createPendingUser(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "User created and pending approval", createdUser));
    }


    @PostMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<String>> approveUser(@PathVariable Long id) {
        boolean result = dualAuthSystemService.approveUser(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User approved", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<ApiResponse<String>> rejectUser(@PathVariable Long id) {
        boolean result = dualAuthSystemService.rejectUser(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User rejected", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        UserDto updatedUserDto = dualAuthSystemService.updateUser(id, updatedUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated and pending approval", updatedUserDto));
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {

        boolean isDeleted = dualAuthSystemService.deleteUser(id);
        if (isDeleted) {
            return ResponseEntity.ok(new ApiResponse<>(true, "User deletion pending approval", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
        }
    }

    @PostMapping("/approveDeletion/{id}")
    public ResponseEntity<ApiResponse<String>> approveUserDeletion(@PathVariable Long id) {
        boolean result = dualAuthSystemService.approveUserDeletion(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User deletion approved", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
    }

    @PostMapping("/rejectDeletion/{id}")
    public ResponseEntity<ApiResponse<String>> rejectUserDeletion(@PathVariable Long id) {
        boolean result = dualAuthSystemService.rejectUserDeletion(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User deletion rejected", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
    }

/*
    @PostMapping("/activate/{userId}")
    public ResponseEntity<String> activateUser(@PathVariable Long userId) {
        boolean activated = dualAuthSystemService.activateUser(userId);
        if (activated) {
            return ResponseEntity.ok("User activation request submitted successfully and is pending approval.");
        } else {
            return ResponseEntity.badRequest().body("User not found or activation request failed.");
        }
    }

    @PostMapping("/approveActivation/{id}")
    public ResponseEntity<String> approveActivation(@PathVariable Long id) {
        boolean approved = dualAuthSystemService.approveActivation(id);
        if (approved) {
            return ResponseEntity.ok("User activation approved. The user is now activated.");
        } else {
            return ResponseEntity.badRequest().body("Activation approval failed. Record not found or not in pending status.");
        }
    }

    @PostMapping("/rejectActivation/{id}")
    public ResponseEntity<String> rejectActivation(@PathVariable Long id) {
        boolean rejected = dualAuthSystemService.rejectActivation(id);
        if (rejected) {
            return ResponseEntity.ok("User activation rejected. The user is now deactivated.");
        } else {
            return ResponseEntity.badRequest().body("Activation rejection failed. Record not found or not in pending status.");
        }
    }

 */
@PostMapping("/activate/{userId}")
public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long userId) {
    boolean activated = dualAuthSystemService.activateUser(userId);
    if (activated) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User activation request submitted successfully and is pending approval.", null));
    } else {
        return ResponseEntity.ok(new ApiResponse<>(false, "User not found or activation request failed.", null));
    }
}

    @PostMapping("/approveActivation/{id}")
    public ResponseEntity<ApiResponse<String>> approveActivation(@PathVariable Long id) {
        boolean approved = dualAuthSystemService.approveActivation(id);
        if (approved) {
            return ResponseEntity.ok(new ApiResponse<>(true, "User activation approved. The user is now activated.", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "Activation approval failed. Record not found or not in pending status.", null));
        }
    }

    @PostMapping("/approveDeactivation/{id}")
    public ResponseEntity<ApiResponse<String>> approveDeactivation(@PathVariable Long id) {
        boolean deactivated = dualAuthSystemService.approveDeactivation(id);
        if (deactivated) {
            return ResponseEntity.ok(new ApiResponse<>(true, "User deactivation approved. The user is now deactivated.", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "Deactivation approval failed. Record not found or not in pending status.", null));
        }
    }

}





 