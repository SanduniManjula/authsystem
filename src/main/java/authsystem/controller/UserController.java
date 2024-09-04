package authsystem.controller;
import authsystem.entity.User;
import authsystem.model.UserDto;
import authsystem.model.UserSearchCriteria;
import authsystem.model.response.ApiResponse;
import authsystem.services.DualAuthSystemService;
import authsystem.services.ReportService;
import authsystem.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReportService reportService;

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

@PostMapping("/activate/{userId}")
public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long userId) {
    boolean activated = dualAuthSystemService.activateUser(userId);
    if (activated) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User activation request submitted successfully and is pending approval.", null));
    } else {
        return ResponseEntity.ok(new ApiResponse<>(false, "User not found or activation request failed.", null));
    }
}

    @PostMapping("/deactivate/{userId}")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long userId) {
        boolean deactivated = dualAuthSystemService.deactivateUser(userId);
        if (deactivated) {
            return ResponseEntity.ok(new ApiResponse<>(true, "User deactivation request submitted successfully and is pending approval.", null));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(false, "User not found or deactivation request failed.", null));
        }
    }


@PostMapping("/approveActivation/{id}")
public ResponseEntity<ApiResponse<String>> approveActivation(@PathVariable Long id) {
    boolean result = dualAuthSystemService.approveActivation(id);
    return result ? ResponseEntity.ok(new ApiResponse<>(true, "User activation approved", null))
            : ResponseEntity.ok(new ApiResponse<>(false, "User is already activated or not found", null));
}

    @PostMapping("/approveDeactivation/{id}")
    public ResponseEntity<ApiResponse<String>> approveDeactivation(@PathVariable Long id) {
        boolean result = dualAuthSystemService.approveDeactivation(id);
        return result ? ResponseEntity.ok(new ApiResponse<>(true, "User deactivation approved", null))
                : ResponseEntity.ok(new ApiResponse<>(false, "User is already deactivated or not found", null));
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> generateUserReport(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam("reportType") String reportType) {

        byte[] reportData = reportService.generateUserReport(fromDate, toDate, reportType.trim());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_report." + reportType.trim());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
    }




}





