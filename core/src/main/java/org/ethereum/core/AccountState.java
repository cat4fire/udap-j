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
package org.ethereum.core;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;

import org.spongycastle.util.encoders.Hex;

import java.io.Externalizable;
import java.math.BigInteger;
import java.util.Arrays;

import static org.ethereum.crypto.HashUtil.*;
import static org.ethereum.util.FastByteComparisons.equal;

public class AccountState {

    private byte[] rlpEncoded;

    /* A value equal to the number of transactions sent
     * from this address, or, in the case of contract accounts,
     * the number of contract-creations made by this account */
    private final BigInteger nonce;

    /* A scalar value equal to the number of Wei owned by this address */
    private final BigInteger balance;

    /* A 256-bit hash of the root node of a trie structure
     * that encodes the storage contents of the contract,
     * itself a simple mapping between byte arrays of size 32.
     * The hash is formally denoted σ[a] s .
     *
     * Since I typically wish to refer not to the trie’s root hash
     * but to the underlying set of key/value pairs stored within,
     * I define a convenient equivalence TRIE (σ[a] s ) ≡ σ[a] s .
     * It shall be understood that σ[a] s is not a ‘physical’ member
     * of the account and does not contribute to its later serialisation */
    private final byte[] stateRoot;

    /* The hash of the EVM code of this contract—this is the code
     * that gets executed should this address receive a message call;
     * it is immutable and thus, unlike all other fields, cannot be changed
     * after construction. All such code fragments are contained in
     * the state database under their corresponding hashes for later
     * retrieval */
    /*private final byte[] codeHash;*/


    /**
     * External = 1
     * AssetProto = 2
     * Asset = 3
     */
    private final BigInteger accountType;

    private static BigInteger ACCOUNT_TYPE_EXTERNAL = BigInteger.valueOf(1);
    private static BigInteger ACCOUNT_TYPE_ASSET_PROTO = BigInteger.valueOf(2);
    private static BigInteger ACCOUNT_TYPE_ASSET = BigInteger.valueOf(3);

    public AccountState(SystemProperties config) {
        this(config.getBlockchainConfig().getCommonConstants().getInitialNonce(), BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance) {
        this(nonce, balance, EMPTY_TRIE_HASH/*, EMPTY_DATA_HASH*/, ACCOUNT_TYPE_EXTERNAL);
    }

    public AccountState(BigInteger nonce, BigInteger balance, byte[] stateRoot/*, byte[] codeHash*/) {
        this.nonce = nonce;
        this.balance = balance;
        this.stateRoot = stateRoot == EMPTY_TRIE_HASH || equal(stateRoot, EMPTY_TRIE_HASH) ? EMPTY_TRIE_HASH : stateRoot;
        //this.codeHash = codeHash == EMPTY_DATA_HASH || equal(codeHash, EMPTY_DATA_HASH) ? EMPTY_DATA_HASH : codeHash;
        this.accountType = ACCOUNT_TYPE_EXTERNAL;
    }

    public AccountState(BigInteger nonce, BigInteger balance, byte[] stateRoot/*, byte[] codeHash*/, BigInteger accountType) {
        this.nonce = nonce;
        this.balance = balance;
        this.stateRoot = stateRoot == EMPTY_TRIE_HASH || equal(stateRoot, EMPTY_TRIE_HASH) ? EMPTY_TRIE_HASH : stateRoot;
        //this.codeHash = codeHash == EMPTY_DATA_HASH || equal(codeHash, EMPTY_DATA_HASH) ? EMPTY_DATA_HASH : codeHash;
        this.accountType = accountType;
    }

    public AccountState(byte[] rlpData) {
        this.rlpEncoded = rlpData;

        RLPList items = (RLPList) RLP.decode2(rlpEncoded).get(0);
        this.nonce = items.get(0).getRLPData() == null ? BigInteger.ZERO
                : new BigInteger(1, items.get(0).getRLPData());
        this.balance = items.get(1).getRLPData() == null ? BigInteger.ZERO
                : new BigInteger(1, items.get(1).getRLPData());
        this.stateRoot = items.get(2).getRLPData();
        //this.codeHash = items.get(3).getRLPData();
        this.accountType = items.get(3).getRLPData() == null ? ACCOUNT_TYPE_EXTERNAL : new BigInteger(1, items.get(3).getRLPData());
    }


    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] nonce = RLP.encodeBigInteger(this.nonce);
            byte[] balance = RLP.encodeBigInteger(this.balance);
            byte[] stateRoot = RLP.encodeElement(this.stateRoot);
            //byte[] codeHash = RLP.encodeElement(this.codeHash);
            byte[] accountType = RLP.encodeBigInteger(this.accountType);
            this.rlpEncoded = RLP.encodeList(nonce, balance, stateRoot/*, codeHash*/, accountType);
        }
        return rlpEncoded;
    }


    public BigInteger getNonce() {
        return nonce;
    }

    public AccountState withNonce(BigInteger nonce) {
        return new AccountState(nonce, balance, stateRoot/*, codeHash*/);
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public AccountState withStateRoot(byte[] stateRoot) {
        return new AccountState(nonce, balance, stateRoot/*, codeHash*/);
    }

    public AccountState withIncrementedNonce() {
        return new AccountState(nonce.add(BigInteger.ONE), balance, stateRoot/*, codeHash*/);
    }

    /*public byte[] getCodeHash() {
        return codeHash;
    }*/

    public AccountState withCodeHash(byte[] codeHash) {
        return new AccountState(nonce, balance, stateRoot/*, codeHash*/);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public AccountState withBalanceIncrement(BigInteger value) {
        return new AccountState(nonce, balance.add(value), stateRoot/*, codeHash*/);
    }

    public boolean isContractExist(BlockchainConfig blockchainConfig) {
        return /*!FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH) ||*/
                !blockchainConfig.getConstants().getInitialNonce().equals(nonce);
    }

    public boolean isEmpty() {
        return /*FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH) &&*/
                BigInteger.ZERO.equals(balance) &&
                BigInteger.ZERO.equals(nonce);
    }

    public BigInteger getAccountType() {
        return accountType;
    }


    public String toString() {
        String ret = "  Nonce: " + this.getNonce().toString() + "\n" +
                "  Balance: " + getBalance() + "\n" +
                "  State Root: " + Hex.toHexString(this.getStateRoot()) + "\n" /*+
                "  Code Hash: " + Hex.toHexString(this.getCodeHash())*/;
        return ret;
    }
}
