package com.sab.littleh.util.sab_format;

class SabToken {
    private final SabTokenType type;
    private final String value;


    /**
     * Creates a com.sab_format.SabToken object from a token type and value.
     * @param type
     * The type of the token
     * @param value
     * The value to be associated
     */
    public SabToken(SabTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Gets the token type of the token.
     * @return
     * The type of this com.sab_format.SabToken
     */
    public SabTokenType getType() {
        return type;
    }

    /**
     * Gets the string value of the token.
     * @return
     * The value of this com.sab_format.SabToken
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        switch (type) {
            case OpenParen : return "[";
            case CloseParen : return "]";
            case Ident : return String.format("%s: ", value);
            case Val : return value == null ? "null" : String.format("\"%s\"", value);
        }
        return null;
    }
}
