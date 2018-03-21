package org.ethereum.vm.applications;

import lombok.extern.slf4j.Slf4j;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.vm.Api;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MiniContract {
    public Api api;

    public MiniContract(Api api) {
        this.api = api;
    }

    /*
    storage map of MiniContract

    key 0 Title String length
    key 1~n<400 TitleString, this title is plain message.

    key 400 party A address

    key 500 party B address
    key 501 party B confirmation

    key 601 contract abolished

    key 1000 Content String length
    Key 1000~n Content String, this content is long rich message.

    all chat messages stored in log

     */

    public void miniContractCreate() {
        //api.program.transaction.getSender() should be our api-server
        byte[] ramdon = RLP.encodeList(api.program.transaction.getSender(), api.program.getStorage().getNonce(api.program.transaction.getSender()).toByteArray());
        byte[] account = HashUtil.sha3omit12(ramdon);
        api.program.getStorage().increaseNonce(api.program.transaction.getSender());

        //log.debug("account : " + Hex.toHexString(account));

        Object[] params = api.getParams();
        String content = (String) params[0];
        api.setString(new DataWord(account), new DataWord(1000), content);
        byte[] partyA = (byte[]) params[1];
        byte[] partyB = (byte[]) params[2];
        api.program.storageSave(new DataWord(account), new DataWord(400), new DataWord(partyA));
        api.program.storageSave(new DataWord(account), new DataWord(500), new DataWord(partyB));
        api.program.storageSave(new DataWord(account), new DataWord(501), new DataWord(0));
        api.program.storageSave(new DataWord(account), new DataWord(601), new DataWord(0));
        String title = (String) params[3];
        api.setString(new DataWord(account), new DataWord(0), title);
        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("miniContractCreate".getBytes(Charset.forName("UTF-8"))));
        LogInfo logInfo = new LogInfo(account, topics, account);
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

        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("miniContractModify"));
        LogInfo logInfo = new LogInfo(account, topics, account);
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

        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("miniContractConfirm"));
        LogInfo logInfo = new LogInfo(account, topics, account);
        api.program.getResult().addLogInfo(logInfo);
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

        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("miniContractAbolish"));
        LogInfo logInfo = new LogInfo(account, topics, account);
        api.program.getResult().addLogInfo(logInfo);
        return;
    }

    public void miniContractGetAll() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];

        byte[] partyA = api.program.storageLoad(new DataWord(account), new DataWord(400)).getLast20Bytes();
        byte[] partyB = api.program.storageLoad(new DataWord(account), new DataWord(500)).getLast20Bytes();
        long confirmation = api.program.storageLoad(new DataWord(account), new DataWord(501)).longValue();
        long abolished = api.program.storageLoad(new DataWord(account), new DataWord(601)).longValue();
        String title = api.getString(new DataWord(account), new DataWord(0));
        String content = api.getString(new DataWord(account), new DataWord(1000));
        api.setResult(new Object[]{content, partyA, partyB, title, confirmation, abolished});
        return;
    }

    /*
    upload message to key-value server first and send message uuid to here
     */
    public void miniContractChat() {

    }
}
