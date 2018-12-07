/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 The Waykichain Core developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 */

package com.waykichain.wallet

import com.waykichain.wallet.base.WaykiNetworkType
import com.waykichain.wallet.base.WaykiRegId
import com.waykichain.wallet.base.types.encodeInOldWay
import com.waykichain.wallet.impl.LegacyWallet
import org.bitcoinj.core.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import org.slf4j.LoggerFactory
import com.google.common.base.Joiner
import com.waykichain.wallet.base.params.*
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import org.json.JSONArray
import org.json.JSONObject
import org.waykichain.wallet.util.BIP44Util
import org.waykichain.wallet.util.MnemonicUtil


/**
 * @Author: Richard Chen
 * @Date 2018/08/29 下午6:27
 */

class TestWallet {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun  generateMnemonic(){
        var words= MnemonicUtil.randomMnemonicCodes()
        System.out.print(words.toString())
    }
    @Test
    fun importMnemonic(){
        var wordList="parrot swing wear box black grow dwarf custom country wave avocado actress"
        //"lounge enable orphan hire mule hunt physical gym else soft ladder crystal"
        var words=wordList.split(" ")
        MnemonicUtil.validateMnemonics(words)
        val seed = DeterministicSeed(words, null, "", 0L)
        val keyChain = DeterministicKeyChain.builder().seed(seed).build()
        val networkParameters = WaykiTestNetParams.instance
        val mainKey = keyChain.getKeyByPath(BIP44Util.generatePath(BIP44Util.WAYKICHAIN_WALLET_PATH + "/0/0"), true)
        val address = LegacyAddress.fromPubKeyHash(networkParameters, mainKey.pubKeyHash).toString()
        val ecKey=ECKey.fromPrivate(mainKey.privKey)
        System.out.print("address   "+address+"\n"+"privatekey   "+ecKey.getPrivateKeyAsWiF(networkParameters))
    }

    @Test
    fun testGenerateKeyMainNet() {
        val wallet = LegacyWallet()
        val walletAddress = wallet.generateWalletAddress(WaykiNetworkType.MAIN_NET)
        val privKey = walletAddress.privKey
        val address = walletAddress.address
        System.out.print("\n\n$privKey \n$address\n\n")
        /**
         * PhKmEa3M6BJERHdStG7nApRwURDnN3W48rhrnnM1fVKbLs3jaYd6
         * WZ9gVk4sgBuW9oJVtsE2gos5aLXK7rEEwC
         */
    }

    @Test
    fun testGenerateKeyTestNet() {
        val wallet = LegacyWallet()
        val walletAddress = wallet.generateWalletAddress(WaykiNetworkType.TEST_NET)
        val privKey = walletAddress.privKey
        val address = walletAddress.address
        System.out.print("\n\n$privKey \n$address\n\n")
    }

    @Test
    fun testImportPrivKey() {
        val params = WaykiTestNetParams.instance
        val privKeyWiF = "Y9HSQ4GGLUMhUYALoLQTZY42hUGW7dRBsmWy2TWWESZfqDAqKdCs"//"YAHcraeGRDpvwBWVccV7NLGAU6uK39nNUTip8srbJSu6HKSTfDcC"
        val address = "wbCG5rXEbEHQaw1FD9pbK1iUsBobxrbiJM"

        val key = DumpedPrivateKey.fromBase58(params, privKeyWiF).key

        val privKeyWiF2 = key.getPrivateKeyAsWiF(WaykiTestNetParams.instance)
        val address2 = LegacyAddress.fromPubKeyHash(params, key.pubKeyHash).toString()
        System.out.println("$privKeyWiF \n$privKeyWiF2\n$address\n$address2\n\n")

        val legacyAddress = LegacyAddress.fromBase58(WaykiTestNetParams.instance, address)
        System.out.println(Utils.HEX.encode(legacyAddress.hash))
        System.out.println(Utils.HEX.encode(key.pubKeyHash))
    }

    @Test
    fun testGenerateRegisterAccountTxForTestNet() {
        val wallet = LegacyWallet()
        val netParams = WaykiTestNetParams.instance
//        val privKeyWiF = "YAHcraeGRDpvwBWVccV7NLGAU6uK39nNUTip8srbJSu6HKSTfDcC"
//        val privKeyWiF = "YBqQKuQQMBeiTUTMoP5ySPzbWpNUDZpRCCCgvS2LnKbF5jzKwg4p"
        val privKeyWiF =  "Y9HSQ4GGLUMhUYALoLQTZY42hUGW7dRBsmWy2TWWESZfqDAqKdCs"//"Y9XMqNzseQFSK32SvMDNF9J7xz1CQmHRsmY1hMYiqZyTck8pYae3"
        val key = DumpedPrivateKey.fromBase58(netParams, privKeyWiF).key
        System.out.println("            ${key.publicKeyAsHex}")

        val txParams = WaykiRegisterAccountTxParams(key.pubKey, null, 429821, 10000)
        txParams.signTx(key)
        val tx = wallet.createRegisterTransactionRaw(txParams)
        System.out.println(tx)

    }

    @Test
    fun testGenerateRegisterAccountTxForMainNet() {
        val wallet = LegacyWallet()
        val netParams = WaykiMainNetParams.instance
        val privKeyWiF = "PhKmEa3M6BJERHdStG7nApRwURDnN3W48rhrnnM1fVKbLs3jaYd6"
        val key = DumpedPrivateKey.fromBase58(netParams, privKeyWiF).key
        System.out.println("            ${key.publicKeyAsHex}")

        val txParams = WaykiRegisterAccountTxParams(key.pubKey, null, 926112+100, 10000)
        txParams.signTx(key)
        val tx = wallet.createRegisterTransactionRaw(txParams)
        System.out.println(tx)

    }

    @Test
    fun testGenerateCommonTxForTestNet() {
        val wallet = LegacyWallet()
        val netParams = WaykiTestNetParams.instance

        val srcPrivKeyWiF = "Y9HSQ4GGLUMhUYALoLQTZY42hUGW7dRBsmWy2TWWESZfqDAqKdCs"//"Y9XMqNzseQFSK32SvMDNF9J7xz1CQmHRsmY1hMYiqZyTck8pYae3"
        val srcKey = DumpedPrivateKey.fromBase58(netParams, srcPrivKeyWiF).key
        val srcAddress = LegacyAddress.fromPubKeyHash(netParams, srcKey.pubKeyHash).toString()

        val destPrivKeyWif = "YB1ims24GnRCdrB8TJsiDrxos4S5bNS58qetjyFWhSDyxT9phCEa"
        val destKey = DumpedPrivateKey.fromBase58(netParams, destPrivKeyWif).key
        val destAddr = "whGSn4tTPSaEkgzHhUfXrdcwfriuPBKt3R"//LegacyAddress.fromPubKeyHash(netParams, destKey.pubKeyHash).toString()
        System.out.println("Send 1 wicc from: $srcAddress to: $destAddr")

        val txParams = WaykiCommonTxParams(WaykiNetworkType.TEST_NET, 429637, 100660, 100000000,"423318-1",destAddr )//"30947-1", destAddr)
        txParams.signTx(srcKey)
        val tx = wallet.createCommonTransactionRaw(txParams)
        System.out.println(tx)
    }

    @Test
    fun testGenerateCommonTxForMainNet() {
        val wallet = LegacyWallet()
        val netParams = WaykiMainNetParams.instance

        val srcPrivKeyWiF = "PhKmEa3M6BJERHdStG7nApRwURDnN3W48rhrnnM1fVKbLs3jaYd6"
        val srcKey = DumpedPrivateKey.fromBase58(netParams, srcPrivKeyWiF).key

        val destAddr = "WQRwCMmQGy2XvpATTai6AtGhrRrdXDQzQh"
        val txParams = WaykiCommonTxParams(WaykiNetworkType.MAIN_NET, 926165, 10000, 10000, "926152-1", destAddr)
        txParams.signTx(srcKey)
        val tx = wallet.createCommonTransactionRaw(txParams)
        System.out.println(tx)
    }

//    @Test
//    fun testHDWallet() {
//        val params = MainNetParams.get()
//        val wallet = Wallet(params)
//        val seedHex = wallet.keyChainSeed.toHexString()
//        val root = HDNode.fromSeedHex(seedHex)
//        println("Seed words are: " + Joiner.on(" ").join(seed.mnemonicCode!!))
//        println("Seed birthday is: " + seed.creationTimeSeconds)
//    }

    @Test
    fun testSnippet() {
        val ss = ByteArrayOutputStream()
//        ss.write(VarInt(10000).encode())
//        ss.write(VarInt(10000).encodeInOldWay())

        //regData: regHeight, regIndex
        val regId = WaykiRegId(30947,1)
        ss.write(VarInt(4).encodeInOldWay())
        ss.write(VarInt(regId.regHeight).encodeInOldWay())
        ss.write(VarInt(regId.regIndex).encodeInOldWay())

//        ss.write(10000.toBigInteger().toByteArray())
        val bytes = ss.toByteArray()
        val hexStr =  Utils.HEX.encode(bytes)
        System.out.println(hexStr)
    }

}
