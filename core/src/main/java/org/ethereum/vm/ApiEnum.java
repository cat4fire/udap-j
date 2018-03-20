package org.ethereum.vm;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.core.CallTransaction;
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
                    "address",//party A
                    "address",//party B
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
    MiniContractGetAll("MiniContractGetAll", CallTransaction.Function.fromSignature(
            "MiniContractGetAll",
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
            new String[0])),;

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
            case MiniContractGetAll:
                new MiniContract(api).miniContractGetAll();
                break;
            default:
                throw new RuntimeException("unknow Api");
        }
    }
}
