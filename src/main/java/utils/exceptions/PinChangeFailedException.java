package utils.exceptions;

public class PinChangeFailedException extends Exception{
    public PinChangeFailedException(String errorMessage) {
        super(errorMessage);
    }
}
