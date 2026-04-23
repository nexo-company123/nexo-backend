package pl.edu.uj.tp.nexo.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.uj.tp.nexo.organization.repository.OrganizationRepository;
import pl.edu.uj.tp.nexo.organization.service.OrganizationNotFoundException;
import pl.edu.uj.tp.nexo.user.dto.UpdateUserRequest;
import pl.edu.uj.tp.nexo.user.dto.UserResponse;
import pl.edu.uj.tp.nexo.user.entity.User;
import pl.edu.uj.tp.nexo.user.repository.UserRepository;
import pl.edu.uj.tp.nexo.validation.UserDataValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final UserDataValidator userDataValidator;

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByOrganization(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new OrganizationNotFoundException(organizationId);
        }
        return userRepository.findAllByOrganizationId(organizationId).stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.getFirstName() != null) {
            userDataValidator.validateName(request.getFirstName(), pl.edu.uj.tp.nexo.exception.ErrorInfo.INVALID_FIRST_NAME);
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            userDataValidator.validateName(request.getLastName(), pl.edu.uj.tp.nexo.exception.ErrorInfo.INVALID_LAST_NAME);
            user.setLastName(request.getLastName());
        }
        if (request.getPassword() != null) {
            userDataValidator.validatePassword(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        user = userRepository.save(user);
        return toUserResponse(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .role(user.getRole())
                .build();
    }
}