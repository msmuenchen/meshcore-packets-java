package de.afa_amateurfunk.meshcore_packets.crypto;

import org.bouncycastle.crypto.Digest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper class for Ed25519 cryptography from BouncyCastle
 * We need to use a private-limited method as MC uses extended-hash private key format
 * Will be removed if upstream issue <a href="https://github.com/bcgit/bc-java/issues/2302">2302</a> succeeds
 */
public class Ed25519 extends org.bouncycastle.math.ec.rfc8032.Ed25519 {
    protected static Method implSignMethod;
    protected static Method createDigestMethod;
    protected static Method scalarMultBaseEncodedMethod;
    protected static Method pruneScalarMethod;

    static {
        //private static void implSign(Digest d, byte[] h, byte[] s, byte[] pk, int pkOff, byte[] ctx, byte phflag, byte[] m,
        //        int mOff, int mLen, byte[] sig, int sigOff)
        try {
            //load the private method
            implSignMethod = org.bouncycastle.math.ec.rfc8032.Ed25519.class.getDeclaredMethod(
                    "implSign",
                    Digest.class, //d
                    byte[].class, //h
                    byte[].class, //s
                    byte[].class, //pk
                    int.class, //pkOff
                    byte[].class, //ctx
                    byte.class, //phflag
                    byte[].class, //m
                    int.class, //mOff
                    int.class, //mLen
                    byte[].class, //sig
                    int.class //sigOff
            );
            implSignMethod.setAccessible(true);
            createDigestMethod = org.bouncycastle.math.ec.rfc8032.Ed25519.class.getDeclaredMethod(
                    "createDigest"
            );
            createDigestMethod.setAccessible(true);
            scalarMultBaseEncodedMethod = org.bouncycastle.math.ec.rfc8032.Ed25519.class.getDeclaredMethod(
                    "scalarMultBaseEncoded",
                    byte[].class, //k
                    byte[].class, //r
                    int.class //rOff
            );
            scalarMultBaseEncodedMethod.setAccessible(true);
            pruneScalarMethod = org.bouncycastle.math.ec.rfc8032.Ed25519.class.getDeclaredMethod(
                    "pruneScalar",
                    byte[].class, //n
                    int.class, //nOff
                    byte[].class //r
            );
            pruneScalarMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sign a message using orlp-ed25519 style private keys
     * <p>
     * Applications using orlp-ed25519 style private keys (like MeshCore) do not store their seed after deriving the
     * private/public keypair and instead store it in a || RH format.
     * </p>
     *
     * @param signature  buffer to write the signature to, 64 bytes {@link org.bouncycastle.math.ec.rfc8032.Ed25519.SIGNATURE_SIZE}
     * @param message    message buffer
     * @param publicKey  orlp-style public key (32 bytes) {@link org.bouncycastle.math.ec.rfc8032.Ed25519.PUBLIC_KEY_SIZE}
     * @param privateKey orlp-style private key (64 bytes)
     * @see <a href="https://github.com/orlp/ed25519/blob/master/src/sign.c">orlp-ed25519 implementation</a>
     * @see <a href="https://blog.mozilla.org/warner/2011/11/29/ed25519-keys/">Brian Warner on ed25519 key formats</a>
     */
    public static void sign_orlp(byte[] signature, byte[] message, byte[] publicKey, byte[] privateKey) {
        try {
            Digest d = (Digest) createDigestMethod.invoke(null);
            //h = LH || RH (corresponding to sha512(seed))
            //orlp-style private key goes further: it already has LH pruned
            byte[] h = new byte[64];
            //h gets mutated by implSign, so copy the buffer to avoid silent corruption
            System.arraycopy(privateKey, 0, h, 0, 64);

            //s = pruned scalar, 32 bytes
            //orlp-style private keys are already assumed to be pruned
            byte[] s = new byte[32];
            //copy the buffer to avoid silent corruption
            System.arraycopy(privateKey, 0, s, 0, 32);

            //orlp-style public key
            byte[] pk = publicKey;
            int pkOff = 0;

            //set to null to avoid the extraneous (compared to orlp ed25519_sign) dom2 code path
            byte[] ctx = null;
            byte phflag = 0x00;

            // message
            byte[] m = message;
            int mOff = 0;
            int mLen = message.length;

            //signature (destination) buffer
            byte[] sig = signature;
            int sigOff = 0;

            implSignMethod.invoke(null, d, h, s, pk, pkOff, ctx, phflag, m, mOff, mLen, sig, sigOff);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sign a message using orlp-ed25519 style private keys
     * <p>
     * Applications using orlp-ed25519 style private keys (like MeshCore) do not store their seed after deriving the
     * private/public keypair and instead store it in a || RH format.
     * </p>
     * <p>This overload derives the public key on demand from the private key.</p>
     *
     * @param signature  buffer to write the signature to, 64 bytes {@link org.bouncycastle.math.ec.rfc8032.Ed25519.SIGNATURE_SIZE}
     * @param message    message buffer
     * @param privateKey orlp-style private key (64 bytes)
     * @see <a href="https://github.com/orlp/ed25519/blob/master/src/sign.c">orlp-ed25519 implementation</a>
     * @see <a href="https://blog.mozilla.org/warner/2011/11/29/ed25519-keys/">Brian Warner on ed25519 key formats</a>
     * @see <a href="https://github.com/orlp/ed25519/pull/17/changes">orlp-ed25519 PR describing derivation of public key</a>
     */
    public static void sign_orlp(byte[] signature, byte[] message, byte[] privateKey) {
        try {
            // orlp private keys are a || RH, so a can be simply extracted
            byte[] a = new byte[32];
            System.arraycopy(privateKey, 0, a, 0, 32);

            // derive public key
            byte[] pk = derive_pubkey_orlp(privateKey);
            int pkOff = 0;
            scalarMultBaseEncodedMethod.invoke(null, a, pk, pkOff);

            sign_orlp(signature, message, pk, privateKey);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Derive public key from orlp-ed25519 style private keys
     * <p>
     * Applications using orlp-ed25519 style private keys (like MeshCore) do not store their seed after deriving the
     * private/public keypair and instead store it in a || RH format.
     * </p>
     *
     * @param privateKey orlp-style private key (64 bytes)
     * @return public key, byte[32]
     * @see <a href="https://blog.mozilla.org/warner/2011/11/29/ed25519-keys/">Brian Warner on ed25519 key formats</a>
     * @see <a href="https://github.com/orlp/ed25519/pull/17/changes">orlp-ed25519 PR describing derivation of public key</a>
     * @see org.bouncycastle.math.ec.rfc8032.Ed25519#generatePublicKey(byte[], int, byte[], int) (end of function)
     */
    public static byte[] derive_pubkey_orlp(byte[] privateKey) {
        try {
            // orlp private keys are a || RH, so a can be simply extracted
            byte[] a = new byte[32];
            System.arraycopy(privateKey, 0, a, 0, 32);

            // derive public key
            // public key (A) = multiply (a)
            byte[] pk = new byte[32];
            int pkOff = 0;
            scalarMultBaseEncodedMethod.invoke(null, a, pk, pkOff);

            return pk;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * given a seed, derive its orlp-format private key
     *
     * @param seed 32 byte seed
     * @return orlp-format private key (a || RH)
     * @see org.bouncycastle.math.ec.rfc8032.Ed25519#generatePublicKey(byte[], int, byte[], int)
     */
    public static byte[] seed_to_orlp(byte[] seed) {
        try {
            Digest d = (Digest) createDigestMethod.invoke(null);

            // LH || RH = sha512(seed)
            byte[] lhrh = new byte[64];
            d.update(seed, 0, SECRET_KEY_SIZE);
            d.doFinal(lhrh, 0);

            // a = prune(LH)
            byte[] a = new byte[32];
            pruneScalarMethod.invoke(null, lhrh, 0, a);

            // orlp = a || RH
            byte[] orlp = new byte[64];
            System.arraycopy(a, 0, orlp, 0, 32);
            System.arraycopy(lhrh, 32, orlp, 32, 32);
            return orlp;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
