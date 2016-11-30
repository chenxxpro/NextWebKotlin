package com.github.yoojia.web.lang;

public interface Transformer<I, O> {

    O transform(I in);

}