package com.sheldon.model;

public enum ResponseEnum {
    DATA_CONNECTION_OK("125", "Data Connection Already open;Transfer starting!"),
    PASV_DATA_CONN_SUCC("150","Opening binary mode data connection for LIST on port: "),
    OPTS_SUCCESS_OK("200", "OPTS UTF8 command success"),
    PORT_OK("200", "Port is ok"),
    NOOP_OK("200","NOOP command successful."),
    TYPE_SUCCESS("200","Type set to "),
    SIZE_ACCEPT("213","{}"),
    CONN_SUCCESS_WC("220", "Power by SheldonPeng"),
    TRANSFER_COMPLETE("226", "Transfer complete,Closing data connection"),
    PASV_TRANSFER_COMPLETE("226","Transfer complete for LIST"),
    PASV_SUCCESS("227","Entering Passive Mode ({})."),
    USER_LOGGED("230", "User logged"),
    DELETE_DIRECTORY_SUCCESS("250","Delete Directory success!"),
    DELETE_FILE_SUCCESS("250","Delete FILE success!"),
    CWD_SUCCESS("250", "CWD command successful"),
    RENAME_SUCCESS("250","Rename command complete!"),
    CURRENT_FILE("257", "\"{}\" is current directory."),
    FILE_CREATE_SUCCESS("257","File Create Success!"),
    USER_NEED_PASSWORD("331", "User name ok,need password"),
    USER_NOT_IDENTIFY("332", "Need user name"),
    RNFR_SUCCESS("350","Requested file action pending further information."),
    INVALID_COMMAND("501", "Invalid command of parameters"),
    UNIDENTIFY_CONNECTION("521", "Connection not exists,close 2 second later"),
    PASS_INCORRECT("530", "Error,The username or password is incorrect"),
    NOT_LOGIN("530", "Not Login,Please Login and then operate!"),
    SIZE_DENY("550","Access is denied."),
    FILE_ALREADY_EXISTS("550","Create File error ! File Already Exists"),
    DELETE_DIRECTORY_FAIL("550","Delete Fail! The File is not a directory"),
    DELETE_FILE_FAIL("550","Delete Fail! The File is a directory"),
    UPLOAD_FAIL("550","File System Denied!"),
    FILE_NOT_INVALID("550", "File not invalid");

    private final String code;
    private final String msg;
    ResponseEnum(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String toString(){

        StringBuilder builder = new StringBuilder();
        builder.append(code);
        builder.append(" ");
        builder.append(msg);
        return builder.toString();
    }
}
