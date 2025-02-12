package wins.insomnia.backyardrocketry.util.input;

public class TypeInputEvent extends InputEvent {

    private final char CHARACTER;

    public TypeInputEvent(char character) {

        CHARACTER = character;

    }

    public char getCharacter() {
        return CHARACTER;
    }

}
