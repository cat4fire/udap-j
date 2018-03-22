package org.ethereum.vm.applications;

import org.ethereum.vm.Api;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GlobalChat {
    public Api api;

    public GlobalChat(Api api) {
        this.api = api;
    }

    public void chat() {
        Object[] params = api.getParams();
        byte[] from = (byte[]) params[0];
        byte[] to = (byte[]) params[1];
        String words = (String) params[2];


        List<DataWord> topics = new ArrayList<>();
        topics.add(new DataWord("GlobalChatFrom"));
        LogInfo logInfo = new LogInfo(from, topics, from);

        List<DataWord> topics2 = new ArrayList<>();
        topics2.add(new DataWord("GlobalChatTo"));
        LogInfo logInfo2 = new LogInfo(to, topics2, to);

        List<DataWord> topics3 = new ArrayList<>();
        topics3.add(new DataWord("GlobalChatWord"));
        LogInfo logInfo3 = new LogInfo(new byte[0], topics3, words.getBytes(Charset.forName("UTF-8")));

        List<LogInfo> logInfos = new ArrayList<>();
        logInfos.add(logInfo);
        logInfos.add(logInfo2);
        logInfos.add(logInfo3);
        api.program.getResult().addLogInfos(logInfos);
    }
}
