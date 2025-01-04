package raingor.ru.userservice.mappers;

import org.springframework.stereotype.Component;
import raingor.ru.userservice.domain.Role;
import raingor.ru.userservice.domain.User;
import raingor.ru.userservice.dtos.UserInfoDTO;

@Component
public class UserMapper {
    public UserInfoDTO userToUserInfoDTO(User user) {
        return new UserInfoDTO(
                user.getAvatar_url(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public User createdUserDTOToUser(UserInfoDTO userInfoDTO) {
        User user = new User();

        user.setEmail(userInfoDTO.email());
        user.setName(userInfoDTO.name());
        user.setAvatar_url(userInfoDTO.avatar_url());
        user.setRole(Role.USER);

        return user;
    }
}
