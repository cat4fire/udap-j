package org.ethereum.vm;

import org.ethereum.core.CallTransaction;
import org.ethereum.vm.program.Program;
import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.subarray;

public class Api {

    public static void helloworld(Program program) {
        CallTransaction.Function fn = CallTransaction.Function.fromSignature("helloworld", new String[]{"uint256", "uint256"}, new String[]{"uint256"});
        Object[] params = getParams(fn, program.getInvoke().getDataCopy(DataWord.ZERO, program.getInvoke().getDataSize()));
        BigInteger addr = (BigInteger) params[0];
        BigInteger value = (BigInteger) params[1];
        program.storageSave(new DataWord(addr.longValue()), new DataWord(value.longValue()));
        program.spendGas(10l, "helloworld function");
        Object[] obj = new Object[1];
        obj[0] = BigInteger.valueOf(1223l);
        byte[] res = setResult(fn, obj);
        program.setHReturn(res);
        return;
    }

    ;

    public static void foo(Program program) {
        return;
    }

    ;

    public static void bar(Program program) {
        return;
    }

    ;

    public static void main(String[] args) {
        CallTransaction.Function fn = CallTransaction.Function.fromSignature("helloworld", new String[]{"uint256", "uint256"}, new String[]{"uint256"});
        Object[] params = fn.decode(Hex.decode("683dd91100000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001"));
        byte[] sig = fn.encodeSignature();
        System.out.println(params);
        Object[] obj = new Object[2];
        obj[0] = BigInteger.valueOf(1223l);
        obj[1] = BigInteger.valueOf(333l);
        byte[] res = setResult(fn, obj);
        System.out.println(res);
    }

    public static Object[] getParams(CallTransaction.Function fn, byte[] callData) {
        if (callData.length < 4 || !Arrays.equals(subarray(callData, 0, 4), fn.encodeSignature())) {
            return null;
        }
        Object[] params = fn.decode(callData);
        if (params.length != fn.inputs.length) {
            return null;
        }
        return params;
    }

    public static byte[] setResult(CallTransaction.Function fn, Object[] returns) {
        byte[] ret = fn.encodeResult(returns);
        return ret;
    }
}
