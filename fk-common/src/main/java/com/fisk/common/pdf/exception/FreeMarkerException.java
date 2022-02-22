package com.fisk.common.pdf.exception;

public class FreeMarkerException extends BaseException {
    public FreeMarkerException(){
        super("FreeMarker异常");
    }

    public FreeMarkerException(int errorCode, String errorMsg){
        super(errorMsg);
        this.errorCode=errorCode;
        this.errorMsg=errorMsg;
    }
    public FreeMarkerException(String errorMsg){
        super(errorMsg);
        this.errorCode=500;
        this.errorMsg=errorMsg;
    }
    public FreeMarkerException(String errorMsg, Exception e){
        super(errorMsg,e);
        this.errorCode=500;
        this.errorMsg=errorMsg;
    }
}
