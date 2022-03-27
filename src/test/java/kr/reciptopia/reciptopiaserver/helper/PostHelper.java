package kr.reciptopia.reciptopiaserver.helper;

import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Post;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;

public class PostHelper {

	private static final String ARBITRARY_TITLE = "가문어 오징어 튀김 만들기";
	private static final String ARBITRARY_CONTENT = "가문어 오징어 다리를 잘라서 튀긴다.";
	private static final String ARBITRARY_PICTURE_URL_1 = "C:\\Users\\eunsung\\Desktop\\temp\\picture";
	private static final String ARBITRARY_PICTURE_URL_2 = "C:\\Users\\tellang\\Desktop\\temp\\picture";

	// 임의의 지정된 필드 값으로 채워진 Post 반환
	public static Post aPost() {
		Post post = Post.builder()
				.title(ARBITRARY_TITLE)
				.content(ARBITRARY_CONTENT)
				.pictureUrl(ARBITRARY_PICTURE_URL_1)
				.pictureUrl(ARBITRARY_PICTURE_URL_2)
				.build();
		post.setId(0L);
		post.setOwner(anAccount());
//		post.setRecipe(anRecipe());
		return post;
	}

	public static Update aPostUpdateDto() {
		return Update.builder()
				.title(ARBITRARY_TITLE)
				.content(ARBITRARY_CONTENT)
				.pictureUrl(ARBITRARY_PICTURE_URL_1)
				.pictureUrl(ARBITRARY_PICTURE_URL_2)
				.build();
	}

}
