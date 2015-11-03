package edu.harvard.hul.ois.fits.service.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

//iimport org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class FitsWrapperPool extends GenericObjectPool<FitsWrapper> {

    public FitsWrapperPool(PooledObjectFactory<FitsWrapper> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }

}

