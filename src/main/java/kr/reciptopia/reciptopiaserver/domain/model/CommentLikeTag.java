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
import lombok.With;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
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
        setOwner(owner);
        setComment(comment);
    }
}
