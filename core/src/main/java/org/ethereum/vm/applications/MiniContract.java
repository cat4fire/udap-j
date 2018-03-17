package org.ethereum.vm.applications;

import org.ethereum.crypto.HashUtil;
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

    key 0 ContentString length
    Key 1~n ContentString

    key 100 Picture length
    key 101 ~ 10n byte32 of Picture address

    key 200 voice length
    key 201 ~ 10n byte32 of voice address

    key 300 video length
    key 301 ~ 10n byte32 of video address

    key 400 party A address
    key 401 party A confirmation

    key 500 party A address
    key 501 party A confirmation

    key 600 contract signed
    key 601 contract abolished

    all chat messages stored in log

     */

    /*
     MiniConTractCreate("MiniContractCreate", CallTransaction.Function.fromSignature(
            "MiniContractCreate",
            new String[]{"string", //content
                    "bytes32[]",//pictures
                    "bytes32[]",//voice
                    "bytes32[]",//video
                    "address",//party A
                    "address"//party B
            },
     */
    public void miniContractCreate() {
        byte[] account = HashUtil.sha3omit12(api.program.transaction.getSender());

        Object[] params = api.getParams();
        String content = (String) params[0];
        Object[] pictures = (Object[]) params[1];

        api.program.storageSave(new DataWord(account), new DataWord(100), new DataWord(pictures.length));
        for (int i = 0; i < pictures.length; i++) {
            byte[] picture = (byte[]) pictures[i];
            api.program.storageSave(new DataWord(account), new DataWord(101 + i), new DataWord(picture));
        }
        List<DataWord> dataWords = new ArrayList<>();
        dataWords.add(new DataWord("miniContractCreate"));
        LogInfo logInfo = new LogInfo(account, dataWords, new byte[0]);
        api.program.getResult().addLogInfo(logInfo);
        return;
    }

    /*
    reset all data and clear confirm status
     */
    public void miniContractModify() {
        Object[] params = api.getParams();
        byte[] account = (byte[]) params[0];

    }

    /*
    confirm and if both are confirmed, finishes
     */
    public void miniContractConfirm() {

    }

    /*
    upload message to key-value server first and send message uuid to here
     */
    public void miniContractChat() {

    }
}
