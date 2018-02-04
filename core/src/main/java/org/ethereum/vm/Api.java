package org.ethereum.vm;

import org.ethereum.core.CallTransaction;
import org.ethereum.vm.program.Program;
import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.apache.commons.lang3.ArrayUtils.subarray;

public class Api {

    public static void helloworld(Program program) {
        CallTransaction.Function fn = CallTransaction.Function.fromSignature("helloworld", new String[]{"Uint256", "Uint256"}, new String[0]);
        Object[] params = getParams(fn, program.getInvoke().getDataCopy(DataWord.ZERO, program.getInvoke().getDataSize()));
        BigInteger addr = (BigInteger) params[0];
        BigInteger value = (BigInteger) params[1];
        program.storageSave(new DataWord(addr.longValue()), new DataWord(value.longValue()));
        program.spendGas(10l, "helloworld function");
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

    public static Object[] getParams(CallTransaction.Function fn, byte[] callData) {
        if (callData.length < 4) {
            return null;
        }
        //  if(SHA3(subarray(callData, 0, 4)))//todo check function's hash
        return fn.decode(callData);
    }
}
