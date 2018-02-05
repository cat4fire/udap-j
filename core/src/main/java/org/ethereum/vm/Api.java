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
        program.setHReturn()
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
        CallTransaction.Function fn = CallTransaction.Function.fromSignature("helloworld", new String[]{"uint256", "uint256"}, new String[0]);
        Object[] params = fn.decode(Hex.decode("683dd91100000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001"));
        byte[] sig = fn.encodeSignature();
        System.out.println(params);
    }

    ;

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

    public static byte[] getParams(CallTransaction.Function fn,) {
        if (callData.length < 4 || !Arrays.equals(subarray(callData, 0, 4), fn.encodeSignature())) {
            return null;
        }
        Object[] params = fn.decode(callData);
        if (params.length != fn.inputs.length) {
            return null;
        }
        return params;
    }
}
