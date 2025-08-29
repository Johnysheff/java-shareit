package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommentMapperTest {

    @Test
    void toCommentDto_whenValidComment_thenReturnDto() {
        User author = new User();
        author.setId(1L);
        author.setName("Иван");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Хорошая вещь");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertNotNull(dto);
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(comment.getAuthor().getName(), dto.getAuthorName());
        assertEquals(comment.getCreated(), dto.getCreated());
    }
}