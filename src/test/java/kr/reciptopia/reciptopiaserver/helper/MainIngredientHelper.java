package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Update;

import kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;

public class MainIngredientHelper {

    public static MainIngredient aMainIngredient() {
        MainIngredient entitiy = MainIngredient.builder()
            .build();
        entitiy.setId(0L);
        return entitiy;
    }

    public static Create aMainIngredientCreateDto() {
        return Create.builder()
            .build();
    }

    public static Update aMainIngredientUpdateDto() {
        return Update.builder()
            .build();
    }

    public static Result aMainIngredientResultDto() {
        return Result.builder()
            .build();
    }

    public interface Bulk {

        static MainIngredientDto.Bulk.Create aMainIngredientCreateDto() {
            return MainIngredientDto.Bulk.Create.builder()
                .build();
        }

        static MainIngredientDto.Bulk.Update aMainIngredientUpdateDto() {
            return MainIngredientDto.Bulk.Update.builder()
                .build();
        }

        static MainIngredientDto.Bulk.Result aMainIngredientResultDto() {
            return MainIngredientDto.Bulk.Result.builder()
                .build();
        }
    }
}
