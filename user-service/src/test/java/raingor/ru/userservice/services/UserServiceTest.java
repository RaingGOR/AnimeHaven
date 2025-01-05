package raingor.ru.userservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import raingor.ru.userservice.FakeDomain;
import raingor.ru.userservice.domain.Role;
import raingor.ru.userservice.domain.User;
import raingor.ru.userservice.dtos.UserInfoDTO;
import raingor.ru.userservice.exceptions.UserExistsWithThisEmailException;
import raingor.ru.userservice.exceptions.UserNotFoundException;
import raingor.ru.userservice.mappers.UserMapper;
import raingor.ru.userservice.repositories.UserRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    private FakeDomain fakeDomain;
    private User fakeUser;
    private UserInfoDTO fakeUserInfoDTO;

    @BeforeEach
    void setUp() {
        fakeDomain = new FakeDomain();
        fakeUser = fakeDomain.createFakeUser();
        fakeUserInfoDTO = fakeDomain.createFakeUserInfoDTO();
    }

    @Test
    void test_getUsers_shouldReturnUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> fakeUserList = List.of(fakeUser, fakeUser);
        Page<User> fakeUserPage = new PageImpl<>(fakeUserList, pageable, fakeUserList.size());
        List<UserInfoDTO> expectedUserList = List.of(fakeUserInfoDTO, fakeUserInfoDTO);

        when(userRepository.findAll(pageable)).thenReturn(fakeUserPage);
        when(userMapper.userToUserInfoDTO(any(User.class))).thenReturn(fakeUserInfoDTO);

        List<UserInfoDTO> fakeUserInfoDTOList = userService.getUsers(pageable);

        assertEquals(expectedUserList, fakeUserInfoDTOList);
        verify(userRepository, times(1)).findAll(pageable);
        verify(userMapper, times(2)).userToUserInfoDTO(any(User.class));
    }

    @Test
    void test_getUserById_shouldReturnUser() {
        Long fakeUserId = 1L;
        UserInfoDTO expectedUserInfoDTO = fakeUserInfoDTO;

        when(userRepository.findById(fakeUserId)).thenReturn(Optional.ofNullable(fakeUser));
        when(userMapper.userToUserInfoDTO(any(User.class))).thenReturn(fakeUserInfoDTO);

        UserInfoDTO fakeUserInfoDTO = userService.getUserById(fakeUserId);

        assertEquals(expectedUserInfoDTO, fakeUserInfoDTO);
        verify(userRepository, times(1)).findById(fakeUserId);
        verify(userMapper, times(1)).userToUserInfoDTO(any(User.class));
    }

    @Test
    void test_getUserById_shouldReturnUserNotFoundException() {
        Long fakeUserId = 1L;

        when(userRepository.findById(fakeUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(fakeUserId));
    }

    @Test
    void test_updateUserByIdWithoutEmail_shouldUpdateUser() {
        Long fakeUserId = 1L;
        User expectedUpdatedUser = new User(
                1L, null, "expected", "fake@fake.com", Role.ADMIN);
        UserInfoDTO fakeUserInfoDTO = new UserInfoDTO(
                null, "expected", "fake@fake.com", "ADMIN");

        when(userRepository.findById(fakeUserId)).thenReturn(Optional.ofNullable(fakeUser));

        userService.updateUserById(fakeUserId, fakeUserInfoDTO);

        assertEquals(expectedUpdatedUser, fakeUser);
        verify(userRepository, times(1)).save(fakeUser);
    }

    @Test
    void test_updateUserByIdWithEmail_shouldUpdateUser() {
        Long fakeUserId = 1L;
        String newEmail = "new@email.com";
        User expectedUpdatedUser = new User(
                1L, null, "expected", newEmail, Role.ADMIN);
        UserInfoDTO fakeUserInfoDTO = new UserInfoDTO(
                null, "expected", newEmail, "ADMIN");

        when(userRepository.findById(fakeUserId)).thenReturn(Optional.ofNullable(fakeUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());

        userService.updateUserById(fakeUserId, fakeUserInfoDTO);

        assertEquals(expectedUpdatedUser, fakeUser);
        verify(userRepository, times(1)).findByEmail(any(String.class));
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(userRepository, times(1)).save(fakeUser);
    }

    @Test
    void test_updateUserByIdWithEmail_shouldReturnUserExistsWithThisEmailException() {
        Long fakeUserId = 1L;
        String newEmail = "new@email.com";
        User expectedUpdatedUser = new User(
                1L, null, "expected", newEmail, Role.ADMIN);
        UserInfoDTO fakeUserInfoDTO = new UserInfoDTO(
                null, "expected", newEmail, "ADMIN");

        when(userRepository.findById(fakeUserId)).thenReturn(Optional.ofNullable(fakeUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.ofNullable(fakeUser));


        assertThrows(UserExistsWithThisEmailException.class,
                () -> userService.updateUserById(fakeUserId, fakeUserInfoDTO));
        verify(userRepository, times(1)).findByEmail(any(String.class));
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void test_updateUserById_shouldReturnUserNotFoundException() {
        Long fakeUserId = 1L;

        when(userRepository.findById(fakeUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUserById(fakeUserId, fakeUserInfoDTO));
    }
}