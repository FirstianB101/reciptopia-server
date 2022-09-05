package kr.reciptopia.reciptopiaserver.helper.fieldfilter;

public class FilterHelper {

    public static Long getNonNull(Long inputValue, Long arbitraryValue) {
        return inputValue == null ? arbitraryValue : inputValue;
    }
}
