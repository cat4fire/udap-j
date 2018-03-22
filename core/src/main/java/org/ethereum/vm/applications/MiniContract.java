package org.ethereum.vm.applications;

import lombok.extern.slf4j.Slf4j;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.vm.Api;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MiniContract {
    public Api api;

    public MiniContract(Api api) {
        this.api = api;
    }

    public DataWord metadataPoolPartyA = new DataWord(HashUtil.sha3omit12("MetadataOfMiniContractPartyA".getBytes(Charset.forName("UTF-8"))));

    public DataWord metadataPoolPartyB = new DataWord(HashUtil.sha3omit12("MetadataOfMiniContractPartyB".getBytes(Charset.forName("UTF-8"))));
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

    /*
    storage map of Metadata of MiniContract, address is last20bytes of hash("MetadataOfMiniContractPartyA")

    key addressOfOwner  number of MiniContract

    key hash(addressOfOwner+indexOfnumberOfMiniContract) address
     */

    public void miniContractCreate() {
        //api.program.transaction.getSender() should be our api-server
        byte[] ramdon = RLP.encodeList(api.program.transaction.getSender(), api.program.getStorage().getNonce(api.program.transaction.getSender()).toByteArray());
        byte[] account = HashUtil.sha3omit12(ramdon);
        api.program.getStorage().increaseNonce(api.program.transaction.getSender());
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
        //==update metadata of MiniContract pool
        DataWord numberA = api.program.storageLoad(metadataPoolPartyA, new DataWord(partyA));
        numberA = numberA == null ? new DataWord() : numberA;
        BigInteger indexA = numberA.value();
        numberA.add(new DataWord(1l));
        api.program.storageSave(metadataPoolPartyA, new DataWord(partyA), numberA);
        api.program.storageSave(metadataPoolPartyA, new DataWord(HashUtil.sha3(partyA, indexA.toByteArray())), new DataWord(account));

        DataWord numberB = api.program.storageLoad(metadataPoolPartyB, new DataWord(partyB));
        numberB = numberB == null ? new DataWord() : numberB;
        BigInteger indexB = numberB.value();
        numberB.add(new DataWord(1l));
        api.program.storageSave(metadataPoolPartyB, new DataWord(partyB), numberB);
        api.program.storageSave(metadataPoolPartyB, new DataWord(HashUtil.sha3(partyB, indexB.toByteArray())), new DataWord(account));
        //=====================================
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
        api.program.storageSave(new DataWord(account), new DataWord(501), new DataWord(0));
        String content = (String) params[2];
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

    public void miniContractStatus() {
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

    public void miniContractList() {
        Object[] params = api.getParams();
        byte[] owner = (byte[]) params[0];

        DataWord numberA = api.program.storageLoad(metadataPoolPartyA, new DataWord(owner));
        numberA = numberA == null ? new DataWord() : numberA;
        BigInteger indexA = numberA.value();
        List<byte[]> contractAddressPartyA = new ArrayList<>();
        for (int i = 0; i < indexA.longValue(); i++) {
            byte[] contractAddress = api.program.storageLoad(metadataPoolPartyA, new DataWord(HashUtil.sha3(owner, indexA.toByteArray()))).getData();
            contractAddressPartyA.add(contractAddress);
        }

        DataWord numberB = api.program.storageLoad(metadataPoolPartyB, new DataWord(owner));
        numberB = numberB == null ? new DataWord() : numberB;
        BigInteger indexB = numberB.value();
        List<byte[]> contractAddressPartyB = new ArrayList<>();
        for (int i = 0; i < indexB.longValue(); i++) {
            byte[] contractAddress = api.program.storageLoad(metadataPoolPartyB, new DataWord(HashUtil.sha3(owner, indexB.toByteArray()))).getData();
            contractAddressPartyB.add(contractAddress);
        }

        api.setResult(new Object[]{contractAddressPartyA.toArray(), contractAddressPartyB.toArray()});
        return;
    }

    /*
    upload message to key-value server first and send message uuid to here
     */
    public void miniContractChat() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];
        byte[] talker = (byte[]) params[1];
        String words = (String) params[2];

        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("miniContractChatFrom"));
        LogInfo logInfo = new LogInfo(account, topics, talker);

        List<DataWord> topics2 = new ArrayList<>();
        topics2.add(new DataWord("miniContractChatWords"));
        LogInfo logInfo2 = new LogInfo(account, topics2, words.getBytes(Charset.forName("UTF-8")));

        List<LogInfo> logInfos = new ArrayList<>();
        logInfos.add(logInfo);
        logInfos.add(logInfo2);
        api.program.getResult().addLogInfos(logInfos);
    }
}
