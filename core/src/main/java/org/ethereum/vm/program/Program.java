/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.program;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.core.Transaction;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.vm.*;
import org.ethereum.vm.program.invoke.ProgramInvoke;
import org.ethereum.vm.program.invoke.ProgramInvokeFactory;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.ethereum.vm.program.listener.CompositeProgramListener;
import org.ethereum.vm.program.listener.ProgramListenerAware;
import org.ethereum.vm.program.listener.ProgramStorageChangeListener;
import org.ethereum.vm.trace.ProgramTraceListener;
import org.ethereum.vm.trace.ProgramTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

import static java.lang.StrictMath.min;
import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.*;
import static org.ethereum.util.BIUtil.*;

/**
 * @author Roman Mandeleil
 * @since 01.06.2014
 */
public class Program {

    private static final Logger logger = LoggerFactory.getLogger("VM");

    /**
     * This attribute defines the number of recursive calls allowed in the EVM
     * Note: For the JVM to reach this level without a StackOverflow exception,
     * ethereumj may need to be started with a JVM argument to increase
     * the stack size. For example: -Xss10m
     */
    /*private static final int MAX_DEPTH = 1024;*/

    //Max size for stack checks
    /*private static final int MAX_STACKSIZE = 1024;*/

    private Transaction transaction;

    private ProgramInvoke invoke;
    private ProgramInvokeFactory programInvokeFactory = new ProgramInvokeFactoryImpl();

    /*private ProgramOutListener listener;*/
    private ProgramTraceListener traceListener;
    private ProgramStorageChangeListener storageDiffListener = new ProgramStorageChangeListener();
    private CompositeProgramListener programListener = new CompositeProgramListener();

    private Storage storage;
    /*private byte[] returnDataBuffer;*/

    private ProgramResult result = new ProgramResult();
    /*private ProgramPrecompile programPrecompile;*/

    CommonConfig commonConfig = CommonConfig.getDefault();

    private final SystemProperties config;

    private final BlockchainConfig blockchainConfig;

    public Program(ProgramInvoke programInvoke, Transaction transaction, SystemProperties config) {
        this.config = config;
        this.invoke = programInvoke;
        this.transaction = transaction;
        traceListener = new ProgramTraceListener(config.vmTrace());
        this.storage = setupProgramListener(new Storage(programInvoke));
        this.blockchainConfig = config.getBlockchainConfig().getConfigForBlock(programInvoke.getNumber().longValue());
    }


    public Program withCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        return this;
    }

    public int getCallDeep() {
        return invoke.getCallDeep();
    }

    /*private InternalTransaction addInternalTx(byte[] nonce, DataWord gasLimit, byte[] senderAddress, byte[] receiveAddress,
                                              BigInteger value, byte[] data, String note) {

        InternalTransaction result = null;
        if (transaction != null) {
            byte[] senderNonce = isEmpty(nonce) ? getStorage().getNonce(senderAddress).toByteArray() : nonce;

            data = config.recordInternalTransactionsData() ? data : null;
            result = getResult().addInternalTransaction(transaction.getHash(), getCallDeep(), senderNonce,
                    getGasPrice(), gasLimit, senderAddress, receiveAddress, value.toByteArray(), data, note);
        }
        return result;
    }*/

    private <T extends ProgramListenerAware> T setupProgramListener(T programListenerAware) {
        if (programListener.isEmpty()) {
            programListener.addListener(traceListener);
            programListener.addListener(storageDiffListener);
        }

        programListenerAware.setProgramListener(programListener);

        return programListenerAware;
    }

    public Map<DataWord, DataWord> getStorageDiff() {
        return storageDiffListener.getDiff();
    }

    public void suicide(DataWord obtainerAddress) {

        byte[] owner = getOwnerAddress().getLast20Bytes();
        byte[] obtainer = obtainerAddress.getLast20Bytes();
        BigInteger balance = getStorage().getBalance(owner);

        if (logger.isInfoEnabled())
            logger.info("Transfer to: [{}] heritage: [{}]",
                    Hex.toHexString(obtainer),
                    balance);

        //addInternalTx(null, null, owner, obtainer, balance, null, "suicide");

        if (FastByteComparisons.compareTo(owner, 0, 20, obtainer, 0, 20) == 0) {
            // if owner == obtainer just zeroing account according to Yellow Paper
            getStorage().addBalance(owner, balance.negate());
        } else {
            transfer(getStorage(), owner, obtainer, balance);
        }

        getResult().addDeleteAccount(this.getOwnerAddress());
    }

    public Repository getStorage() {
        return this.storage;
    }

    public void spendGas(long gasValue, String cause) {
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] Spent for cause: [{}], gas: [{}]", invoke.hashCode(), cause, gasValue);
        }

        if (getGasLong() < gasValue) {
            throw Program.Exception.notEnoughSpendingGas(cause, gasValue, this);
        }
        getResult().spendGas(gasValue);
    }

    public void spendAllGas() {
        spendGas(getGas().longValue(), "Spending all remaining");
    }

    public void refundGas(long gasValue, String cause) {
        logger.info("[{}] Refund for cause: [{}], gas: [{}]", invoke.hashCode(), cause, gasValue);
        getResult().refundGas(gasValue);
    }

    public void futureRefundGas(long gasValue) {
        logger.info("Future refund added: [{}]", gasValue);
        getResult().addFutureRefund(gasValue);
    }

    public void resetFutureRefund() {
        getResult().resetFutureRefund();
    }

    public void storageSave(DataWord word1, DataWord word2) {
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val) {
        DataWord keyWord = new DataWord(key);
        DataWord valWord = new DataWord(val);
        getStorage().addStorageRow(getOwnerAddress().getLast20Bytes(), keyWord, valWord);
    }

    public void createAccount(byte[] addr, BigInteger accountType) {
        getStorage().createAccount(addr, accountType);
        return;
    }

    public DataWord getOwnerAddress() {
        return invoke.getOwnerAddress().clone();
    }

    public DataWord getBlockHash(int index) {
        return index < this.getNumber().longValue() && index >= Math.max(256, this.getNumber().intValue()) - 256 ?
                new DataWord(this.invoke.getBlockStore().getBlockHashByNumber(index, getPrevHash().getData())).clone() :
                DataWord.ZERO.clone();
    }

    public DataWord getBalance(DataWord address) {
        BigInteger balance = getStorage().getBalance(address.getLast20Bytes());
        return new DataWord(balance.toByteArray());
    }

    public DataWord getOriginAddress() {
        return invoke.getOriginAddress().clone();
    }

    public DataWord getCallerAddress() {
        return invoke.getCallerAddress().clone();
    }

    public DataWord getGasPrice() {
        return invoke.getMinGasPrice().clone();
    }

    public long getGasLong() {
        return invoke.getGasLong() - getResult().getGasUsed();
    }

    public DataWord getGas() {
        return new DataWord(invoke.getGasLong() - getResult().getGasUsed());
    }

    public DataWord getCallValue() {
        return invoke.getCallValue().clone();
    }

    public DataWord getDataSize() {
        return invoke.getDataSize().clone();
    }

    public DataWord getDataValue(DataWord index) {
        return invoke.getDataValue(index);
    }

    public byte[] getDataCopy(DataWord offset, DataWord length) {
        return invoke.getDataCopy(offset, length);
    }

    /*public DataWord getReturnDataBufferSize() {
        return new DataWord(getReturnDataBufferSizeI());
    }

    private int getReturnDataBufferSizeI() {
        return returnDataBuffer == null ? 0 : returnDataBuffer.length;
    }

    public byte[] getReturnDataBufferData(DataWord off, DataWord size) {
        if ((long) off.intValueSafe() + size.intValueSafe() > getReturnDataBufferSizeI()) return null;
        return returnDataBuffer == null ? new byte[0] :
                Arrays.copyOfRange(returnDataBuffer, off.intValueSafe(), off.intValueSafe() + size.intValueSafe());
    }*/

    public DataWord storageLoad(DataWord key) {
        DataWord ret = getStorage().getStorageValue(getOwnerAddress().getLast20Bytes(), key.clone());
        return ret == null ? null : ret.clone();
    }

    public DataWord getPrevHash() {
        return invoke.getPrevHash().clone();
    }

    public DataWord getCoinbase() {
        return invoke.getCoinbase().clone();
    }

    public DataWord getTimestamp() {
        return invoke.getTimestamp().clone();
    }

    public DataWord getNumber() {
        return invoke.getNumber().clone();
    }

    public BlockchainConfig getBlockchainConfig() {
        return blockchainConfig;
    }

    public DataWord getDifficulty() {
        return invoke.getDifficulty().clone();
    }

    public DataWord getGasLimit() {
        return invoke.getGaslimit().clone();
    }

    public boolean isStaticCall() {
        return invoke.isStaticCall();
    }

    public ProgramResult getResult() {
        return result;
    }

    public void setRuntimeFailure(RuntimeException e) {
        getResult().setException(e);
    }


    /*public void addListener(ProgramOutListener listener) {
        this.listener = listener;
    }*/

    public boolean byTestingSuite() {
        return invoke.byTestingSuite();
    }

    /*public interface ProgramOutListener {
        void output(String out);
    }*/

    /**
     * Denotes problem when executing Ethereum bytecode.
     * From blockchain and peer perspective this is quite normal situation
     * and doesn't mean exceptional situation in terms of the program execution
     */
    @SuppressWarnings("serial")
    public static class ExecutionException extends RuntimeException {
        public ExecutionException(String message) {
            super(message);
        }
    }

    @SuppressWarnings("serial")
    public static class OutOfGasException extends ExecutionException {

        public OutOfGasException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class IllegalOperationException extends ExecutionException {

        public IllegalOperationException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class ReturnDataCopyIllegalBoundsException extends ExecutionException {
        public ReturnDataCopyIllegalBoundsException(DataWord off, DataWord size, long returnDataSize) {
            super(String.format("Illegal RETURNDATACOPY arguments: offset (%s) + size (%s) > RETURNDATASIZE (%d)", off, size, returnDataSize));
        }
    }

    @SuppressWarnings("serial")
    public static class StaticCallModificationException extends ExecutionException {
        public StaticCallModificationException() {
            super("Attempt to call a state modifying opcode inside STATICCALL");
        }
    }


    public static class Exception {

        public static OutOfGasException notEnoughOpGas(OpCode op, long opGas, long programGas) {
            return new OutOfGasException("Not enough gas for '%s' operation executing: opGas[%d], programGas[%d];", op, opGas, programGas);
        }

        public static OutOfGasException notEnoughOpGas(OpCode op, DataWord opGas, DataWord programGas) {
            return notEnoughOpGas(op, opGas.longValue(), programGas.longValue());
        }

        public static OutOfGasException notEnoughOpGas(OpCode op, BigInteger opGas, BigInteger programGas) {
            return notEnoughOpGas(op, opGas.longValue(), programGas.longValue());
        }

        public static OutOfGasException notEnoughSpendingGas(String cause, long gasValue, Program program) {
            return new OutOfGasException("Not enough gas for '%s' cause spending: invokeGas[%d], gas[%d], usedGas[%d];",
                    cause, program.invoke.getGas().longValue(), gasValue, program.getResult().getGasUsed());
        }

        public static OutOfGasException gasOverflow(BigInteger actualGas, BigInteger gasLimit) {
            return new OutOfGasException("Gas value overflow: actualGas[%d], gasLimit[%d];", actualGas.longValue(), gasLimit.longValue());
        }

        public static IllegalOperationException invalidOpCode(byte... opCode) {
            return new IllegalOperationException("Invalid operation code: opCode[%s];", Hex.toHexString(opCode, 0, 1));
        }
    }


    public ProgramInvoke getInvoke() {
        return invoke;
    }

    public void setHReturn(byte[] hReturn) {
        this.result.setHReturn(hReturn);

    }

    public void getHReturn() {
        this.result.getHReturn();

    }

    public void run() {
        ApiEnum.determin(this).delegate(this);
    }

}
