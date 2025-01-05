package raingor.ru.userservice;

import raingor.ru.userservice.domain.Role;
import raingor.ru.userservice.domain.User;
import raingor.ru.userservice.dtos.UserInfoDTO;

public class FakeDomain {
    public User createFakeUser() {
        User user = new User();

        user.setName("Fake User");
        user.setEmail("fake@fake.com");
        user.setRole(Role.USER);
        user.setId(1L);

        return user;
    }

    public UserInfoDTO createFakeUserInfoDTO() {
        return new UserInfoDTO(
                null,
                "Fake User",
                "fake@fake.com",
                "USER"
                );
    }
}
