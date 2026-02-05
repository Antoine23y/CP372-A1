public class BoardError extends Exception {

    private final String errorType;

    public BoardError(String errorType, String errorMessage){
        super(errorMessage);
        this.errorType = errorType;
    }
    public String getErrorType(){
        return errorType;
    }

    public String errorResponse() {
        return "ERROR " + errorType + " " + getErrorType();
    }

}