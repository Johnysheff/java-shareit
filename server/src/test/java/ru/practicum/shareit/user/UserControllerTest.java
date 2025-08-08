package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getUser_whenUserExists_thenReturnUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("user@email.ru");

        when(userService.getById(anyLong())).thenReturn(UserMapper.toUser(userDto));

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnUserList() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("user@email.ru");

        when(userService.getAll()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User"));
    }

    @Test
    void createUser_whenValidUser_thenReturnCreatedUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@email.ru");

        when(userService.create(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void updateUser_whenValidUpdates_thenReturnUpdatedUser() throws Exception {
        UserDto updates = new UserDto();
        updates.setName("Updated");

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setName("Updated");
        updatedUser.setEmail("user@email.ru");

        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteUser_whenUserExists_thenReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_whenNoUsersExist_thenReturnEmptyList() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateUser_whenPartialFieldsProvided_thenReturnUpdatedUser() throws Exception {
        UserDto updatedDto = new UserDto();
        updatedDto.setName("UpdatedName");

        UserDto responseDto = new UserDto();
        responseDto.setId(1L);
        responseDto.setName("UpdatedName");
        responseDto.setEmail("user@example.com");

        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"));
    }

    @Test
    void createUser_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("invalid-email");

        when(userService.create(any()))
                .thenThrow(new ValidationException("Некорректный email"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }
}