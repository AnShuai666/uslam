package com.ubtrobot.uslam.utils;

import com.ubtrobot.uslam.net.HttpRet;

public class Result {
    public String cammand;
    public int returnCode;
    public String errMsg;

    public static Result makeOkResult() {
        Result ret = new Result();
        ret.returnCode = 200;
        return ret;
    }

   public static Result makeResult(int code, String msg) {
       Result result = new Result();
       result.returnCode = code;
       result.errMsg = msg;
       return result;
   }

   public static Result makeGloableErrResult(String errMsg) {
       Result result = new Result();
       result.returnCode = HttpRet.NET_ERROR;
       result.errMsg = errMsg;
       return result;
   }

    public boolean isSuccess() { return returnCode == HttpRet.SUCCESS; }

    public boolean isFinished(){
        return  returnCode == HttpRet.MISSION_FINISHED;
    }

    public boolean isWorking(){
        return  returnCode == HttpRet.MISSION_IS_DOING;
    }

    @Override
    public String toString() {
        return String.format("return code = %d, msg = %s", returnCode, errMsg);
    }
}
