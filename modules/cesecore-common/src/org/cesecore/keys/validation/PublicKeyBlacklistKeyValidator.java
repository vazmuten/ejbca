/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General                  *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.cesecore.keys.validation;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.util.CertTools;

/**
 * Public key blacklist key validator using the Bouncy Castle BCRSAPublicKey implementation 
 * (see org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey). 
 * 
 * The key validator is used to filter out weak debian keys or other blacklisted public keys 
 * generated by 'openssl', 'ssh-keygen', or 'openvpn --keygen', see {@link https://wiki.debian.org/SSLkeys#Identifying_Weak_Keys}
 * 
 * @version $Id: BlacklistKeyValidator.java 25263 2017-03-01 12:12:00Z anjakobs $
 */
public class PublicKeyBlacklistKeyValidator extends BaseKeyValidator implements PublicKeyBlacklistConsumer {

    private static final long serialVersionUID = 215729318959311916L;

    /** Public key fingerprint digest algorithm. */
    public static final String DIGEST_ALGORITHM = "SHA-256";

    /** List separator. */
    private static final String LIST_SEPARATOR = ";";

    /** Class logger. */
    private static final Logger log = Logger.getLogger(PublicKeyBlacklistKeyValidator.class);

    public static final float LATEST_VERSION = 1F;

    /** The key validator type. */
    public static final int KEY_VALIDATOR_TYPE = 3;

    /** View template in /ca/editkeyvalidators. */
    protected static final String TEMPLATE_FILE = "editBlacklistKeyValidator.xhtml";

    protected static final String KEY_GENERATOR_SOURCES = "keyGeneratorSources";

    protected static final String KEY_ALGORITHMS = "keyAlgorithms";

    /** Reference to the session bean in EJBCA packages. */
    protected PublicKeyBlacklistProducer blacklistProducer;

    /**
     * Creates a new instance.
     */
    public PublicKeyBlacklistKeyValidator() {
        init();
    }

    /**
     * Creates a new instance with the same attributes as the given one.
     * @param keyValidator the base key validator to load.
     */
    public PublicKeyBlacklistKeyValidator(final BaseKeyValidator keyValidator) {
        super(keyValidator);
    }

    @Override
    public void setBlacklistProducer(PublicKeyBlacklistProducer producer) {
        blacklistProducer = producer;
    }

    @Override
    public PublicKeyBlacklistProducer getBlacklistProducer() {
        return blacklistProducer;
    }

    @Override
    public Integer getKeyValidatorType() {
        return KEY_VALIDATOR_TYPE;
    }

    /**
     * Initializes uninitialized data fields.
     */
    public void init() {
        super.init();
        if (null == data.get(TYPE)) {
            data.put(TYPE, Integer.valueOf(KEY_VALIDATOR_TYPE));
        }
        if (data.get(KEY_GENERATOR_SOURCES) == null) {
            setKeyGeneratorSources(new ArrayList<String>()); // KeyGeneratorSources.sourcesAsString()
        }
        if (data.get(KEY_ALGORITHMS) == null) {
            setKeyAlgorithms(new ArrayList<String>());
        }
    }

    /** Gets a list of key generation sources.
     * @return a list of key generation source indexes.
     */
    public List<Integer> getKeyGeneratorSources() {
        final String value = (String) data.get(KEY_GENERATOR_SOURCES);
        final List<Integer> result = new ArrayList<Integer>();
        if (StringUtils.isNotBlank(value)) {
            final String[] tokens = value.trim().split(LIST_SEPARATOR);
            for (int i = 0, j = tokens.length; i < j; i++) {
                result.add(Integer.valueOf(tokens[i]));
            }
        }
        return result;
    }

    /** Sets key generation source indexes.
     * 
     * @param indexes list of key generation source indexes.
     */
    public void setKeyGeneratorSources(List<String> indexes) {
        final StringBuilder builder = new StringBuilder();
        for (String index : indexes) {
            if (builder.length() == 0) {
                builder.append(index);
            } else {
                builder.append(LIST_SEPARATOR).append(index);
            }
        }
        data.put(KEY_GENERATOR_SOURCES, builder.toString());
    }

    /** Gets a list of key algorithms.
     * @return a list.
     */
    public List<String> getKeyAlgorithms() {
        final String value = (String) data.get(KEY_ALGORITHMS);
        final List<String> result = new ArrayList<String>();
        final String[] tokens = value.trim().split(LIST_SEPARATOR);
        for (int i = 0, j = tokens.length; i < j; i++) {
            result.add(tokens[i]);
        }
        return result;
    }

    /** Sets the key algorithms.
     * 
     * @param algorithms list of key algorithms.
     */
    public void setKeyAlgorithms(List<String> algorithms) {
        final StringBuilder builder = new StringBuilder();
        for (String index : algorithms) {
            if (builder.length() == 0) {
                builder.append(index);
            } else {
                builder.append(LIST_SEPARATOR).append(index);
            }
        }
        data.put(KEY_ALGORITHMS, builder.toString());
    }

    @Override
    public String getTemplateFile() {
        return TEMPLATE_FILE;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object clone() throws CloneNotSupportedException {
        final PublicKeyBlacklistKeyValidator result = new PublicKeyBlacklistKeyValidator();
        final HashMap map = (HashMap) result.saveData();
        final Iterator iterator = (data.keySet()).iterator();
        Object key;
        while (iterator.hasNext()) {
            key = iterator.next();
            map.put(key, data.get(key));
        }
        result.loadData(map);
        return result;
    }

    @Override
    public float getLatestVersion() {
        return LATEST_VERSION;
    }

    @Override
    public void upgrade() {
        super.upgrade();
        if (log.isTraceEnabled()) {
            log.trace(">upgrade: " + getLatestVersion() + ", " + getVersion());
        }
        if (Float.compare(LATEST_VERSION, getVersion()) != 0) {
            // New version of the class, upgrade.
            log.info(intres.getLocalizedMessage("blacklistkeyvalidator.upgrade", new Float(getVersion())));
            init();
        }
    }

    @Override
    public void before() {
        if (log.isDebugEnabled()) {
            log.debug("BlacklistKeyValidator before.");
            // Initialize used objects here.
        }
    }

    @Override
    public boolean validate(final PublicKey publicKey) throws KeyValidationException, Exception {
        super.validate(publicKey);
        final int keyLength = KeyTools.getKeyLength(publicKey);
        final String keyAlgorithm = publicKey.getAlgorithm(); // AlgorithmTools.getKeyAlgorithm(publicKey);
        if (log.isDebugEnabled()) {
            log.debug("Validating public key with algorithm " + keyAlgorithm + ", length " + keyLength + ", format " + publicKey.getFormat()
                    + ", implementation " + publicKey.getClass().getName() + " against public key blacklist.");
        }
        final String fingerprint = CertTools.createPublicKeyFingerprint(publicKey, DIGEST_ALGORITHM);
        log.info("Matching public key with fingerprint " + fingerprint + " with public key blacklist.");
        final PublicKeyBlacklist entry = blacklistProducer.findByFingerprint(fingerprint);
        boolean keyGeneratorSourceMatched = false;
        boolean keySpecMatched = false;

        if (null != entry) {
            // Filter for key generator sources.
            if (getKeyGeneratorSources().contains(new Integer(-1)) || getKeyGeneratorSources().contains(Integer.valueOf(entry.getSource()))) {
                keyGeneratorSourceMatched = true;
            }
            // Filter for key specifications.
            if (getKeyAlgorithms().contains("-1") || getKeyAlgorithms().contains(getKeySpec(publicKey))) {
                keySpecMatched = true;
            }
        }
        if (keyGeneratorSourceMatched && keySpecMatched) {
            final String message = "Public key with id " + entry.getPublicKeyBlacklistId() + " and fingerprint " + fingerprint
                    + " found in public key blacklist.";
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            messages.add("Invalid: " + message);
        }

        if (log.isTraceEnabled()) {
            for (String message : getMessages()) {
                log.trace(message);
            }
        }
        return getMessages().size() == 0;
    }

    @Override
    public void after() {
        if (log.isDebugEnabled()) {
            log.debug("BlacklistKeyValidator after.");
            // Finalize used objects here.
        }
    }

    private final String getKeySpec(PublicKey publicKey) {
        String keySpec;
        if (publicKey instanceof BCRSAPublicKey) {
            keySpec = publicKey.getAlgorithm().toUpperCase() + Integer.toString(((BCRSAPublicKey) publicKey).getModulus().bitLength());
        } else if (publicKey instanceof BCECPublicKey) {
            keySpec = publicKey.getAlgorithm().toUpperCase();
        } else {
            keySpec = publicKey.getAlgorithm().toUpperCase();
        }
        if (log.isTraceEnabled()) {
            log.trace("Key specification " + keySpec + " determined for public key " + publicKey.getEncoded());
        }
        return keySpec;
    }
}
