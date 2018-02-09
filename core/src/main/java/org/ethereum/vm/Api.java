package org.ethereum.vm;

import org.ethereum.core.CallTransaction;
import org.ethereum.vm.program.Program;
import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.subarray;

public class Api {

    private final ApiEnum apiEnum;

    private final Program program;

    private final CallTransaction.Function function;

    private Api(ApiEnum apiEnum, Program program) {
        this.apiEnum = apiEnum;
        this.program = program;
        this.function = apiEnum.getFunction();
    }

    public static Api create(Program program) {
        ApiEnum apiEnum = ApiEnum.determin(program);
        return new Api(apiEnum, program);
    }

    public ApiEnum getApiEnum() {
        return apiEnum;
    }

    public void run() {
        if (apiEnum == null) {
            program.setRuntimeFailure(new RuntimeException("functionhash doesn't exist"));
        }
        apiEnum.delegate(this);
    }

    public void helloworld() {
        //CallTransaction.Function fn = CallTransaction.Function.fromSignature("helloworld", new String[]{"uint256", "uint256"}, new String[]{"uint256","uint256"});
        Object[] params = getParams();
        BigInteger addr = (BigInteger) params[0];
        BigInteger value = (BigInteger) params[1];
        DataWord load = program.storageLoad(getTargetAddress(), new DataWord(addr.longValue()));
        program.storageSave(getTargetAddress(), new DataWord(addr.longValue()), new DataWord(value.longValue()));
        program.spendGas(10l, "helloworld function");
        Object[] obj = new Object[1];
        obj[0] = BigInteger.valueOf(1234);//0x04d2

        byte[] res = setResult(obj);

        return;
    }

    ;

    public void foo() {
        return;
    }

    ;

    public void bar() {
        return;
    }

    ;

    public static void main(String[] args) {
        CallTransaction.Function fn = CallTransaction.Function.fromSignature("helloworld", new String[]{"uint256", "uint256"}, new String[]{"uint256"});
        System.out.println(Hex.toHexString(fn.encodeSignature()));

        System.out.println(Hex.toHexString(ApiEnum.HELLOWORLD.getFunction().encodeSignature()));

        Object[] obj = new Object[2];
        obj[0] = BigInteger.valueOf(1223l);
        obj[1] = BigInteger.valueOf(333l);
        byte[] ret = fn.encodeResult(obj);
        System.out.println(ret);
    }

    public Object[] getParams() {
        byte[] callData = program.getInvoke().getDataCopy(DataWord.ZERO, program.getInvoke().getDataSize());
        return function.decodeParam(callData);
    }

    public DataWord getTargetAddress() {
        return program.getInvoke().getOwnerAddress();
    }

    public byte[] setResult(Object[] returns) {
        byte[] ret = function.encodeResult(returns);
        program.setHReturn(ret);
        return ret;
    }
}
