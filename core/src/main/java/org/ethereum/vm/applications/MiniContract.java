package org.ethereum.vm.applications;

import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.vm.Api;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

public class MiniContract {
    public Api api;

    public MiniContract(Api api) {
        this.api = api;
    }

    /*
    storage map of MiniContract

    key 0 TitleString length
    key 1~n<400 TitleString, this title is plain message.

    key 400 party A address

    key 500 party B address
    key 501 party B confirmation

    key 601 contract abolished

    key 1000 ContentString length
    Key 1000~n ContentString, this content is long rich message.

    all chat messages stored in log

     */

    public void miniContractCreate() {
        byte[] ramdon = RLP.encodeList(api.program.transaction.getSender(), new Long(System.currentTimeMillis()).toString().getBytes());
        byte[] account = HashUtil.sha3omit12(ramdon);

        Object[] params = api.getParams();
        String title = (String) params[0];
        api.setString(new DataWord(account), new DataWord(0), title);
        byte[] partyA = (byte[]) params[1];
        byte[] partyB = (byte[]) params[2];
        api.program.storageSave(new DataWord(account), new DataWord(400), new DataWord(partyA));
        api.program.storageSave(new DataWord(account), new DataWord(500), new DataWord(partyB));
        api.program.storageSave(new DataWord(account), new DataWord(501), new DataWord(0));
        String content = (String) params[3];
        api.setString(new DataWord(account), new DataWord(1000), content);
        List<DataWord> dataWords = new ArrayList<>();
        dataWords.add(new DataWord("miniContractCreate"));
        LogInfo logInfo = new LogInfo(account, dataWords, account);
        api.program.getResult().addLogInfo(logInfo);
        return;
    }


    public void miniContractModify() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];
        DataWord confirmD = api.program.storageLoad(new DataWord(account), new DataWord(501));
        DataWord abolishD = api.program.storageLoad(new DataWord(account), new DataWord(601));
        if (confirmD.intValue() == 1 || abolishD.intValue() == 1) {
            //confirmed already
            api.program.result.setRevert();
            return;
        }
        String title = (String) params[1];
        api.setString(new DataWord(account), new DataWord(0), title);
        byte[] partyA = (byte[]) params[2];
        byte[] partyB = (byte[]) params[3];
        api.program.storageSave(new DataWord(account), new DataWord(400), new DataWord(partyA));
        api.program.storageSave(new DataWord(account), new DataWord(500), new DataWord(partyB));
        api.program.storageSave(new DataWord(account), new DataWord(501), new DataWord(0));
        String content = (String) params[4];
        api.setString(new DataWord(account), new DataWord(1000), content);
        List<DataWord> dataWords = new ArrayList<>();
        dataWords.add(new DataWord("miniContractCreate"));
        LogInfo logInfo = new LogInfo(account, dataWords, new byte[0]);
        api.program.getResult().addLogInfo(logInfo);
        return;

    }

    public void miniContractConfirm() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];
        DataWord confirmD = api.program.storageLoad(new DataWord(account), new DataWord(501));
        DataWord abolishD = api.program.storageLoad(new DataWord(account), new DataWord(601));
        if (confirmD.intValue() == 1 || abolishD.intValue() == 1) {
            //confirmed already
            api.program.result.setRevert();
            return;
        }
        api.program.storageSave(new DataWord(account), new DataWord(501), new DataWord(1));
        return;
    }

    public void miniContractAbolish() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];
        DataWord confirmD = api.program.storageLoad(new DataWord(account), new DataWord(501));
        DataWord abolishD = api.program.storageLoad(new DataWord(account), new DataWord(601));
        if (confirmD.intValue() == 1 || abolishD.intValue() == 1) {
            //confirmed already
            api.program.result.setRevert();
            return;
        }
        api.program.storageSave(new DataWord(account), new DataWord(601), new DataWord(1));
        return;
    }

    /*
    upload message to key-value server first and send message uuid to here
     */
    public void miniContractChat() {

    }
}
