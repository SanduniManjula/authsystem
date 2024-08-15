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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody User user, @RequestParam Long creatorId) {
        UserDto createdUser = dualAuthSystemService.createPendingUser(user, creatorId);
        return ResponseEntity.ok(new ApiResponse<>(true, "User created and pending approval", createdUser));
    }
    @PostMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<String>> approveUser(@PathVariable Long id, @RequestParam Long reviewerId) {
        boolean result = dualAuthSystemService.approveUser(id, reviewerId);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User approved", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<ApiResponse<String>> rejectUser(@PathVariable Long id, @RequestParam Long reviewerId) {
        boolean result = dualAuthSystemService.rejectUser(id, reviewerId);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User rejected", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @RequestBody User updatedUser, @RequestParam Long creatorId) {
        UserDto updatedUserDto = dualAuthSystemService.updateUser(id, updatedUser, creatorId);
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated and pending approval", updatedUserDto));
    }
    /*
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id, @RequestParam Long creatorId) {
        boolean isDeleted = dualAuthSystemService.deleteUser(id, creatorId);
        if (isDeleted) {
            return ResponseEntity.ok(new ApiResponse<>(true, "User deletion pending approval", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
        }
    }
    @PostMapping("/approveDeletion/{id}")
    public ResponseEntity<ApiResponse<String>> approveUserDeletion(@PathVariable Long id, @RequestParam Long reviewerId) {
        boolean result = dualAuthSystemService.approveUserDeletion(id, reviewerId);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User deletion approved", null)) :
                ResponseEntity.ok(new ApiResponse<>(false, "User not found or already processed", null));
    }

     */
}
