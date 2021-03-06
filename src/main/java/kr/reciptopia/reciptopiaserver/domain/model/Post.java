package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
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
public class Post extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @NotNull
    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id")
    private Account owner;

    @ElementCollection
    private List<String> pictureUrls = new ArrayList<>();

    @NotEmpty
    private String title;

    private String content;

    @NotNull
    private Long views;

    @Builder
    public Post(Account owner, @Singular List<String> pictureUrls, String title, String content) {
        setOwner(owner);
        this.pictureUrls = pictureUrls;
        this.title = title;
        this.content = content;
        this.views = 0L;
    }

    public Post withPictureUrl(String pictureUrl) {
        return Post.builder()
            .title(title)
            .content(content)
            .pictureUrls(pictureUrls)
            .pictureUrl(pictureUrl)
            .build()
            .withId(id)
            .withOwner(owner)
            .withViews(views);
    }

    public void addViews() {
        this.views++;
    }
}
