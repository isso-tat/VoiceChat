package com.tatsukii.voicechat.Util

class Size(width: Int, height: Int)
{
    private var _width : Int = 0;
    private var _height : Int = 0;

    val Width: Int = _width;
    val Height: Int = _height;

    init {
        _width = width;
        _height = height;
    }
}