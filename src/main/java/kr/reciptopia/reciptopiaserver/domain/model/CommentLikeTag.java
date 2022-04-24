package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@Entity
public class CommentLikeTag extends LikeTag {

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "comment_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private Comment comment;

    @Builder
    public CommentLikeTag(Account owner, Comment comment) {
        super(owner);
        setComment(comment);
    }

    public CommentLikeTag withId(Long id) {
        CommentLikeTag commentLikeTag = CommentLikeTag.builder()
            .owner(owner)
            .comment(comment)
            .build();
        commentLikeTag.setId(id);
        return this.id != null && this.id.equals(id) ? this : commentLikeTag;
    }

    public CommentLikeTag withOwner(Account owner) {
        return this.owner != null && this.owner.equals(owner) ? this :
            CommentLikeTag.builder()
                .owner(owner)
                .comment(comment)
                .build()
                .withId(id);
    }

    public CommentLikeTag withComment(Comment comment) {
        return this.comment != null && this.comment.equals(comment) ? this :
            CommentLikeTag.builder()
                .owner(owner)
                .comment(comment)
                .build()
                .withId(id);
    }
}
