package raingor.ru.userservice.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raingor.ru.userservice.dtos.UserInfoDTO;
import raingor.ru.userservice.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserInfoDTO>> getUsers(Pageable pageable) {
        List<UserInfoDTO> pageOfUsers = userService.getUsers(pageable);

        if (!pageOfUsers.isEmpty()) {
            return ResponseEntity.ok().body(pageOfUsers);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable Long id) {
        UserInfoDTO user = userService.getUserById(id);
        return ResponseEntity.ok().body(user);
    }

    @PatchMapping("/{id}")
    public HttpStatus updateUser(@PathVariable Long id, @RequestBody UserInfoDTO user) {
        userService.updateUserById(id, user);
        return HttpStatus.NO_CONTENT;
    }

    //in future if user banned we need set invisible mode on "banned user"
}
