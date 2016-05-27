package org.mbi.nussinovrna;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mbi.nussinovrna.exceptions.ViennaFormatException;
import org.mbi.nussinovrna.rna.RnaSecondaryStruct;

import java.util.Stack;
import java.util.function.IntPredicate;


/* This is a "custom" version of a Vienna format converter. It makes heavy use of
   several assumptions based on a way in which secondary structure is being generated
   in this project!
 */

/* TODO: Make this converter more generic */
/* TODO: Refactor */
public class ViennaConverter {

    @ToString
    @RequiredArgsConstructor
    private enum BRACKET {
        LEFT("("),
        RIGHT(")");

        public static BRACKET fromString(final String bracketString) {
            if(bracketString.equals("(")) {
                return BRACKET.LEFT;
            } else if(bracketString.equals(")")) {
                return BRACKET.RIGHT;
            } else {
                return null;
            }
        }

        private final String value;
    }

    public static String toViennaFormat(@NonNull final RnaSecondaryStruct rnaSecondaryStruct) {
        final StringBuilder stringBuilder = new StringBuilder(
                StringUtils.repeat('.', rnaSecondaryStruct.getRnaSequence().getLength())
        );

        rnaSecondaryStruct.getSecondaryStructMap().entrySet().stream()
                .forEach(integerIntegerEntry -> {
                    stringBuilder.setCharAt(integerIntegerEntry.getKey(), '(');
                    stringBuilder.setCharAt(integerIntegerEntry.getValue(), ')');
                });

        return stringBuilder.toString();
    }

    /* This method also performs basic correctness validation of a generated Vienna format */
    public static String toViennaFormatWithValidation(@NonNull final RnaSecondaryStruct rnaSecondaryStruct) {
        final String viennaFormattedStructure = toViennaFormat(rnaSecondaryStruct);

        final Stack<BRACKET> bracketStack = new Stack<>();

        viennaFormattedStructure.chars()
                .filter(bracketCharacter)
                .mapToObj(operand -> (char)operand)
                .map(String::valueOf)
                .map(BRACKET::fromString)
                .forEach(bracket -> {

                    if(bracket == BRACKET.LEFT) {
                        bracketStack.push(bracket);
                    } else {
                        if(bracketStack.empty() || bracketStack.pop() != BRACKET.LEFT) {
                            throw new ViennaFormatException("Invalid secondary structure format. Brackets mismatch.");
                        }
                    }
                });

        if(!bracketStack.empty()) {
            throw new ViennaFormatException("Invalid secondary structure format. Brackets mismatch.");
        }

        return viennaFormattedStructure;
    }

    private static IntPredicate bracketCharacter = character -> character == '(' || character == ')';

}
