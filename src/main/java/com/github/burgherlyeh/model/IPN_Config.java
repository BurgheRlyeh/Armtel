package com.github.burgherlyeh.model;

public interface IPN_Config {
    boolean R_flag = false;

    default boolean isRedrawBlocked() {
        return R_flag;
    }
}