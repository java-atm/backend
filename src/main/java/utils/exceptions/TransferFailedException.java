package utils.exceptions;

public class TransferFailedException extends Exception{
    public TransferFailedException(String errorMessage) {
        super(errorMessage);
    }
}
