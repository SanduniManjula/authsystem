package authsystem.services;

import authsystem.annotation.*;
import authsystem.entity.DualAuthSystem;
import authsystem.entity.Permission;
import authsystem.entity.Role;
import authsystem.entity.User;
import authsystem.model.RoleDto;
import authsystem.model.UserDto;
import authsystem.repository.DualAuthSystemRepository;
import authsystem.repository.PermissionRepository;
import authsystem.repository.RoleRepository;
import authsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DualAuthSystemService {

    @Autowired
    private DualAuthSystemRepository dualAuthSystemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    /// User///////////
    public UserDto createPendingUser(User user) {
        Long creatorId = getCurrentUserId();
        log.info("Creating pending user: {} with creatorId: {}", user.getUsername(), creatorId);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLocked(true);
        userRepository.save(user);

        String newUserJson = convertToJson(user);

        DualAuthSystem dualAuthSystem = new DualAuthSystem();
        dualAuthSystem.setEntity("User");
        dualAuthSystem.setNewData(newUserJson);
        dualAuthSystem.setCreatedBy(creatorId);
        dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
        dualAuthSystem.setAction(DualAuthSystem.Action.CREATE);

        dualAuthSystemRepository.save(dualAuthSystem);

        return new UserDto(user.getId(), user.getUsername(), user.getRole().getId());
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found for username: " + username));

        return user.getId();
    }
    @CheckUserLocked
    public UserDto updateUser(Long id, User updatedUser) {
        Long creatorId = getCurrentUserId();
        Optional<User> existingUserOpt = userRepository.findById(id);

        User existingUser = existingUserOpt.orElseThrow(() -> new EntityNotFoundException("User not found"));
    /*
        if(existingUser.isLocked()){
            throw new UserPendingApprovalException("User update is not allowed while the account is in a pending approval state.");
        }

     */

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(updatedUser.getPassword());
            updatedUser.setPassword(encryptedPassword);
        }

        String oldUserJson = convertToJson(existingUser);
        String newUserJson = convertToJson(updatedUser);

        DualAuthSystem dualAuthSystem = new DualAuthSystem();
        dualAuthSystem.setEntity("User");
        dualAuthSystem.setOldData(oldUserJson);
        dualAuthSystem.setNewData(newUserJson);
        dualAuthSystem.setCreatedBy(creatorId);
        dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
        dualAuthSystem.setAction(DualAuthSystem.Action.UPDATE);

        dualAuthSystemRepository.save(dualAuthSystem);

        updatedUser.setLocked(true);
        userRepository.save(updatedUser);

        return new UserDto(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getRole().getId());
    }

    @UnlockUserAfterApproval
    public boolean approveUser(Long id) {
        Long reviewerId = getCurrentUserId();
        return processUser(id, reviewerId, DualAuthSystem.Status.APPROVED);
    }

    public boolean rejectUser(Long id) {
        Long reviewerId = getCurrentUserId();
        return processUser(id, reviewerId, DualAuthSystem.Status.REJECTED);
    }

    private boolean processUser(Long id, Long reviewerId, DualAuthSystem.Status status) {
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getNewData(), User.class);

            if (status == DualAuthSystem.Status.APPROVED) {
                if (user.getId() == null) {
                    throw new IllegalArgumentException("User ID must not be null");
                }
                Optional<User> existingUserOpt = userRepository.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    User existingUser = existingUserOpt.get();
                    existingUser.setLocked(false);
                    existingUser.setUsername(user.getUsername());
                    existingUser.setRole(user.getRole());
                    existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(existingUser);
                } else {
                    throw new EntityNotFoundException("User not found for approval");
                }
            } else if (status == DualAuthSystem.Status.REJECTED) {
                if (user.getId() == null) {
                    throw new IllegalArgumentException("User ID must not be null");
                }
                Optional<User> existingUserOpt = userRepository.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    User existingUser = existingUserOpt.get();
                    existingUser.setLocked(false);
                    userRepository.save(existingUser);
                } else {
                    throw new EntityNotFoundException("User not found for rejection");
                }
            }

            dualAuthSystem.setStatus(status);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    @CheckUserLocked
    public boolean deleteUser(Long userId) {
        Long creatorId = getCurrentUserId();
       return userRepository.findById(userId).map(user -> {
            user.setLocked(true);
            userRepository.save(user);
            String oldUserData = convertToJson(user);

        DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("User");
            dualAuthSystem.setOldData(oldUserData);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.DELETE);

            dualAuthSystemRepository.save(dualAuthSystem);



            return true;
        }).orElse(false);
    }

    public boolean approveUserDeletion(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);
            userRepository.deleteById(user.getId());
            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    public boolean rejectUserDeletion(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);
            user.setLocked(false);
            userRepository.save(user);
            dualAuthSystem.setStatus(DualAuthSystem.Status.REJECTED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }
    @CheckBeforeActivation
    public boolean activateUser(Long userId) {
        Long creatorId = getCurrentUserId();

        return userRepository.findById(userId).map(user -> {

            if (user.getStatus() == User.Status.ACTIVATED) {
                log.info("User {} is already activated.", userId);

                return false;
            }

            String userJson = convertToJson(user);

            DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("User");
            dualAuthSystem.setOldData(userJson);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.ACTIVATE);

            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }
    @CheckBeforeDeactivation
    public boolean deactivateUser(Long userId) {
        Long creatorId = getCurrentUserId();

        return userRepository.findById(userId).map(user -> {

            if (user.getStatus() == User.Status.DEACTIVATED) {
                log.info("User {} is already deactivated.", userId);

                return false;
            }


            String userJson = convertToJson(user);

            DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("User");
            dualAuthSystem.setOldData(userJson);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.DEACTIVATE);

            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }


    public boolean approveActivation(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);

            if (user.getStatus() == User.Status.ACTIVATED) {
                log.info("User {} is already activated.", user.getId());
                return false;
            }


            user.setStatus(User.Status.ACTIVATED);
            userRepository.save(user);

            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }


    public boolean approveDeactivation(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);
            if (user.getStatus() == User.Status.DEACTIVATED) {
                log.info("User {} is already deactivated.", user.getId());
                return false;
            }
            user.setStatus(User.Status.DEACTIVATED);
            userRepository.save(user);

            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }





    //Role///////
    public RoleDto createPendingRole(RoleDto roleDto) {
        Long creatorId = getCurrentUserId();
        log.info("Creating pending role: {} with creatorId: {}", roleDto.getName(), creatorId);
       // Role role = convertToEntity(roleDto);
       // role.setActivated(false);

        String newRoleJson = convertToJson(roleDto);

        DualAuthSystem dualAuthSystem = new DualAuthSystem();
        dualAuthSystem.setEntity("Role");
        dualAuthSystem.setNewData(newRoleJson);
        dualAuthSystem.setCreatedBy(creatorId);
        dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
        dualAuthSystem.setAction(DualAuthSystem.Action.CREATE);

        dualAuthSystemRepository.save(dualAuthSystem);

        return roleDto;
    }
    @CheckRoleLocked
    public RoleDto updateRole(Long id, RoleDto updatedRoleDto) {
        Long creatorId = getCurrentUserId();
        Optional<Role> existingRoleOpt = roleRepository.findById(id);

        Role existingRole = existingRoleOpt.orElseThrow(() -> new EntityNotFoundException("Role not found"));

        existingRole.setLocked(true);
        roleRepository.save(existingRole);
        RoleDto existingRoleDto = convertToDto(existingRole);
        String oldRoleJson = convertToJson(existingRoleDto);
        String newRoleJson = convertToJson(updatedRoleDto);

        DualAuthSystem dualAuthSystem = new DualAuthSystem();
        dualAuthSystem.setEntity("Role");
        dualAuthSystem.setOldData(oldRoleJson);
        dualAuthSystem.setNewData(newRoleJson);
        dualAuthSystem.setCreatedBy(creatorId);
        dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
        dualAuthSystem.setAction(DualAuthSystem.Action.UPDATE);

        dualAuthSystemRepository.save(dualAuthSystem);

        Role updatedRole = convertToEntity(updatedRoleDto);
        //updatedRole.setActivated(false);

        return convertToDto(roleRepository.save(updatedRole));
    }
    @UnlockRoleAfterApproval
    public boolean approveRole(Long id) {
        Long reviewerId = getCurrentUserId();
        return processRole(id, reviewerId, DualAuthSystem.Status.APPROVED);
    }
    @UnlockRoleAfterApproval
    public boolean rejectRole(Long id) {
        Long reviewerId = getCurrentUserId();
        return processRole(id, reviewerId, DualAuthSystem.Status.REJECTED);
    }

    private boolean processRole(Long id, Long reviewerId, DualAuthSystem.Status status) {
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            RoleDto roleDto = convertFromJson(dualAuthSystem.getNewData(), RoleDto.class);

            if (status == DualAuthSystem.Status.APPROVED) {
                Role role = convertToEntity(roleDto);
              //  role.setActivated(true);
                role.setLocked(false);
                roleRepository.save(role);
            } else if (status == DualAuthSystem.Status.REJECTED) {
                Role role = convertToEntity(roleDto);
                role.setLocked(false);
                roleRepository.deleteById(role.getId());
            }

            dualAuthSystem.setStatus(status);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    @CheckRoleLocked
    public boolean deleteRole(Long roleId) {
        Long creatorId = getCurrentUserId();
        return roleRepository.findById(roleId).map(role -> {
            RoleDto roleDto = convertToDto(role);
           // roleRepository.deleteById(roleId);
            role.setLocked(true);
            roleRepository.save(role);
            String oldRoleData = convertToJson(roleDto);

            DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("Role");
            dualAuthSystem.setOldData(oldRoleData);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.DELETE);

            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }

    @UnlockRoleAfterApproval
    public boolean approveRoleDeletion(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            RoleDto roleDto = convertFromJson(dualAuthSystem.getOldData(), RoleDto.class);
            Role role = convertToEntity(roleDto);
            roleRepository.deleteById(role.getId());
            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    @UnlockRoleAfterApproval
    public boolean rejectRoleDeletion(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            RoleDto roleDto = convertFromJson(dualAuthSystem.getOldData(), RoleDto.class);
            Role role = convertToEntity(roleDto);
            role.setLocked(false);
            roleRepository.save(role);
            dualAuthSystem.setStatus(DualAuthSystem.Status.REJECTED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    @ActivateRole
    public boolean activateRole(Long roleId) {
        Long creatorId = getCurrentUserId();

        return roleRepository.findById(roleId).map(role -> {

            if (role.getStatus() == Role.Status.ACTIVATED) {
                log.info("Role {} is already activated.", roleId);
                return false;
            }

            String roleJson = convertToJson(role);

            DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("Role");
            dualAuthSystem.setOldData(roleJson);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.ACTIVATE);

            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }


    @DeactivateRole
    public boolean deactivateRole(Long roleId) {
        Long creatorId = getCurrentUserId();

        return roleRepository.findById(roleId).map(role -> {

            if (role.getStatus() == Role.Status.DEACTIVATED) {
                log.info("Role {} is already deactivated.", roleId);
                return false;
            }

            String roleJson = convertToJson(role);

            DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("Role");
            dualAuthSystem.setOldData(roleJson);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.DEACTIVATE);

            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }

    // Method to approve role activation
    public boolean approveRoleActivation(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            Role role = convertFromJson(dualAuthSystem.getOldData(), Role.class);

            if (role.getStatus() == Role.Status.ACTIVATED) {
                log.info("Role {} is already activated.", role.getId());
                return false;
            }

            role.setStatus(Role.Status.ACTIVATED);
            role.setLocked(false); // Ensure role is unlocked upon activation
            roleRepository.save(role);

            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }

    // Method to approve role deactivation
    public boolean approveRoleDeactivation(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            Role role = convertFromJson(dualAuthSystem.getOldData(), Role.class);

            if (role.getStatus() == Role.Status.DEACTIVATED) {
                log.info("Role {} is already deactivated.", role.getId());
                return false;
            }

            role.setStatus(Role.Status.DEACTIVATED);
            role.setLocked(false);
            roleRepository.save(role);

            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }




    private Role convertToEntity(RoleDto roleDto) {
        Role role = new Role();
        role.setId(roleDto.getId());
        role.setName(roleDto.getName());
      //  role.setActivated(roleDto.isActivated());
        Set<Permission> permissions = roleDto.getPermissionIds().stream()
                .map(permissionId -> {
                    Permission permission = permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));
                    return permission;
                })
                .collect(Collectors.toSet());
        role.setPermissions(permissions);
        return role;
    }

    private RoleDto convertToDto(Role role) {
        Set<Long> permissionIds = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());

        return new RoleDto(
                role.getId(),
                role.getName(),
               // role.isActivated(),
                permissionIds
        );
    }



    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    private <T> T convertFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert from JSON", e);
        }
    }

}
