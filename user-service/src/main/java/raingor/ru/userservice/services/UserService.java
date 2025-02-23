package raingor.ru.userservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import raingor.ru.userservice.domain.Role;
import raingor.ru.userservice.domain.User;
import raingor.ru.userservice.dtos.UserInfoDTO;
import raingor.ru.userservice.exceptions.UserExistsWithThisEmailException;
import raingor.ru.userservice.exceptions.UserNotFoundException;
import raingor.ru.userservice.mappers.UserMapper;
import raingor.ru.userservice.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserInfoDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).stream().map(userMapper::userToUserInfoDTO).toList();
    }

    public UserInfoDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userMapper.userToUserInfoDTO(user);
    }

    public void updateUserById(Long id, UserInfoDTO updatedUser) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        user.setName(updatedUser.name() != null ? updatedUser.name() : user.getName());
        user.setAvatar_url(updatedUser.avatar_url() != null ? updatedUser.avatar_url() : user.getAvatar_url());
        //in future need check permissions("admin")
        user.setRole(updatedUser.role() != null ? Role.valueOf(updatedUser.role()) : user.getRole());

        //check email
        if (user.getEmail() != null) {
            Optional<User> existingUser = userRepository.findByEmail(updatedUser.email());

            if (existingUser.isEmpty()) {
                user.setEmail(updatedUser.email());
            } else throw new UserExistsWithThisEmailException();
        }

        userRepository.save(user);
    }

    public ResponseEntity<?> checkUser(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("user", user.get());
            return ResponseEntity.ok(response);

        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    public ResponseEntity<?> createUser(Map<String, Object> userInfo) {
        User newUser = new User();

        newUser.setEmail((String) userInfo.get("email"));
        newUser.setName((String) userInfo.get("name"));
        newUser.setAvatar_url((String) userInfo.get("picture"));
        newUser.setRole(Role.USER);

        userRepository.save(newUser);
        Map<String, Object> response = new HashMap<>();
        response.put("user", newUser);
        return ResponseEntity.ok(response);
    }
}
