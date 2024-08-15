package authsystem.controller;
import authsystem.model.response.ApiResponse;
import authsystem.model.request.RegisterRequest;
import authsystem.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true, message, null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody RegisterRequest request) {
        String jwt = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", jwt));
    }

}
