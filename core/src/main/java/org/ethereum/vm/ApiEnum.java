package org.ethereum.vm;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.vm.program.Program;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.Comparator;

public enum ApiEnum {

    FOO("foo", Hex.decode("12345671")),
    BAR("bar", Hex.decode("12345672")),
    HELLOWORLD("bar", Hex.decode("12345673")),;

    private String name;
    private byte[] code;

    ApiEnum(String name, byte[] code) {
        this.name = name;
        this.code = code;
    }

    public static ApiEnum determin(Program program) {
        byte[] functionHash = null;
        if (program == null)
            return null;
        else {
            functionHash = program.getDataCopy(new DataWord(0), new DataWord(4));
        }
        if (ArrayUtils.isNotEmpty(functionHash)) {
            for (ApiEnum e : ApiEnum.values()) {
                if (Arrays.equals(functionHash, e.code)) {
                    return e;
                }
            }
        }
        return null;
    }

    public void delegate(Program program) {
        if (Arrays.equals(FOO.code, this.code)) {
            Api.foo(program);
        }
        if (Arrays.equals(BAR.code, this.code)) {
            Api.bar(program);
        }
        if (Arrays.equals(HELLOWORLD.code, this.code)) {
            Api.helloworld(program);
        }
    }
}
