package com.sab.littleh.net;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Connection {
    private final Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private checkpointSaved ConnectionState state;

    public Connection(String address, int port) throws IOException {
        state = ConnectionState.CONNECTING;
        socket = new Socket(address, port);
        socket.setTcpNoDelay(true);

        state = ConnectionState.ENCRYPTING;
        KeyPair keyPair = Encryption.generateKeyPair();
        writePublicKey(keyPair.getPublic(), socket.getOutputStream());
        try {
            Cipher rsaDecrypt = Cipher.getInstance(Encryption.RSA);
            rsaDecrypt.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            SecretKey secret = readSecretKey(socket.getInputStream(), rsaDecrypt);

            Cipher encrypt = Cipher.getInstance(Encryption.AES_CTR);
            encrypt.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(new byte[encrypt.getBlockSize()]));

            Cipher decrypt = Cipher.getInstance(Encryption.AES_CTR);
            decrypt.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(new byte[decrypt.getBlockSize()]));

            out = new DataOutputStream(new CipherOutputStream(socket.getOutputStream(), encrypt));
            in = new DataInputStream(new CipherInputStream(socket.getInputStream(), decrypt));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        state = ConnectionState.CONNECTED;
    }

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        socket.setTcpNoDelay(true);

        state = ConnectionState.ENCRYPTING;

        PublicKey key = readPublicKey(socket.getInputStream());
        try {
            Cipher rsaEncrypt = Cipher.getInstance(Encryption.RSA);
            rsaEncrypt.init(Cipher.ENCRYPT_MODE, key);

            SecretKey secret = Encryption.generateSecretKey();
            writeSecretKey(secret, socket.getOutputStream(), rsaEncrypt);

            Cipher encrypt = Cipher.getInstance(Encryption.AES_CTR);
            encrypt.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(new byte[encrypt.getBlockSize()]));

            Cipher decrypt = Cipher.getInstance(Encryption.AES_CTR);
            decrypt.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(new byte[decrypt.getBlockSize()]));

            out = new DataOutputStream(new CipherOutputStream(socket.getOutputStream(), encrypt));
            in = new DataInputStream(new CipherInputStream(socket.getInputStream(), decrypt));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        state = ConnectionState.CONNECTED;
    }

    private static void writePublicKey(PublicKey key, OutputStream out) throws IOException {
        byte[] data = key.getEncoded();
        int len = data.length;

        out.write(len);
        out.write(len >> 8);
        out.write(len >> 16);
        out.write(len >> 24);
        out.write(data);
    }

    private static PublicKey readPublicKey(InputStream in) throws IOException {
        int len = in.read();
        len |= in.read() << 8;
        len |= in.read() << 16;
        len |= in.read() << 24;

        byte[] data = in.readNBytes(len);
        try {
            return KeyFactory.getInstance(Encryption.RSA).generatePublic(new X509EncodedKeySpec(data));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeSecretKey(SecretKey key, OutputStream out, Cipher encrypt) throws IOException {
        byte[] data = key.getEncoded();
        try {
            data = encrypt.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        int len = data.length;

        out.write(len);
        out.write(len >> 8);
        out.write(len >> 16);
        out.write(len >> 24);
        out.write(data);
    }

    private static SecretKey readSecretKey(InputStream in, Cipher decrypt) throws IOException {
        int len = in.read();
        len |= in.read() << 8;
        len |= in.read() << 16;
        len |= in.read() << 24;

        byte[] data = in.readNBytes(len);
        try {
            byte[] decryptedData = decrypt.doFinal(data);
            return new SecretKeySpec(decryptedData, Encryption.AES);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }

    public ConnectionState getState() {
        return state;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void close() throws IOException {
        socket.close();
        out.close();
        in.close();
        state = ConnectionState.DISCONNECTED;
    }

    public boolean readBoolean() throws IOException {
        return in.readBoolean();
    }

    public byte readByte() throws IOException {
        return in.readByte();
    }

    public int readInt() throws IOException {
        return in.readInt();
    }

    public long readLong() throws IOException {
        return in.readLong();
    }

    public float readFloat() throws IOException {
        return in.readFloat();
    }

    public String readUTF() throws IOException {
        return in.readUTF();
    }

    public void writeBoolean(boolean b) throws IOException {
        out.writeBoolean(b);
    }

    public void writeByte(byte b) throws IOException {
        out.writeByte(b);
    }

    public void writeInt(int i) throws IOException {
        out.writeInt(i);
    }

    public void writeLong(long l) throws IOException {
        out.writeLong(l);
    }

    public void writeFloat(float f) throws IOException {
        out.writeFloat(f);
    }

    public void writeUTF(String utf) throws IOException {
        out.writeUTF(utf);
    }
}