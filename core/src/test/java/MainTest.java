import com.ethercamp.harmony.jsonrpc.EthJsonRpcImpl;
import com.ethercamp.harmony.jsonrpc.TypeConverter;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import static org.ethereum.util.ByteUtil.bigIntegerToBytes;

public class MainTest {
    public static void main(String[] args) {
        ECKey ecKey = new ECKey();
        System.out.println("lycrus " + Hex.toHexString(ecKey.getPrivKey().toByteArray()));
        System.out.println("lycrus " + Hex.toHexString(ecKey.getAddress()));
        byte[] b = Hex.decode(JSonHexToHex("0x1122334455667788990011223344556677889900112233445566778899001122"));

        ECKey.ECDSASignature signature = ecKey.sign(b);
        byte[] signatureBytes = toByteArray(signature);

        String res = TypeConverter.toJsonHex(signatureBytes);
        return;
    }

    private static String JSonHexToHex(String x) {
        if (!x.startsWith("0x"))
            throw new RuntimeException("Incorrect hex syntax");
        x = x.substring(2);
        return x;
    }

    private static byte[] toByteArray(ECKey.ECDSASignature signature) {
        final byte fixedV = signature.v >= 27
                ? (byte) (signature.v - 27)
                : signature.v;

        return ByteUtil.merge(
                bigIntegerToBytes(signature.r),
                bigIntegerToBytes(signature.s),
                new byte[]{fixedV});
    }
}
