package org.ethereum.vm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.core.CallTransaction;
import org.ethereum.vm.program.Program;
import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.subarray;

@Slf4j(topic = "api")
public class Api {

    public final ApiEnum apiEnum;

    public final Program program;

    public final CallTransaction.Function function;

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
        log.debug("api-helloworld !!!! acc : {} addr : {} data : {}", Hex.toHexString(getTargetAddress().getLast20Bytes()), addr, value);
        program.storageSave(getTargetAddress(), new DataWord(addr.longValue()), new DataWord(value.longValue()));
        program.spendGas(10l, "helloworld function");
        Object[] obj = new Object[1];
        obj[0] = BigInteger.valueOf(1234);//0x04d2

        byte[] res = setResult(obj);

        return;
    }


    public void foo() {
        return;
    }

    public void bar() {
        return;
    }

    public void miniConTractCreate() {

        Object[] params = getParams();

        return;
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

    public void setString(DataWord accout, DataWord address, String input) {
        byte[] b = input.getBytes(Charset.forName("UTF-8"));
        int len = b.length;
        program.storageSave(accout, address, new DataWord(len));

        int addr = address.intValue();
        for (int i = 0; i < len; i += 32) {
            byte[] t = ArrayUtils.subarray(b, i, i + 32);
            DataWord d = new DataWord(t);
            program.storageSave(accout, new DataWord(addr + 1 + i), d);
        }
    }

    // 0 ,1 ,2 ,..... 31, 32   all 33
    //

    public String getString(DataWord accout, DataWord address) {
        DataWord dlen = program.storageLoad(accout, address);
        int len = dlen.intValue();

        byte[] load = new byte[0];
        int addr = address.intValue();
        for (int i = 0; i < len; i += 32) {
            DataWord data = program.storageLoad(accout, new DataWord(addr + 1 + i));
            byte[] d = data.getData();
            if (len - i < 32) {
                d = ArrayUtils.subarray(d, 32 - (len - i), 32);
            }
            load = ArrayUtils.addAll(load, d);
        }
        return new String(load, Charset.forName("UTF-8"));
    }
}
