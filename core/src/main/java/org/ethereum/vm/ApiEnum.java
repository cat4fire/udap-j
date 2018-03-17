package org.ethereum.vm;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.core.CallTransaction;
import org.ethereum.vm.applications.MiniContract;
import org.ethereum.vm.program.Program;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.Comparator;

public enum ApiEnum {

    HELLOWORLD("helloworld", CallTransaction.Function.fromSignature("helloworld", new String[]{"uint256", "uint256"}, new String[]{"uint256"})),
    FOO("foo", CallTransaction.Function.fromSignature("foo", new String[]{"uint256", "uint256"}, new String[]{"uint256"})),
    BAR("bar", CallTransaction.Function.fromSignature("bar", new String[]{"uint256", "uint256"}, new String[]{"uint256"})),

    MiniConTractCreate("MiniContractCreate", CallTransaction.Function.fromSignature(
            "MiniContractCreate",
            new String[]{"string", //content
                    "bytes32[]",//pictures
                    "bytes32[]",//voice
                    "bytes32[]",//video
                    "address",//party A
                    "address"//party B
            },
            new String[0])),;
    private String name;

    private CallTransaction.Function function;
    private byte[] functionHash;

    public CallTransaction.Function getFunction() {
        return function;
    }

    ApiEnum(String name, CallTransaction.Function function) {
        this.name = name;
        this.function = function;
        this.functionHash = function.encodeSignature();
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
                if (Arrays.equals(functionHash, e.functionHash)) {
                    return e;
                }
            }
        }
        return null;
    }

    public void delegate(Api api) {
        switch (api.getApiEnum()) {
            case FOO:
                api.foo();
                break;
            case BAR:
                api.bar();
                break;
            case HELLOWORLD:
                api.helloworld();
                break;
            case MiniConTractCreate:
                new MiniContract(api).miniContractCreate();
                break;
            default:
                throw new RuntimeException("unknow Api");
        }
    }
}
