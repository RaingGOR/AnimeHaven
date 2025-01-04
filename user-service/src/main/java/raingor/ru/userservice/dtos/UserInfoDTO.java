package raingor.ru.userservice.dtos;

public record UserInfoDTO(
    String avatar_url,
    String name,
    String email,
    String role
) {
}
