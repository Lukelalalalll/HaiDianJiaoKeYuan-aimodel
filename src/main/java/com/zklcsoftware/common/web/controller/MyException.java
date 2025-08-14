package com.zklcsoftware.common.web.controller;


public class MyException extends Exception {
    private static final long serialVersionUID = 1L;

    //无参构造方法
    public MyException(){
        super();
    }

    //有参的构造方法
    public MyException(String message){
        super(message);

    }

    // 用指定的详细信息和原因构造一个新的异常
    public MyException(String message, Throwable cause){
        super(message,cause);
    }

    //用指定原因构造一个新的异常
     public MyException(Throwable cause) {
         super(cause);
     }

}
