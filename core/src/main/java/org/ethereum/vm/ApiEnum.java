package org.ethereum.vm;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.core.CallTransaction;
import org.ethereum.vm.applications.MiniCoin;
import org.ethereum.vm.applications.MiniContract;
import org.ethereum.vm.program.Program;

import java.util.Arrays;

public enum ApiEnum {

    HELLOWORLD("helloworld", CallTransaction.Function.fromSignature("helloworld", new String[]{"uint256", "uint256"}, new String[]{"uint256"})),
    FOO("foo", CallTransaction.Function.fromSignature("foo", new String[]{"uint256", "uint256"}, new String[]{"uint256"})),
    BAR("bar", CallTransaction.Function.fromSignature("bar", new String[]{"uint256", "uint256"}, new String[]{"uint256"})),

    MiniContractCreate("MiniContractCreate", CallTransaction.Function.fromSignature(
            "MiniContractCreate",
            new String[]{
                    "string", //content
                    "address",//party A
                    "address",//party B
                    "string",//title
            },
            new String[0])),
    MiniContractModify("MiniContractModify", CallTransaction.Function.fromSignature(
            "MiniContractModify",
            new String[]{
                    "address",//Minicontract to modify
                    "string", //content
                    "string",//title
            },
            new String[0])),
    MiniContractConfirm("MiniContractConfirm", CallTransaction.Function.fromSignature(
            "MiniContractConfirm",
            new String[]{
                    "address",//Minicontract to confirm
            },
            new String[0])),
    MiniContractAbolish("MiniContractAbolish", CallTransaction.Function.fromSignature(
            "MiniContractAbolish",
            new String[]{
                    "address",//Minicontract to confirm
            },
            new String[0])),
    MiniContractStatus("MiniContractStatus", CallTransaction.Function.fromSignature(
            "MiniContractStatus",
            new String[]{
                    "address",//Minicontract to modify
            },
            new String[]{
                    "string", //content
                    "address",//party A
                    "address",//party B
                    "string",//title
                    "uint256",//confirmed
                    "uint256",//abolished

            })),
    MiniContractList("MiniContractList", CallTransaction.Function.fromSignature(
            "MiniContractList",
            new String[]{
                    "address",//owner of Minicontracts
            },
            new String[]{
                    "address[]",//address of Minicontracts whose party A is the given address
                    "address[]",//address of Minicontracts whose party B is the given address
            })),
    MiniContractChat("MiniContractChat", CallTransaction.Function.fromSignature(
            "MiniContractChat",
            new String[]{
                    "address",//Minicontract
                    "address",//address of talker
                    "string",//what you say
            },
            new String[0])),
    //====================================================================

    MiniCoinCreate("MiniCoinCreate", CallTransaction.Function.fromSignature(
            "MiniCoinCreate",
            new String[]{
                    "string", //content
                    "string",//title
                    "bytes32",//logo
                    "uint256",//fixed , 0 for mintable and non-0 for fixed and represents its capacity
                    "uint256",//transferable 0 for able and 1 for disable
                    "address",//issuer
                    //more decorate and background images
            },
            new String[0])),
    MiniCoinMint("MiniCoinMint", CallTransaction.Function.fromSignature(
            "MiniCoinMint",
            new String[]{
                    "address", //coin address
                    "address",//receiver
                    "uint256",//amount
            },
            new String[0])),
    MiniCoinTransfer("MiniCoinTransfer", CallTransaction.Function.fromSignature(
            "MiniCoinTransfer",
            new String[]{
                    "address", //coin address
                    "uint256",//serial number of coin
                    "address",//transfer to
            },
            new String[0])),
    MiniCoinSearch("MiniCoinSearch", CallTransaction.Function.fromSignature(
            "MiniCoinSearch",
            new String[]{
                    "address", //coin address
                    "uint256",//serial number of coin
            },
            new String[]{
                    "address",//owner
            })),
    MiniCoinStatus("MiniCoinStatus", CallTransaction.Function.fromSignature(
            "MiniCoinStatus",
            new String[]{
                    "address",//coin address
            },
            new String[]{
                    "string", //content
                    "string",//title
                    "bytes32",//logo
                    "uint256",//fixed , 0 for mintable and non-0 for fixed and represents its capacity
                    "uint256",//transferable 0 for able and 1 for disable
                    "address",//issuer
                    "uint256",//total mint
                    "uint256",//coin id
            })),
    //===========================================================
    GlobalChat("GlobalChat", CallTransaction.Function.fromSignature(
            "GlobalChat",
            new String[]{
                    "address",//coin address
            },
            new String[]{
                    "string", //content
                    "string",//title
                    "bytes32",//logo
                    "uint256",//fixed , 0 for mintable and non-0 for fixed and represents its capacity
                    "uint256",//transferable 0 for able and 1 for disable
                    "address",//issuer
                    "uint256",//total mint
                    "uint256",//coin id
            })),;

    public String name;

    public CallTransaction.Function function;
    public byte[] functionHash;

    public CallTransaction.Function getFunction() {
        return function;
    }

    ApiEnum(String name, CallTransaction.Function function) {
        this.name = name;
        this.function = function;
        this.functionHash = function.encodeSignature();
    }

    public static ApiEnum determin(Program program) {
        byte[] functionHash = null;
        if (program == null)
            return null;
        else {
            functionHash = program.getDataCopy(new DataWord(0), new DataWord(4));
        }
        if (ArrayUtils.isNotEmpty(functionHash)) {
            for (ApiEnum e : ApiEnum.values()) {
                if (Arrays.equals(functionHash, e.functionHash)) {
                    return e;
                }
            }
        }
        return null;
    }

    public void delegate(Api api) {
        switch (api.getApiEnum()) {
            case FOO:
                api.foo();
                break;
            case BAR:
                api.bar();
                break;
            case HELLOWORLD:
                api.helloworld();
                break;
            case MiniContractCreate:
                new MiniContract(api).miniContractCreate();
                break;
            case MiniContractModify:
                new MiniContract(api).miniContractModify();
                break;
            case MiniContractConfirm:
                new MiniContract(api).miniContractConfirm();
                break;
            case MiniContractAbolish:
                new MiniContract(api).miniContractAbolish();
                break;
            case MiniContractStatus:
                new MiniContract(api).miniContractStatus();
                break;
            case MiniContractList:
                new MiniContract(api).miniContractList();
                break;
            case MiniContractChat:
                new MiniContract(api).miniContractChat();
                break;
//===============================================
            case MiniCoinCreate:
                new MiniCoin(api).miniCoinCreate();
                break;
            case MiniCoinMint:
                new MiniCoin(api).miniCoinMint();
                break;
            case MiniCoinTransfer:
                new MiniCoin(api).miniCoinTransfer();
                break;
            case MiniCoinSearch:
                new MiniCoin(api).miniCoinSearch();
                break;
            case MiniCoinStatus:
                new MiniCoin(api).miniCoinStatus();
                break;


            default:
                throw new RuntimeException("unknow Api");
        }
    }
}
