package kr.reciptopia.reciptopiaserver.domain.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.error.exception.BoardNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.CommentNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.FavoriteNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.HistoryNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.LikeTagNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.ReplyNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Account extends TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @NotEmpty
    @Size(min = 8, max = 16, message = "password는 8 ~ 16자 이여야 합니다!")
    private String password;

    @NotBlank
    @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
    private String nickname;

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Board> boards = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reply> replies = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LikeTag> likeTags = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Favorite> favorites = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<History> histories = new HashSet<>();

    private String profilePictureUrl;

    @Builder
    public Account(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }


    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void addBorad(Board board) {
        boards.add(board);
        if (!this.equals(board.getOwner())) {
            board.setOwner(this);
        }
    }

    public void removeBoard(Board board) {
        if (!boards.contains(board))
            throw new BoardNotFoundException();

        boards.remove(board);
        if (this.equals(board.getOwner())) {
            board.setOwner(null);
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        if (!this.equals(comment.getOwner())) {
            comment.setOwner(this);
        }
    }

    public void removeComment(Comment comment) {
        if (!comments.contains(comment))
            throw new CommentNotFoundException();

        comments.remove(comment);
        if (this.equals(comment.getOwner())) {
            comment.setOwner(null);
        }
    }

    public void addReply(Reply reply) {
        replies.add(reply);
        if (!this.equals(reply.getOwner())) {
            reply.setOwner(this);
        }
    }

    public void removeReply(Reply reply) {
        if (!replies.contains(reply))
            throw new ReplyNotFoundException();

        replies.remove(reply);
        if (this.equals(reply.getOwner())) {
            reply.setOwner(null);
        }
    }

    public void addLikeTag(LikeTag likeTag) {
        likeTags.add(likeTag);
        if (!this.equals(likeTag.getOwner())) {
            likeTag.setOwner(this);
        }
    }

    public void removeLikeTag(LikeTag likeTag) {
        if (!likeTags.contains(likeTag))
            throw new LikeTagNotFoundException();

        likeTags.remove(likeTag);
        if (this.equals(likeTag.getOwner())) {
            likeTag.setOwner(null);
        }
    }

    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
    }

    public void removeFavorite(Favorite favorite) {
        if (!favorites.contains(favorite))
            throw new FavoriteNotFoundException();

        favorites.remove(favorite);
    }

    public void addHistory(History history) {
        Account historyOwner = history.getOwner();
        if (historyOwner != null && !historyOwner.equals(this))
            throw new AlreadyRegisteredHistoryException();
        histories.add(history);
        if (!this.equals(historyOwner)) {
            history.setOwner(this);
        }
    }

    public void removeHistory(History history) {
        if (!histories.contains(history))
            throw new HistoryNotFoundException();

        histories.remove(history);
    }
}
