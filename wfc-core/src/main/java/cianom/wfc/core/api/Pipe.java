package cianom.wfc.core.api;

public interface Pipe<IN, OUT> {

    OUT run(IN in) throws Exception;


}
