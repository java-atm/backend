package utils.exceptions;

public class FailedToWithdrawException extends Exception{
    public FailedToWithdrawException(String errorMessage) {
        super(errorMessage);
    }
}
