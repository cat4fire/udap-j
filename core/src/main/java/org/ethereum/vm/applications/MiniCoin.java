package org.ethereum.vm.applications;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.vm.Api;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MiniCoin {

    public Api api;

    public MiniCoin(Api api) {
        this.api = api;
    }

    /*
    storage map of MiniContract

    key 0~10^10-1 address of owner

    key 10^10 Title String length
    key 10^10+1~10^10+n  n<400  Title String, this title is plain  message.

    key 10^10 + 500 address of issuer
    key 10^10 + 501 logo bytes32 hash
    key 10^10 + 502 coin id

    key 10^10 + 600 issue fixed or elastic  0 = elastic  n = fixed
    key 10^10 + 601 total mint
    key 10^10 + 602 transfer-able 1 = able

    key 10^10 + 700 pointer to next mint coin

    key 10^10+1000 Content String length
    key 10^10+1000+1~10^10+1000+n    Content String, this Content is rich message.

    //coin id :  serial number
    0~10^10   :  0~10^10

     */


    /*
    "string", //content
    "string",//title
    "bytes32",//logo
    "bool",//fixed
    "uint256",//fixed munber
    "bool",//transferable
     */

    public void miniCoinCreate() {
        byte[] ramdon = RLP.encodeList(api.program.transaction.getSender(), new Long(System.currentTimeMillis()).toString().getBytes());
        byte[] account = HashUtil.sha3omit12(ramdon);

        Object[] params = api.getParams();
        String content = (String) params[0];
        api.setString(new DataWord(account), new DataWord(10000000000l + 1000), content);
        String title = (String) params[1];
        api.setString(new DataWord(account), new DataWord(10000000000l), title);
        byte[] logo = (byte[]) params[2];
        api.program.storageSave(new DataWord(account), new DataWord(10000000000l + 501), new DataWord(logo));
        long fixed = (long) params[3];
        api.program.storageSave(new DataWord(account), new DataWord(10000000000l + 600), new DataWord(fixed));
        long transferable = (long) params[4];
        api.program.storageSave(new DataWord(account), new DataWord(10000000000l + 602), new DataWord(transferable));
        byte[] issuer = (byte[]) params[5];
        api.program.storageSave(new DataWord(account), new DataWord(10000000000l + 500), new DataWord(issuer));

        api.program.storageSave(new DataWord(account), new DataWord(10000000000l + 601), new DataWord(0l));

        Random r = new Random(System.currentTimeMillis());
        long i = r.nextInt();//2^32 is less than 10^10 but currently it's enough
        api.program.storageSave(new DataWord(account), new DataWord(10000000000l + 502), new DataWord(i));

        api.program.storageSave(new DataWord(account), new DataWord(10000000000l + 700), new DataWord(0l));

        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("miniCoinCreate"));
        LogInfo logInfo = new LogInfo(account, topics, account);
        List<DataWord> topics2 = new ArrayList<>();
        topics2.add(new DataWord("miniCoinCreateCoinId"));
        LogInfo logInfo2 = new LogInfo(account, topics2, BigInteger.valueOf(i).toByteArray());
        List<LogInfo> logInfos = new ArrayList<>();
        logInfos.add(logInfo);
        logInfos.add(logInfo2);
        api.program.getResult().addLogInfos(logInfos);
        return;
    }

    public void miniCoinMint() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];
        byte[] receiver = (byte[]) params[1];
        long amount = (long) params[2];
        long pointer = api.program.storageLoad(new DataWord(account), new DataWord(10000000000l + 700)).longValue();
        List<LogInfo> logInfos = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            api.program.storageSave(new DataWord(account), new DataWord(0 + pointer + i), new DataWord(receiver));
            List<DataWord> l = new ArrayList<>();
            l.add(new DataWord("miniCoinMint"));
            logInfos.add(new LogInfo(account, l, new DataWord(0 + pointer + i).getData()));
        }
        api.program.getResult().addLogInfos(logInfos);
        return;
    }

    public void miniCoinTransfer() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];
        long serial = (long) params[1];
        byte[] receiver = (byte[]) params[2];

        DataWord previousOwner = api.program.storageLoad(new DataWord(account), new DataWord(0 + serial));
        if (previousOwner == null) {
            api.program.result.setRevert();
            return;
        }
        api.program.storageSave(new DataWord(account), new DataWord(0 + serial), new DataWord(receiver));

        List<LogInfo> logInfos = new ArrayList<>();
        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("miniCoinTransferSerial"));
        logInfos.add(new LogInfo(account, topics, new DataWord(0 + serial).getData()));

        List<DataWord> topics2 = new ArrayList<>();
        topics2.add(new DataWord("miniCoinTransferFrom"));
        logInfos.add(new LogInfo(account, topics2, previousOwner.getData()));

        List<DataWord> topics3 = new ArrayList<>();
        topics3.add(new DataWord("miniCoinTransferTo"));
        logInfos.add(new LogInfo(account, topics3, new DataWord(receiver).getData()));

        api.program.getResult().addLogInfos(logInfos);
        return;
    }

    public void miniCoinSearch() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];
        long serial = (long) params[1];
        DataWord owner = api.program.storageLoad(new DataWord(account), new DataWord(0 + serial));
        if (owner == null) {
            owner = new DataWord((byte[]) null);
        }
        api.setResult(new Object[]{owner.getLast20Bytes()});
        return;
    }

}
